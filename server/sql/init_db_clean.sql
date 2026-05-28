-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: health_data_manager
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `dim_region`
--

DROP TABLE IF EXISTS `dim_region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dim_region` (
  `id` bigint NOT NULL AUTO_INCREMENT ,
  `region_code` varchar(20) NOT NULL ,
  `region_name` varchar(100) NOT NULL ,
  `region_level` tinyint NOT NULL COMMENT '级别:1省 2市 3区',
  `parent_id` bigint DEFAULT NULL COMMENT '服务记录 ID',
  `sort_order` int DEFAULT '0' ,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `region_code` (`region_code`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dim_region`
--

LOCK TABLES `dim_region` WRITE;
/*!40000 ALTER TABLE `dim_region` DISABLE KEYS */;

/*!40000 ALTER TABLE `dim_region` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dim_population`
--

DROP TABLE IF EXISTS `dim_population`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dim_population` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `region_id` bigint NOT NULL COMMENT '区域 ID',
  `total_population` int NOT NULL DEFAULT '0' ,
  `male_population` int DEFAULT '0' ,
  `female_population` int DEFAULT '0' ,
  `age_0_14` int DEFAULT '0' COMMENT '0-14岁人口',
  `age_15_59` int DEFAULT '0' COMMENT '15-59岁人口',
  `age_60_plus` int DEFAULT '0' COMMENT '60岁及以上人口',
  `stat_year` int NOT NULL ,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_year` (`region_id`,`stat_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dim_population`
--

LOCK TABLES `dim_population` WRITE;
/*!40000 ALTER TABLE `dim_population` DISABLE KEYS */;
/*!40000 ALTER TABLE `dim_population` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_institution`
--

DROP TABLE IF EXISTS `medical_institution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_institution` (
  `id` bigint NOT NULL AUTO_INCREMENT ,
  `org_code` varchar(30) NOT NULL ,
  `org_name` varchar(200) NOT NULL ,
  `org_type` varchar(50) NOT NULL ,
  `org_level` varchar(20) DEFAULT NULL ,
  `region_id` bigint NOT NULL COMMENT '接诊医生 ID',
  `address` varchar(300) DEFAULT NULL ,
  `contact_phone` varchar(30) DEFAULT NULL ,
  `is_active` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用:1启用 0停用',
  `established_date` date DEFAULT NULL ,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `org_code` (`org_code`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_institution`
--

LOCK TABLES `medical_institution` WRITE;
/*!40000 ALTER TABLE `medical_institution` DISABLE KEYS */;

/*!40000 ALTER TABLE `medical_institution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_staff`
--

DROP TABLE IF EXISTS `medical_staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_staff` (
  `id` bigint NOT NULL AUTO_INCREMENT ,
  `staff_code` varchar(30) NOT NULL ,
  `staff_name` varchar(50) NOT NULL ,
  `gender` tinyint DEFAULT NULL COMMENT '性别:1男 2女',
  `birth_date` date DEFAULT NULL ,
  `org_id` bigint NOT NULL COMMENT '接诊医生 ID',
  `department` varchar(100) DEFAULT NULL ,
  `job_title` varchar(50) DEFAULT NULL ,
  `job_category` varchar(50) DEFAULT NULL ,
  `education` varchar(30) DEFAULT NULL ,
  `is_active` tinyint NOT NULL DEFAULT '1' ,
  `entry_date` date DEFAULT NULL ,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `staff_code` (`staff_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5207 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_staff`
--

LOCK TABLES `medical_staff` WRITE;
/*!40000 ALTER TABLE `medical_staff` DISABLE KEYS */;

/*!40000 ALTER TABLE `medical_staff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_bed`
--

DROP TABLE IF EXISTS `medical_bed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT ,
  `org_id` bigint NOT NULL COMMENT '接诊医生 ID',
  `bed_count` int NOT NULL DEFAULT '0' ,
  `actual_bed_count` int NOT NULL DEFAULT '0' ,
  `used_bed_count` int NOT NULL DEFAULT '0' ,
  `bed_usage_rate` decimal(5,2) DEFAULT NULL COMMENT '使用率(百分比)',
  `dept_type` varchar(50) DEFAULT NULL ,
  `stat_year` int NOT NULL ,
  `stat_month` int DEFAULT NULL ,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1656 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_bed`
--

LOCK TABLES `medical_bed` WRITE;
/*!40000 ALTER TABLE `medical_bed` DISABLE KEYS */;

/*!40000 ALTER TABLE `medical_bed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_service`
--

DROP TABLE IF EXISTS `medical_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_service` (
  `id` bigint NOT NULL AUTO_INCREMENT ,
  `service_code` varchar(30) NOT NULL ,
  `org_id` bigint NOT NULL COMMENT '接诊医生 ID',
  `patient_name` varchar(50) DEFAULT NULL ,
  `patient_gender` tinyint DEFAULT NULL ,
  `patient_age` int DEFAULT NULL ,
  `service_type` varchar(50) NOT NULL ,
  `department` varchar(100) DEFAULT NULL ,
  `diagnosis_code` varchar(30) DEFAULT NULL COMMENT '诊断编码(ICD-10)',
  `diagnosis_name` varchar(200) DEFAULT NULL ,
  `doctor_id` bigint DEFAULT NULL COMMENT '接诊医生 ID',
  `service_date` date NOT NULL ,
  `discharge_date` date DEFAULT NULL ,
  `days_in_hospital` int DEFAULT NULL ,
  `service_status` tinyint NOT NULL DEFAULT '1' ,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_code` (`service_code`)
) ENGINE=InnoDB AUTO_INCREMENT=50001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_service`
--

LOCK TABLES `medical_service` WRITE;
/*!40000 ALTER TABLE `medical_service` DISABLE KEYS */;







/*!40000 ALTER TABLE `medical_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medical_cost`
--

DROP TABLE IF EXISTS `medical_cost`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medical_cost` (
  `id` bigint NOT NULL AUTO_INCREMENT ,
  `service_id` bigint NOT NULL COMMENT '服务记录 ID',
  `total_cost` decimal(12,2) NOT NULL DEFAULT '0.00' ,
  `drug_cost` decimal(12,2) DEFAULT '0.00' ,
  `treatment_cost` decimal(12,2) DEFAULT '0.00' ,
  `surgery_cost` decimal(12,2) DEFAULT '0.00' ,
  `inspection_cost` decimal(12,2) DEFAULT '0.00' ,
  `laboratory_cost` decimal(12,2) DEFAULT '0.00' ,
  `bed_cost` decimal(12,2) DEFAULT '0.00' ,
  `nursing_cost` decimal(12,2) DEFAULT '0.00' ,
  `other_cost` decimal(12,2) DEFAULT '0.00' ,
  `insurance_paid` decimal(12,2) DEFAULT '0.00' ,
  `self_paid` decimal(12,2) DEFAULT '0.00' ,
  `cost_category` varchar(30) DEFAULT NULL ,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_id` (`service_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42532 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medical_cost`
--

LOCK TABLES `medical_cost` WRITE;
/*!40000 ALTER TABLE `medical_cost` DISABLE KEYS */;





/*!40000 ALTER TABLE `medical_cost` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-27 17:58:33
