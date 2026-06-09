import os, sys, json, time, ssl
import certifi

os.environ["SSL_CERT_FILE"] = certifi.where()
os.environ["REQUESTS_CA_BUNDLE"] = certifi.where()

_SSL_CTX = ssl.create_default_context(cafile=certifi.where())

import aiohttp
_original_init = aiohttp.TCPConnector.__init__
def _patched_init(self, *, ssl=None, **kwargs):
    if ssl is None:
        ssl = _SSL_CTX
    _original_init(self, ssl=ssl, **kwargs)
aiohttp.TCPConnector.__init__ = _patched_init

from dashscope.audio.asr import Recognition, RecognitionCallback, RecognitionResult


class _CB(RecognitionCallback):
    def __init__(self):
        self.text = ""
        self.done = False
        self.error = ""

    def on_open(self): pass
    def on_close(self): self.done = True
    def on_complete(self, r): self.done = True
    def on_error(self, r): self.done = True; self.error = str(r)
    def on_event(self, r):
        s = r.get_sentence()
        if s and isinstance(s, dict):
            t = s.get("text", "")
            if t: self.text += t


if __name__ == "__main__":
    if len(sys.argv) < 4:
        print(json.dumps({"error": "need 3 args"}))
        sys.exit(1)

    cb = _CB()
    r = Recognition(model=sys.argv[1], callback=cb, format="wav", sample_rate=16000)
    r.call(sys.argv[2])

    for _ in range(60):
        if cb.done:
            break
        time.sleep(1)

    print(json.dumps({"text": cb.text, "done": cb.done, "error": cb.error}, ensure_ascii=False))
