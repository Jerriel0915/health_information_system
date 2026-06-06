import logging
import pymysql
import pandas as pd
import numpy as np
from datetime import datetime
from sklearn.ensemble import IsolationForest
from db import get_connection

logger = logging.getLogger(__name__)

# Cost query - built without raw SQL in shell
CQ_PARTS = [
    "SELECT c.cost_id, c.org_id, mi.org_name, c.service_type,",
    "c.total_amount, c.self_pay, c.insurance_pay, c.service_date",
    "FROM medical_cost c",
    "LEFT JOIN medical_institution mi ON c.org_id = mi.org_id",
    "WHERE c.service_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)",
    "ORDER BY c.service_date DESC LIMIT 50000"
]
CQ = chr(32).join(CQ_PARTS)

SQ_PARTS = [
    "SELECT s.service_id, s.patient_name, s.patient_id_card,",
    "s.org_id, mi.org_name, s.service_date, s.service_type",
    "FROM medical_service s",
    "LEFT JOIN medical_institution mi ON s.org_id = mi.org_id",
    "WHERE s.service_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)",
    "ORDER BY s.service_date DESC LIMIT 50000"
]
SQ = chr(32).join(SQ_PARTS)


def _fetch_cost_data(conn):
    return pd.read_sql(CQ, conn)


def _fetch_service_data(conn):
    return pd.read_sql(SQ, conn)


def detect_cost_anomalies(df):
    res = []
    if df.empty:
        return res
    cols = ["mean","std","count"]
    stats = df.groupby("service_type")["total_amount"].agg(cols).reset_index()
    stats = stats[stats["count"] >= 5]
    for _, r in stats.iterrows():
        st = r["service_type"]
        mv = r["mean"]
        sv = r["std"]
        if sv == 0 or pd.isna(sv):
            continue
        sub = df[df["service_type"] == st]
        th = mv + 3 * sv
        for _, a in sub[sub["total_amount"] > th].iterrows():
            desc = chr(36153) + chr(29992) + str(round(a['total_amount'],2)) + chr(20803) + chr(36229) + chr(20986) + chr(21516) + chr(22343) + str(round(mv,2)) + chr(20803) + chr(51) + chr(20493) + chr(26631) + chr(20934) + chr(24046)
            res.append({
                "recordId": str(a.get("cost_id","")),
                "orgName": a.get("org_name",""),
                "anomalyType": chr(36829)+chr(35268)+chr(25910)+chr(36153),
                "description": desc,
                "riskLevel": chr(39640) if a["total_amount"] > mv+5*sv else chr(20013),
                "detectTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            })
    return res


def detect_visit_anomalies(df):
    res = []
    if df.empty or "patient_id_card" not in df.columns:
        return res
    v = df[df["patient_id_card"].notna() & (df["patient_id_card"] != "")]
    if v.empty:
        return res
    v = v.sort_values(["patient_id_card","service_date"])
    v["service_date"] = pd.to_datetime(v["service_date"])
    for _, g in v.groupby("patient_id_card"):
        dts = g["service_date"].values
        for i in range(len(dts)):
            cnt = 1
            for j in range(i+1, len(dts)):
                if (dts[j] - dts[i]) <= pd.Timedelta(days=7):
                    cnt += 1
                else:
                    break
            if cnt >= 3:
                w = g.iloc[i:i+cnt]
                nm = str(w["patient_name"].iloc[0])
                desc = chr(30149) + chr(32773) + nm + chr(55) + chr(22825) + chr(20869) + chr(23601) + chr(35786) + str(cnt) + chr(27425)
                res.append({
                    "recordId": str(w["service_id"].iloc[0]),
                    "orgName": w["org_name"].iloc[0] if "org_name" in w.columns else "",
                    "anomalyType": chr(24322)+chr(24120)+chr(23601)+chr(35786),
                    "description": desc,
                    "riskLevel": chr(39640) if cnt >= 5 else chr(20013),
                    "detectTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                })
                break
    return res


def detect_isolation_forest(dfc):
    res = []
    if dfc.empty or len(dfc) < 10:
        return res
    f = dfc[["total_amount","self_pay","insurance_pay"]].copy().fillna(0)
    model = IsolationForest(n_estimators=100, contamination=0.01, random_state=42, n_jobs=-1)
    preds = model.fit_predict(f)
    scores = model.score_samples(f)
    for idx in np.where(preds == -1)[0]:
        r = dfc.iloc[idx]
        desc = chr(24322)+chr(24120)+chr(20998)+chr(25968)+chr(58)+str(round(scores[idx],4))+chr(44)+chr(37329)+chr(39069)+chr(58)+str(round(r['total_amount'],2))+chr(20803)
        res.append({
            "recordId": str(r.get("cost_id","")),
            "orgName": r.get("org_name",""),
            "anomalyType": chr(24322)+chr(24120)+chr(36153)+chr(29992),
            "description": desc,
            "riskLevel": chr(39640) if scores[idx] < np.percentile(scores,1) else chr(20013),
            "detectTime": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        })
    return res


def run_detection():
    conn = None
    try:
        conn = get_connection()
        dfc = _fetch_cost_data(conn)
        dfs = _fetch_service_data(conn)
        all_items = detect_cost_anomalies(dfc) + detect_visit_anomalies(dfs) + detect_isolation_forest(dfc)
        seen = set()
        unique = []
        for a in all_items:
            k = (a["recordId"], a["anomalyType"])
            if k not in seen:
                seen.add(k)
                unique.append(a)
        cc = sum(1 for a in unique if chr(36829)+chr(35268) in a['anomalyType'] or chr(24322)+chr(24120)+chr(36153)+chr(29992) in a['anomalyType'])
        vc = sum(1 for a in unique if chr(24322)+chr(24120)+chr(23601)+chr(35786) in a['anomalyType'])
        return {
            "code": 200,
            "data": {
                "stats": {"total": len(unique), "costAnomaly": cc, "visitAnomaly": vc},
                "list": unique[:100],
            },
        }
    except Exception as e:
        logger.error(f"Detection failed: {e}")
        return {"code": 500, "msg": f"Detection failed: {e}"}
    finally:
        if conn:
            conn.close()