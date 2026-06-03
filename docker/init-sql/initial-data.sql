-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: jeez_fitness
-- ------------------------------------------------------
-- Server version	8.0.44

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
-- Table structure for table `jeez_card_purchase_order`
--

DROP TABLE IF EXISTS `jeez_card_purchase_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_card_purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `member_id` bigint NOT NULL COMMENT '会员ID(关联jeez_member表)',
  `card_type_id` bigint NOT NULL COMMENT '卡种ID(关联jeez_membership_card_type表)',
  `store_id` bigint NOT NULL COMMENT '购买门店ID',
  `quantity` int DEFAULT '1' COMMENT '购买数量',
  `original_price` decimal(10,2) NOT NULL COMMENT '原价',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `final_price` decimal(10,2) NOT NULL COMMENT '最终支付金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付方式(WECHAT:微信,ALIPAY:支付宝,BANK_CARD:银行卡)',
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '支付状态(PENDING:待支付,PAID:已支付,REFUNDED:已退款,CANCELLED:已取消)',
  `order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '订单状态(PENDING:待支付,PAID:已支付,COMPLETED:已完成,CANCELLED:已取消,REFUNDED:已退款)',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `coupon_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优惠券码',
  `invite_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邀请码',
  `buyer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '购买人姓名',
  `buyer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '购买人手机号',
  `buyer_id_card` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '购买人身份证号',
  `expire_time` datetime DEFAULT NULL COMMENT '订单过期时间',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款原因',
  `third_party_trade_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方交易号',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_card_type_id` (`card_type_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_payment_time` (`payment_time`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_card_purchase_card_type` FOREIGN KEY (`card_type_id`) REFERENCES `jeez_membership_card_type` (`id`),
  CONSTRAINT `fk_card_purchase_member` FOREIGN KEY (`member_id`) REFERENCES `jeez_member` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡购买订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_card_purchase_order`
--

LOCK TABLES `jeez_card_purchase_order` WRITE;
/*!40000 ALTER TABLE `jeez_card_purchase_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_card_purchase_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_card_usage_record`
--

DROP TABLE IF EXISTS `jeez_card_usage_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_card_usage_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `card_id` bigint NOT NULL COMMENT '会员卡ID(关联jeez_member_card.id)',
  `member_id` bigint NOT NULL COMMENT '会员ID(关联jeez_member.id)',
  `store_id` bigint NOT NULL COMMENT '门店ID(关联jeez_store.id)',
  `usage_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '使用类型(CHECK_IN:签到,COURSE:课程,EQUIPMENT:设备,SERVICE:服务,OTHER:其他)',
  `usage_time` datetime NOT NULL COMMENT '使用时间',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_usage_time` (`card_id`,`usage_time`),
  KEY `idx_card_id` (`card_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_usage_type` (`usage_type`),
  KEY `idx_usage_time` (`usage_time`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_member_usage_time` (`member_id`,`usage_time`),
  KEY `idx_card_usage_time` (`card_id`,`usage_time`),
  KEY `idx_store_date_range` (`store_id`,`usage_time`),
  KEY `idx_store_date_type` (`store_id`,`usage_time`,`usage_type`),
  KEY `idx_member_time_status` (`member_id`,`usage_time`,`deleted`),
  CONSTRAINT `fk_card_usage_record_card` FOREIGN KEY (`card_id`) REFERENCES `jeez_member_card` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_card_usage_record_member` FOREIGN KEY (`member_id`) REFERENCES `jeez_member` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_card_usage_record_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡使用记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_card_usage_record`
--

LOCK TABLES `jeez_card_usage_record` WRITE;
/*!40000 ALTER TABLE `jeez_card_usage_record` DISABLE KEYS */;
INSERT INTO `jeez_card_usage_record` VALUES (1,1,1,1,'CHECK_IN','2024-12-01 09:00:00',0),(2,1,1,1,'EQUIPMENT','2024-12-03 18:30:00',0),(3,2,2,1,'COURSE','2024-12-02 14:00:00',0),(4,2,2,1,'EQUIPMENT','2024-12-06 11:20:00',0);
/*!40000 ALTER TABLE `jeez_card_usage_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_assessment`
--

DROP TABLE IF EXISTS `jeez_coach_assessment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_assessment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '考核ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `assessment_type` tinyint NOT NULL COMMENT '考核类型(1:入职考核 2:晋升考核 3:年度考核 4:专项考核)',
  `assessment_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '考核名称',
  `current_level_id` tinyint DEFAULT NULL COMMENT '当前等级ID',
  `target_level_id` tinyint DEFAULT NULL COMMENT '目标等级ID',
  `assessment_date` date NOT NULL COMMENT '考核日期',
  `assessor_id` bigint DEFAULT NULL COMMENT '考核官ID',
  `assessment_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '考核地点',
  `total_score` decimal(5,2) DEFAULT '0.00' COMMENT '总分',
  `max_score` decimal(5,2) DEFAULT '100.00' COMMENT '满分',
  `pass_score` decimal(5,2) DEFAULT '60.00' COMMENT '及格分',
  `assessment_status` tinyint DEFAULT '1' COMMENT '考核状态(1:待考核 2:考核中 3:已完成 4:未通过 5:已取消)',
  `result_status` tinyint DEFAULT NULL COMMENT '结果状态(1:优秀 2:良好 3:及格 4:不及格)',
  `theoretical_score` decimal(5,2) DEFAULT NULL COMMENT '理论考试成绩',
  `practical_score` decimal(5,2) DEFAULT NULL COMMENT '实践考试成绩',
  `teaching_score` decimal(5,2) DEFAULT NULL COMMENT '教学能力成绩',
  `communication_score` decimal(5,2) DEFAULT NULL COMMENT '沟通能力成绩',
  `safety_score` decimal(5,2) DEFAULT NULL COMMENT '安全意识成绩',
  `assessment_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '考核备注',
  `assessor_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '考核官反馈',
  `improvement_suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '改进建议',
  `next_assessment_date` date DEFAULT NULL COMMENT '下次考核日期',
  `certificate_issued` tinyint(1) DEFAULT '0' COMMENT '是否发证',
  `certificate_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '证书编号',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_assessment_type` (`assessment_type`),
  KEY `idx_assessment_date` (`assessment_date`),
  KEY `idx_assessment_status` (`assessment_status`),
  KEY `idx_result_status` (`result_status`),
  KEY `idx_assessor_id` (`assessor_id`),
  KEY `idx_created_time` (`created_time`),
  KEY `current_level_id` (`current_level_id`),
  KEY `target_level_id` (`target_level_id`),
  CONSTRAINT `jeez_coach_assessment_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_coach_assessment_ibfk_2` FOREIGN KEY (`current_level_id`) REFERENCES `jeez_dict_coach_skill_level` (`id`),
  CONSTRAINT `jeez_coach_assessment_ibfk_3` FOREIGN KEY (`target_level_id`) REFERENCES `jeez_dict_coach_skill_level` (`id`),
  CONSTRAINT `jeez_coach_assessment_ibfk_4` FOREIGN KEY (`assessor_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练考核记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_assessment`
--

LOCK TABLES `jeez_coach_assessment` WRITE;
/*!40000 ALTER TABLE `jeez_coach_assessment` DISABLE KEYS */;
INSERT INTO `jeez_coach_assessment` VALUES (1,2,1,'新教练入职考核',NULL,2,'2023-07-15',1,NULL,85.50,100.00,60.00,3,2,88.00,84.00,86.00,85.00,84.00,NULL,'专业能力扎实，教学态度认真，建议加强课程设计能力',NULL,'2024-07-15',0,NULL,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0),(2,3,1,'瑜伽教练资格考核',NULL,3,'2023-02-01',1,NULL,88.00,100.00,70.00,3,1,90.00,86.00,88.00,90.00,86.00,NULL,'瑜伽技能优秀，体位法标准，沟通能力突出',NULL,'2024-02-01',0,NULL,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0),(3,4,2,'游泳教练晋升考核',2,3,'2023-09-20',1,NULL,92.50,100.00,75.00,3,1,92.00,94.00,90.00,93.00,93.00,NULL,'技术水平优秀，教学方法多样，安全意识强',NULL,'2024-09-20',0,NULL,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0);
/*!40000 ALTER TABLE `jeez_coach_assessment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_available_time`
--

DROP TABLE IF EXISTS `jeez_coach_available_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_available_time` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '可用时间段ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `available_date` date NOT NULL COMMENT '可用日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `time_slot_duration` int DEFAULT '60' COMMENT '时间段长度(分钟)',
  `max_bookings` int DEFAULT '1' COMMENT '最大预约数量',
  `current_bookings` int DEFAULT '0' COMMENT '当前预约数量',
  `booking_type` tinyint DEFAULT '1' COMMENT '预约类型(1:一对一 2:小组课 3:团体课)',
  `price_per_session` decimal(8,2) DEFAULT NULL COMMENT '单次课程价格',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上课地点',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '课程要求',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:可预约 2:已约满 3:已取消 4:已过期)',
  `repeat_type` tinyint DEFAULT '0' COMMENT '重复类型(0:不重复 1:每日 2:每周 3:每月)',
  `repeat_end_date` date DEFAULT NULL COMMENT '重复结束日期',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_available_date` (`available_date`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_status` (`status`),
  KEY `idx_booking_type` (`booking_type`),
  KEY `idx_date_time` (`available_date`,`start_time`),
  KEY `idx_created_time` (`created_time`),
  CONSTRAINT `jeez_coach_available_time_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_coach_available_time_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练可用时间段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_available_time`
--

LOCK TABLES `jeez_coach_available_time` WRITE;
/*!40000 ALTER TABLE `jeez_coach_available_time` DISABLE KEYS */;
INSERT INTO `jeez_coach_available_time` VALUES (1,2,1,'2025-11-06','09:00:00','10:00:00',60,1,0,1,300.00,'力量训练区','适合有一定基础的学员',NULL,1,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(2,2,1,'2025-11-06','10:00:00','11:00:00',60,1,0,1,300.00,'力量训练区','适合有一定基础的学员',NULL,1,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(3,2,1,'2025-11-06','14:00:00','15:00:00',60,1,0,1,300.00,'力量训练区','适合有一定基础的学员',NULL,1,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(4,3,1,'2025-11-06','07:00:00','08:00:00',60,3,0,2,88.00,'瑜伽室A','适合初学者',NULL,1,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',3,3,0),(5,3,1,'2025-11-06','08:00:00','09:00:00',60,3,0,2,88.00,'瑜伽室A','适合初学者',NULL,1,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',3,3,0),(6,4,1,'2025-11-06','06:00:00','07:00:00',60,2,0,1,280.00,'游泳池','会游泳者优先',NULL,1,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',4,4,0);
/*!40000 ALTER TABLE `jeez_coach_available_time` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_continuing_education`
--

DROP TABLE IF EXISTS `jeez_coach_continuing_education`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_continuing_education` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '继续教育ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `education_type` tinyint NOT NULL COMMENT '教育类型(1:培训课程 2:研讨会 3:工作坊 4:在线学习 5:学术会议)',
  `program_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `organizer` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主办方',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `total_hours` decimal(5,1) DEFAULT '0.0' COMMENT '总学时',
  `completion_status` tinyint DEFAULT '1' COMMENT '完成状态(1:进行中 2:已完成 3:未完成)',
  `certificate_obtained` tinyint(1) DEFAULT '0' COMMENT '是否获得证书',
  `certificate_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '证书图片URL',
  `score_or_rating` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '成绩或评级',
  `key_learnings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '主要收获',
  `application_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '应用说明',
  `credits_earned` decimal(5,2) DEFAULT '0.00' COMMENT '获得学分',
  `verification_status` tinyint DEFAULT '1' COMMENT '验证状态(1:待验证 2:已验证 3:验证不通过)',
  `verifier_id` bigint DEFAULT NULL COMMENT '验证人ID',
  `verification_time` datetime DEFAULT NULL COMMENT '验证时间',
  `verification_notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '验证备注',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:有效 2:已过期)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_education_type` (`education_type`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_completion_status` (`completion_status`),
  KEY `idx_verification_status` (`verification_status`),
  KEY `idx_created_time` (`created_time`),
  KEY `verifier_id` (`verifier_id`),
  CONSTRAINT `jeez_coach_continuing_education_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_coach_continuing_education_ibfk_2` FOREIGN KEY (`verifier_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练继续教育记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_continuing_education`
--

LOCK TABLES `jeez_coach_continuing_education` WRITE;
/*!40000 ALTER TABLE `jeez_coach_continuing_education` DISABLE KEYS */;
INSERT INTO `jeez_coach_continuing_education` VALUES (1,2,1,'高级私教技能培训','国际健身协会','2024-03-01','2024-03-03',24.0,2,1,NULL,'A','学习了最新的训练方法和康复技术',NULL,24.00,2,1,'2024-03-10 10:00:00',NULL,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',2,2,0),(2,3,2,'瑜伽教学研讨会','瑜伽联盟','2024-01-15','2024-01-16',16.0,2,1,NULL,'优秀','探讨了瑜伽教学的创新方法',NULL,16.00,2,1,'2024-01-20 14:00:00',NULL,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',3,3,0),(3,4,4,'在线游泳教学课程','游泳教练协会','2024-02-01','2024-02-15',20.0,2,1,NULL,'95分','掌握了科学的教学方法',NULL,20.00,2,1,'2024-02-20 09:00:00',NULL,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',4,4,0);
/*!40000 ALTER TABLE `jeez_coach_continuing_education` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_info`
--

DROP TABLE IF EXISTS `jeez_coach_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '教练信息ID',
  `user_id` bigint NOT NULL COMMENT '用户ID(关联jeez_user.id)',
  `coach_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教练编号',
  `skill_level_id` tinyint DEFAULT NULL COMMENT '技能等级ID(关联jeez_dict_coach_skill_level.id)',
  `specialty_domains` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '专业领域ID(JSON数组格式)',
  `work_years` int DEFAULT '0' COMMENT '工作年限',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '个人简介',
  `specialties` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '专长描述',
  `certification_status` tinyint DEFAULT '0' COMMENT '资质认证状态(0:未认证 1:已认证 2:认证中)',
  `work_status` tinyint DEFAULT '1' COMMENT '工作状态(0:离职 1:在职 2:休假)',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `resignation_date` date DEFAULT NULL COMMENT '离职日期',
  `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
  `current_experience_years` int DEFAULT '0' COMMENT '当前经验年限',
  `certification_count` int DEFAULT '0' COMMENT '持有认证数量',
  `training_count` int DEFAULT '0' COMMENT '参与培训数量',
  `total_sessions` int DEFAULT '0' COMMENT '累计授课次数',
  `total_students` int DEFAULT '0' COMMENT '累计服务学员数',
  `average_rating` decimal(3,2) DEFAULT '0.00' COMMENT '平均评分(1-5)',
  `price_multiplier` decimal(5,2) DEFAULT '1.00' COMMENT '价格倍数',
  `min_session_price` decimal(10,2) DEFAULT NULL COMMENT '最低单次课程价格',
  `max_session_price` decimal(10,2) DEFAULT NULL COMMENT '最高单次课程价格',
  `level_icon_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '等级图标URL',
  `level_color_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '等级颜色代码',
  `level_effective_date` date DEFAULT NULL COMMENT '等级生效日期',
  `level_expiry_date` date DEFAULT NULL COMMENT '等级过期日期',
  `can_upgrade` tinyint(1) DEFAULT '1' COMMENT '是否可升级',
  `next_level_requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '下一级别要求(JSON格式)',
  `benefits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '等级权益描述',
  `remarks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注信息',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `coach_code` (`coach_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coach_code` (`coach_code`),
  KEY `idx_skill_level_id` (`skill_level_id`),
  KEY `idx_work_status` (`work_status`),
  KEY `idx_certification_status` (`certification_status`),
  KEY `idx_hire_date` (`hire_date`),
  KEY `idx_average_rating` (`average_rating`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_coach_info_skill_level` FOREIGN KEY (`skill_level_id`) REFERENCES `jeez_dict_coach_skill_level` (`id`),
  CONSTRAINT `fk_coach_info_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_info`
--

LOCK TABLES `jeez_coach_info` WRITE;
/*!40000 ALTER TABLE `jeez_coach_info` DISABLE KEYS */;
INSERT INTO `jeez_coach_info` VALUES (1,2,'JZ_COACH_001',3,'[1,2,7]',5,'专注于力量训练和体能提升，帮助会员达成健身目标。持有ACE私人教练认证和国家职业资格健身教练证书。','力量训练、体能提升、减脂塑形、功能性训练',1,1,'2023-01-15',NULL,NULL,5,2,12,156,89,4.80,1.50,300.00,450.00,'https://example.com/icons/level3.png','#FF6B35','2023-01-15',NULL,1,NULL,'高级私教待遇、优先分配优质客户、专业培训机会、设备优先使用权',NULL,'2025-12-12 09:45:55','2025-12-12 09:45:55',1,1,0),(2,3,'JZ_COACH_002',3,'[1,4,5]',3,'专业瑜伽教练，擅长哈他瑜伽、流瑜伽和阴瑜伽。RYT200认证，帮助学员放松身心，提升柔韧性。','哈他瑜伽、流瑜伽、阴瑜伽、冥想放松、体态矫正',1,1,'2023-02-01',NULL,NULL,3,1,8,120,65,4.85,1.30,88.00,150.00,'https://example.com/icons/level3.png','#4ECDC4','2023-02-01',NULL,1,NULL,'高级瑜伽教练待遇、专属瑜伽室使用权、瑜伽进修培训',NULL,'2025-12-12 09:45:55','2025-12-12 09:45:55',1,1,0),(3,4,'JZ_COACH_003',3,'[4,6]',4,'专业游泳教练，具备救生员资格证书。擅长自由泳、蛙泳教学，注重安全意识培养。','自由泳、蛙泳、仰泳、蝶泳、游泳教学、水中安全',1,1,'2023-03-10',NULL,NULL,4,2,10,180,95,4.75,1.40,280.00,400.00,'https://example.com/icons/level3.png','#45B7D1','2023-03-10',NULL,0,NULL,'高级游泳教练待遇、泳池优先使用权、救生培训资格',NULL,'2025-12-12 09:45:55','2025-12-12 09:45:55',1,1,0);
/*!40000 ALTER TABLE `jeez_coach_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_level_certification`
--

DROP TABLE IF EXISTS `jeez_coach_level_certification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_level_certification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '等级认证ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `assessment_id` bigint DEFAULT NULL COMMENT '关联考核ID',
  `previous_level_id` tinyint DEFAULT NULL COMMENT '原等级ID',
  `new_level_id` tinyint NOT NULL COMMENT '新等级ID',
  `certification_type` tinyint NOT NULL COMMENT '认证类型(1:首次认证 2:晋升认证 3:年度复审 4:专项认证)',
  `certification_date` date NOT NULL COMMENT '认证日期',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期',
  `is_permanent` tinyint(1) DEFAULT '0' COMMENT '是否永久有效',
  `certifier_id` bigint DEFAULT NULL COMMENT '认证官ID',
  `certification_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '认证编号',
  `certificate_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '证书图片URL',
  `conditions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '认证条件(JSON格式)',
  `privileges` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '等级特权(JSON格式)',
  `restrictions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '限制条件(JSON格式)',
  `next_review_date` date DEFAULT NULL COMMENT '下次复审日期',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:有效 2:已过期 3:已撤销 4:暂停)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_assessment_id` (`assessment_id`),
  KEY `idx_previous_level_id` (`previous_level_id`),
  KEY `idx_new_level_id` (`new_level_id`),
  KEY `idx_certification_date` (`certification_date`),
  KEY `idx_status` (`status`),
  KEY `certifier_id` (`certifier_id`),
  CONSTRAINT `jeez_coach_level_certification_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_coach_level_certification_ibfk_2` FOREIGN KEY (`assessment_id`) REFERENCES `jeez_coach_assessment` (`id`),
  CONSTRAINT `jeez_coach_level_certification_ibfk_3` FOREIGN KEY (`previous_level_id`) REFERENCES `jeez_dict_coach_skill_level` (`id`),
  CONSTRAINT `jeez_coach_level_certification_ibfk_4` FOREIGN KEY (`new_level_id`) REFERENCES `jeez_dict_coach_skill_level` (`id`),
  CONSTRAINT `jeez_coach_level_certification_ibfk_5` FOREIGN KEY (`certifier_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练等级认证记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_level_certification`
--

LOCK TABLES `jeez_coach_level_certification` WRITE;
/*!40000 ALTER TABLE `jeez_coach_level_certification` DISABLE KEYS */;
INSERT INTO `jeez_coach_level_certification` VALUES (1,2,1,NULL,2,1,'2023-07-15','2023-07-16','2024-07-15',0,1,'CL20230001',NULL,'{\"min_session_per_month\": 20, \"required_education_hours\": 10}','{\"max_group_size\": 15, \"price_multiplier\": 1.2}',NULL,'2024-06-15',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0),(2,3,2,NULL,3,1,'2023-02-01','2023-02-02','2024-02-01',0,1,'CL20230002',NULL,'{\"min_session_per_month\": 25, \"required_education_hours\": 15}','{\"max_group_size\": 20, \"price_multiplier\": 1.5}',NULL,'2024-01-01',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0),(3,4,3,2,3,2,'2023-09-20','2023-09-21','2024-09-20',0,1,'CL20230003',NULL,'{\"min_session_per_month\": 30, \"required_education_hours\": 20}','{\"max_group_size\": 25, \"price_multiplier\": 1.8}',NULL,'2024-08-20',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0);
/*!40000 ALTER TABLE `jeez_coach_level_certification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_qualification`
--

DROP TABLE IF EXISTS `jeez_coach_qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_qualification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资格证书ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `qualification_type_id` tinyint NOT NULL COMMENT '资格类型ID',
  `qualification_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '证书名称',
  `certificate_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '证书编号',
  `issuing_organization` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发证机构',
  `issue_date` date DEFAULT NULL COMMENT '发证日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期',
  `is_permanent` tinyint(1) DEFAULT '0' COMMENT '是否永久有效',
  `certificate_image` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '证书图片URL',
  `verification_status` tinyint DEFAULT '1' COMMENT '验证状态(1:待审核 2:审核通过 3:审核不通过)',
  `verification_time` datetime DEFAULT NULL COMMENT '审核时间',
  `verifier_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `verification_notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核备注',
  `score` decimal(5,2) DEFAULT NULL COMMENT '证书评定分数',
  `rank` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '证书等级',
  `specialization` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业方向',
  `continuing_education_required` tinyint(1) DEFAULT '0' COMMENT '是否需要继续教育',
  `last_renewal_date` date DEFAULT NULL COMMENT '最近续期日期',
  `next_renewal_date` date DEFAULT NULL COMMENT '下次续期日期',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:有效 2:已过期 3:已吊销 4:待更新)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_qualification_type_id` (`qualification_type_id`),
  KEY `idx_verification_status` (`verification_status`),
  KEY `idx_status` (`status`),
  KEY `idx_issue_date` (`issue_date`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `verifier_id` (`verifier_id`),
  CONSTRAINT `jeez_coach_qualification_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_coach_qualification_ibfk_2` FOREIGN KEY (`qualification_type_id`) REFERENCES `jeez_dict_coach_qualification_type` (`id`),
  CONSTRAINT `jeez_coach_qualification_ibfk_3` FOREIGN KEY (`verifier_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练资格证书表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_qualification`
--

LOCK TABLES `jeez_coach_qualification` WRITE;
/*!40000 ALTER TABLE `jeez_coach_qualification` DISABLE KEYS */;
INSERT INTO `jeez_coach_qualification` VALUES (1,2,1,'国家职业资格健身教练证书','NFPT20230001','国家体育总局','2023-06-15','2026-06-15',0,NULL,2,'2023-07-01 10:00:00',1,NULL,85.50,'中级','力量训练',0,NULL,NULL,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0),(2,2,2,'ACE私人教练认证','ACE-2023-001234','美国运动医学会','2023-03-20','2025-03-20',0,NULL,2,'2023-04-01 14:30:00',1,NULL,92.00,'优秀','综合训练',0,NULL,NULL,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0),(3,3,3,'RYT200瑜伽教练认证','RYT200-2023-005678','瑜伽联盟','2023-01-10','2025-01-10',0,NULL,2,'2023-02-01 09:00:00',1,NULL,88.00,'高级','哈他瑜伽',0,NULL,NULL,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0),(4,4,1,'国家职业资格游泳教练证书','SWIM20230002','国家体育总局','2022-08-25','2025-08-25',0,NULL,2,'2022-09-10 16:00:00',1,NULL,90.50,'中级','游泳教学',0,NULL,NULL,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1,0);
/*!40000 ALTER TABLE `jeez_coach_qualification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_schedule`
--

DROP TABLE IF EXISTS `jeez_coach_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日程ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `schedule_date` date NOT NULL COMMENT '日程日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `schedule_type` tinyint NOT NULL COMMENT '日程类型(1:私教课 2:团体课 3:休息 4:培训 5:会议)',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID(课程排期ID或预约ID)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日程标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '日程描述',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地点',
  `participant_count` int DEFAULT '0' COMMENT '参与人数',
  `is_bookable` tinyint(1) DEFAULT '0' COMMENT '是否可被预约',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:正常 2:已取消 3:已完成)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_schedule_date` (`schedule_date`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_schedule_type` (`schedule_type`),
  KEY `idx_status` (`status`),
  KEY `idx_related_id` (`related_id`),
  KEY `idx_date_time` (`schedule_date`,`start_time`),
  KEY `idx_created_time` (`created_time`),
  CONSTRAINT `jeez_coach_schedule_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_coach_schedule_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练日程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_schedule`
--

LOCK TABLES `jeez_coach_schedule` WRITE;
/*!40000 ALTER TABLE `jeez_coach_schedule` DISABLE KEYS */;
INSERT INTO `jeez_coach_schedule` VALUES (1,2,1,'2025-11-06','09:00:00','10:00:00',1,1,'私教课 - 李会员','胸肌训练课程','力量训练区',1,0,1,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(2,2,1,'2025-11-06','12:00:00','13:00:00',3,NULL,'午休时间','教练午餐休息',NULL,0,0,1,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(3,3,1,'2025-11-06','07:00:00','08:00:00',1,2,'小班瑜伽 - 王会员等','晨练瑜伽课程','瑜伽室A',3,0,1,'2025-11-05 17:54:22','2025-11-05 17:54:22',3,3,0),(4,4,1,'2025-11-06','06:00:00','07:00:00',1,3,'私教游泳 - 陈会员','自由泳技术训练','游泳池',1,0,1,'2025-11-05 17:54:22','2025-11-05 17:54:22',4,4,0);
/*!40000 ALTER TABLE `jeez_coach_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_service_pricing`
--

DROP TABLE IF EXISTS `jeez_coach_service_pricing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_service_pricing` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '定价ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `service_type` tinyint NOT NULL COMMENT '服务类型(1:一对一私教 2:小班课 3:团体课 4:在线指导)',
  `session_duration` int DEFAULT '60' COMMENT '单次时长(分钟)',
  `base_price` decimal(8,2) NOT NULL COMMENT '基础价格',
  `skill_level_multiplier` decimal(5,3) DEFAULT '1.000' COMMENT '技能等级倍数',
  `final_price` decimal(8,2) GENERATED ALWAYS AS ((`base_price` * `skill_level_multiplier`)) STORED COMMENT '最终价格',
  `price_range_min` decimal(8,2) DEFAULT NULL COMMENT '价格区间最低',
  `price_range_max` decimal(8,2) DEFAULT NULL COMMENT '价格区间最高',
  `member_discount_rate` decimal(5,3) DEFAULT '0.000' COMMENT '会员折扣率',
  `package_discount_rates` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '套餐折扣率(JSON格式)',
  `peak_hour_surcharge` decimal(5,3) DEFAULT '0.000' COMMENT '高峰时段附加费率',
  `weekend_surcharge` decimal(5,3) DEFAULT '0.000' COMMENT '周末附加费',
  `location_surcharge` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '地点附加费(JSON格式)',
  `special_pricing_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '特殊定价规则(JSON格式)',
  `commission_rate` decimal(5,3) DEFAULT '0.600' COMMENT '教练分成比例',
  `max_daily_sessions` int DEFAULT NULL COMMENT '每日最大课程数',
  `min_booking_hours` int DEFAULT '2' COMMENT '最少提前预约小时数',
  `cancellation_policy` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '取消政策(JSON格式)',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期',
  `approval_status` tinyint DEFAULT '1' COMMENT '审批状态(1:待审批 2:已批准 3:已拒绝)',
  `approved_by` bigint DEFAULT NULL COMMENT '审批人ID',
  `approved_time` datetime DEFAULT NULL COMMENT '审批时间',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:有效 2:暂停 3:待调整)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_service_type` (`service_type`),
  KEY `idx_final_price` (`final_price`),
  KEY `idx_effective_date` (`effective_date`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_status` (`status`),
  KEY `approved_by` (`approved_by`),
  CONSTRAINT `jeez_coach_service_pricing_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_coach_service_pricing_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_coach_service_pricing_ibfk_3` FOREIGN KEY (`approved_by`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练服务定价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_service_pricing`
--

LOCK TABLES `jeez_coach_service_pricing` WRITE;
/*!40000 ALTER TABLE `jeez_coach_service_pricing` DISABLE KEYS */;
INSERT INTO `jeez_coach_service_pricing` (`id`, `coach_id`, `store_id`, `service_type`, `session_duration`, `base_price`, `skill_level_multiplier`, `price_range_min`, `price_range_max`, `member_discount_rate`, `package_discount_rates`, `peak_hour_surcharge`, `weekend_surcharge`, `location_surcharge`, `special_pricing_rules`, `commission_rate`, `max_daily_sessions`, `min_booking_hours`, `cancellation_policy`, `effective_date`, `expiry_date`, `approval_status`, `approved_by`, `approved_time`, `status`, `created_time`, `updated_time`, `created_by`, `updated_by`, `is_deleted`) VALUES (1,2,1,1,60,300.00,1.500,NULL,NULL,0.100,'{\"5_sessions\": 0.05, \"10_sessions\": 0.1, \"20_sessions\": 0.15}',0.200,0.100,NULL,NULL,0.600,8,4,NULL,'2023-01-01',NULL,2,1,'2023-01-01 09:00:00',1,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(2,3,1,2,60,150.00,1.300,NULL,NULL,0.050,'{\"5_classes\": 0.1, \"10_classes\": 0.15}',0.150,0.050,NULL,NULL,0.550,12,6,NULL,'2023-01-01',NULL,2,1,'2023-01-01 09:30:00',1,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(3,4,1,1,60,280.00,1.400,NULL,NULL,0.080,'{\"5_sessions\": 0.08, \"10_sessions\": 0.12}',0.250,0.150,NULL,NULL,0.650,6,8,NULL,'2023-01-01',NULL,2,1,'2023-01-01 10:00:00',1,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0);
/*!40000 ALTER TABLE `jeez_coach_service_pricing` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_skill_evaluation`
--

DROP TABLE IF EXISTS `jeez_coach_skill_evaluation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_skill_evaluation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评估ID',
  `assessment_id` bigint NOT NULL COMMENT '考核ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID',
  `skill_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能类别',
  `specialty_id` tinyint DEFAULT NULL COMMENT '专长ID，关联到jeez_dict_coach_specialty表',
  `skill_item` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '技能项目',
  `skill_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '技能描述',
  `evaluation_criteria` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评估标准',
  `score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '得分',
  `max_score` decimal(5,2) NOT NULL DEFAULT '10.00' COMMENT '该项满分',
  `weight` decimal(3,2) DEFAULT '1.00' COMMENT '权重',
  `assessor_comments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '评估人评语',
  `evidence_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '证据说明',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_assessment_id` (`assessment_id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_skill_category` (`skill_category`),
  KEY `fk_skill_evaluation_specialty` (`specialty_id`),
  CONSTRAINT `fk_skill_evaluation_specialty` FOREIGN KEY (`specialty_id`) REFERENCES `jeez_dict_coach_specialty` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `jeez_coach_skill_evaluation_ibfk_1` FOREIGN KEY (`assessment_id`) REFERENCES `jeez_coach_assessment` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_coach_skill_evaluation_ibfk_2` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练技能评估表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_skill_evaluation`
--

LOCK TABLES `jeez_coach_skill_evaluation` WRITE;
/*!40000 ALTER TABLE `jeez_coach_skill_evaluation` DISABLE KEYS */;
INSERT INTO `jeez_coach_skill_evaluation` VALUES (1,1,2,'理论知识',NULL,'运动生理学','运动生理学基础知识掌握程度','能够准确解释运动对人体各系统的影响',8.80,10.00,1.00,'理论基础扎实，概念清晰',NULL,'2025-11-05 18:33:55','2025-11-05 18:33:55'),(2,1,2,'实践技能',4,'器械使用','各种健身器械的正确使用方法','能够安全、正确地使用各种器械并指导会员',8.40,10.00,1.00,'操作熟练，安全意识强',NULL,'2025-11-05 18:33:55','2025-12-14 13:04:00'),(3,1,2,'教学能力',NULL,'课程设计','根据会员需求设计合适的训练计划','能够针对不同目标和水平制定个性化方案',8.60,10.00,1.00,'设计合理，但需要更多创新',NULL,'2025-11-05 18:33:55','2025-11-05 18:33:55'),(4,2,3,'瑜伽技能',NULL,'体位法','瑜伽体位的准确性和流畅性','体位标准，过渡流畅，呼吸配合良好',9.00,10.00,1.00,'体位标准，演示清晰',NULL,'2025-11-05 18:33:55','2025-11-05 18:33:55'),(5,2,3,'教学能力',NULL,'口令指导','瑜伽口令的准确性和引导性','口令清晰准确，能够有效引导学员',9.20,10.00,1.00,'语音优美，指导精准',NULL,'2025-11-05 18:33:55','2025-11-05 18:33:55');
/*!40000 ALTER TABLE `jeez_coach_skill_evaluation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_coach_specialty_relation`
--

DROP TABLE IF EXISTS `jeez_coach_specialty_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_coach_specialty_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coach_id` bigint NOT NULL,
  `specialty_id` tinyint NOT NULL,
  `proficiency_level` tinyint DEFAULT '1' COMMENT '熟练度：1-初级，2-中级，3-高级，4-专家',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `is_deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coach_specialty` (`coach_id`,`specialty_id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_specialty_id` (`specialty_id`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_coach_specialty_coach` FOREIGN KEY (`coach_id`) REFERENCES `jeez_coach_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_coach_specialty_specialty` FOREIGN KEY (`specialty_id`) REFERENCES `jeez_dict_coach_specialty` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练专长关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_coach_specialty_relation`
--

LOCK TABLES `jeez_coach_specialty_relation` WRITE;
/*!40000 ALTER TABLE `jeez_coach_specialty_relation` DISABLE KEYS */;
INSERT INTO `jeez_coach_specialty_relation` VALUES (14,1,1,4,'2025-12-14 13:03:47','2025-12-14 13:03:47',1,NULL,0),(15,1,2,3,'2025-12-14 13:03:47','2025-12-14 13:03:47',1,NULL,0),(16,1,4,3,'2025-12-14 13:03:47','2025-12-14 13:03:47',1,NULL,0),(17,2,3,4,'2025-12-14 13:03:47','2025-12-14 13:03:47',1,NULL,0),(18,2,7,3,'2025-12-14 13:03:47','2025-12-14 13:03:47',1,NULL,0),(19,2,6,3,'2025-12-14 13:03:47','2025-12-14 13:03:47',1,NULL,0),(20,3,2,3,'2025-12-14 13:03:47','2025-12-14 13:03:47',1,NULL,0),(21,3,5,4,'2025-12-14 13:03:47','2025-12-14 13:03:47',1,NULL,0);
/*!40000 ALTER TABLE `jeez_coach_specialty_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_course`
--

DROP TABLE IF EXISTS `jeez_course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `course_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程编码',
  `course_type_id` tinyint NOT NULL COMMENT '课程类型ID',
  `difficulty_id` tinyint NOT NULL DEFAULT '1' COMMENT '难度ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `coach_id` bigint DEFAULT NULL COMMENT '主教练ID(关联jeez_user表)',
  `max_participants` int DEFAULT '20' COMMENT '最大参与人数',
  `duration_minutes` int DEFAULT '60' COMMENT '课程时长(分钟)',
  `calorie_consumption` int DEFAULT '0' COMMENT '预计卡路里消耗',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '课程描述',
  `objectives` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '课程目标',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '参与要求',
  `equipment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '所需设备(JSON格式)',
  `benefits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '课程益处',
  `price_per_session` decimal(8,2) DEFAULT NULL COMMENT '单次课程价格',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程图片',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '演示视频URL',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `is_recommended` tinyint(1) DEFAULT '0' COMMENT '是否推荐课程',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `course_code` (`course_code`),
  KEY `idx_course_code` (`course_code`),
  KEY `idx_course_type_id` (`course_type_id`),
  KEY `idx_difficulty_id` (`difficulty_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_recommended` (`is_recommended`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `jeez_course_ibfk_1` FOREIGN KEY (`course_type_id`) REFERENCES `jeez_dict_course_type` (`id`),
  CONSTRAINT `jeez_course_ibfk_2` FOREIGN KEY (`difficulty_id`) REFERENCES `jeez_dict_course_difficulty` (`id`),
  CONSTRAINT `jeez_course_ibfk_3` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_course_ibfk_4` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_course`
--

LOCK TABLES `jeez_course` WRITE;
/*!40000 ALTER TABLE `jeez_course` DISABLE KEYS */;
INSERT INTO `jeez_course` VALUES (1,'流瑜伽初级','YOGA_FLOW_1',1,1,1,3,25,60,180,'适合初学者的流瑜伽课程，动作连贯流畅','学习基础瑜伽体式，提高身体柔韧性和平衡能力','无需基础，穿舒适运动服装','[\"瑜伽垫\", \"瑜伽砖\", \"伸展带\"]','改善体态，缓解压力，提高睡眠质量',88.00,'https://example.com/yoga1.jpg',NULL,1,1,1,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(2,'动感单车燃脂','SPIN_BURN',3,2,1,7,40,45,450,'高强度有氧运动，配合音乐节奏骑行','快速燃烧脂肪，提高心肺功能，塑造腿部线条','建议穿运动裤和运动鞋，带毛巾和水','[\"动感单车\", \"心率监测器\"]','减脂塑形，提升耐力，释放压力',68.00,'https://example.com/spin1.jpg',NULL,1,1,2,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(3,'游泳基础教学','SWIM_BASIC',4,1,1,4,15,60,300,'零基础游泳教学，学习基本泳姿和呼吸技巧','掌握基础泳姿，克服水中恐惧，建立水中安全意识','会游泳者优先安排，需自带泳衣泳帽','[\"游泳池\", \"浮板\", \"背漂\"]','学会游泳，增强体质，掌握求生技能',98.00,'https://example.com/swim1.jpg',NULL,1,0,3,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0);
/*!40000 ALTER TABLE `jeez_course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_course_booking`
--

DROP TABLE IF EXISTS `jeez_course_booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_course_booking` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `booking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预约编号',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `course_schedule_id` bigint NOT NULL COMMENT '课程排期ID',
  `booking_time` datetime NOT NULL COMMENT '预约时间',
  `booking_status` tinyint DEFAULT '1' COMMENT '预约状态(1:已预约 2:已取消 3:已完成 4:未到场)',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取消原因',
  `check_in_time` datetime DEFAULT NULL COMMENT '签到时间',
  `feedback_rating` tinyint DEFAULT NULL COMMENT '评分(1-5分)',
  `feedback_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '评价内容',
  `card_deduct_type` tinyint DEFAULT '1' COMMENT '扣卡类型(1:次卡 2:期限卡 3:现金)',
  `fee` decimal(8,2) DEFAULT '0.00' COMMENT '费用',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `booking_no` (`booking_no`),
  UNIQUE KEY `uk_member_schedule_active` (`member_id`,`course_schedule_id`,`booking_status`),
  KEY `idx_booking_no` (`booking_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_course_schedule_id` (`course_schedule_id`),
  KEY `idx_booking_time` (`booking_time`),
  KEY `idx_booking_status` (`booking_status`),
  KEY `idx_check_in_time` (`check_in_time`),
  CONSTRAINT `jeez_course_booking_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `jeez_member` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_course_booking_ibfk_2` FOREIGN KEY (`course_schedule_id`) REFERENCES `jeez_course_schedule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_course_booking`
--

LOCK TABLES `jeez_course_booking` WRITE;
/*!40000 ALTER TABLE `jeez_course_booking` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_course_booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_course_schedule`
--

DROP TABLE IF EXISTS `jeez_course_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_course_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排期ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `schedule_date` date NOT NULL COMMENT '上课日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `coach_id` bigint DEFAULT NULL COMMENT '教练ID',
  `facility_id` bigint DEFAULT NULL COMMENT '设施ID',
  `current_participants` int DEFAULT '0' COMMENT '当前参与人数',
  `max_participants` int DEFAULT '20' COMMENT '最大参与人数',
  `waiting_list_count` int DEFAULT '0' COMMENT '候补人数',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:正常 2:取消 3:满员)',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_schedule_date` (`schedule_date`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_status` (`status`),
  KEY `idx_schedule_date_time` (`schedule_date`,`start_time`),
  KEY `facility_id` (`facility_id`),
  CONSTRAINT `jeez_course_schedule_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `jeez_course` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_course_schedule_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_course_schedule_ibfk_3` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_course_schedule_ibfk_4` FOREIGN KEY (`facility_id`) REFERENCES `jeez_store_facility` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程排期表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_course_schedule`
--

LOCK TABLES `jeez_course_schedule` WRITE;
/*!40000 ALTER TABLE `jeez_course_schedule` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_course_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_data_statistics`
--

DROP TABLE IF EXISTS `jeez_data_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_data_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` bigint DEFAULT NULL COMMENT '门店ID',
  `data_date` date DEFAULT NULL COMMENT '数据日期',
  `data_type` tinyint DEFAULT NULL COMMENT '数据类型：1-销售额数据，2-退款数据，3-活跃用户数据，4-订单生产数据',
  `daily_sales` decimal(10,2) DEFAULT NULL COMMENT '日销售额（单位：万元）',
  `monthly_sales` decimal(10,2) DEFAULT NULL COMMENT '月销售额（单位：万元）',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额（单位：万元）',
  `refund_count` int DEFAULT NULL COMMENT '退款数量',
  `active_users` int DEFAULT NULL COMMENT '活跃用户数量',
  `new_users` int DEFAULT NULL COMMENT '新用户数量',
  `order_count` int DEFAULT NULL COMMENT '订单数量',
  `completed_order_count` int DEFAULT NULL COMMENT '完成订单数量',
  `order_amount` decimal(10,2) DEFAULT NULL COMMENT '订单总金额（单位：万元）',
  `status` tinyint DEFAULT '1' COMMENT '数据状态：1-有效，0-无效',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_data_date` (`data_date`),
  KEY `idx_data_type` (`data_type`),
  KEY `idx_store_date` (`store_id`,`data_date`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_data_statistics_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_data_statistics`
--

LOCK TABLES `jeez_data_statistics` WRITE;
/*!40000 ALTER TABLE `jeez_data_statistics` DISABLE KEYS */;
INSERT INTO `jeez_data_statistics` VALUES (1,1,'2025-12-01',1,1.50,45.00,0.10,2,120,15,45,43,1.48,1,'正常营业数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0),(2,1,'2025-12-02',1,1.80,46.80,0.05,1,135,18,52,50,1.78,1,'正常营业数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0),(3,1,'2025-12-03',1,2.10,48.90,0.15,3,142,20,58,55,2.08,1,'周末促销数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0),(4,2,'2025-12-01',1,1.20,36.00,0.08,1,95,12,38,37,1.18,1,'正常营业数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0),(5,2,'2025-12-02',1,1.60,37.60,0.00,0,110,16,45,45,1.60,1,'正常营业数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0);
/*!40000 ALTER TABLE `jeez_data_statistics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_device`
--

DROP TABLE IF EXISTS `jeez_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_device` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sn` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `store_id` bigint DEFAULT NULL,
  `online_status` tinyint NOT NULL DEFAULT '0',
  `face_capacity` int DEFAULT NULL,
  `used_face_count` int DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sn` (`sn`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_online_status` (`online_status`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_device_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='device table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_device`
--

LOCK TABLES `jeez_device` WRITE;
/*!40000 ALTER TABLE `jeez_device` DISABLE KEYS */;
INSERT INTO `jeez_device` VALUES (1,'SN20251225001',1,0,10000,0,1,'2026-02-26 23:46:27','2026-02-26 23:46:27',NULL,NULL),(2,'SN20251225002',1,0,10000,0,1,'2026-02-26 23:46:27','2026-02-26 23:46:27',NULL,NULL),(3,'SN20251225003',2,0,5000,0,1,'2026-02-26 23:46:27','2026-02-26 23:46:27',NULL,NULL);
/*!40000 ALTER TABLE `jeez_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_device_face_sync`
--

DROP TABLE IF EXISTS `jeez_device_face_sync`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_device_face_sync` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` bigint NOT NULL,
  `device_sn` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `synced_version` int NOT NULL DEFAULT '0',
  `sync_status` tinyint NOT NULL DEFAULT '0',
  `last_sync_time` datetime DEFAULT NULL,
  `error_reason` int DEFAULT NULL,
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `retry_count` tinyint NOT NULL DEFAULT '0',
  `enroll_id` int DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_user` (`device_id`,`user_id`),
  KEY `idx_device_sn` (`device_sn`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_sync_status` (`sync_status`),
  KEY `idx_version` (`user_id`,`synced_version`),
  KEY `idx_retry` (`retry_count`),
  CONSTRAINT `fk_device_sync_device` FOREIGN KEY (`device_id`) REFERENCES `jeez_device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_device_sync_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='device face sync status';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_device_face_sync`
--

LOCK TABLES `jeez_device_face_sync` WRITE;
/*!40000 ALTER TABLE `jeez_device_face_sync` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_device_face_sync` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_card_type`
--

DROP TABLE IF EXISTS `jeez_dict_card_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_card_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '卡类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_card_type`
--

LOCK TABLES `jeez_dict_card_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_card_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_card_type` VALUES (1,'TIME_CARD','次卡','按次数计费的会员卡',1,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(2,'DURATION_CARD','期限卡','按时间期限的会员卡',2,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(3,'UNLIMITED','无限次卡','在期限内无限次使用',3,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(4,'COURSE_CARD','课程卡','专门用于特定课程的会员卡',4,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(5,'COMBO_CARD','组合卡','多种服务组合的会员卡',5,1,'2025-11-05 18:06:35','2025-11-05 18:06:35');
/*!40000 ALTER TABLE `jeez_dict_card_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_coach_qualification_type`
--

DROP TABLE IF EXISTS `jeez_dict_coach_qualification_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_coach_qualification_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '资格类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`),
  KEY `created_by` (`created_by`),
  KEY `updated_by` (`updated_by`),
  CONSTRAINT `jeez_dict_coach_qualification_type_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_dict_coach_qualification_type_ibfk_2` FOREIGN KEY (`updated_by`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练资格类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_coach_qualification_type`
--

LOCK TABLES `jeez_dict_coach_qualification_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_coach_qualification_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_coach_qualification_type` VALUES (1,'NATIONAL','国家级认证','国家体育总局或相关部委颁发的专业资格证书',1,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(2,'INTERNATIONAL','国际认证','国际权威健身组织颁发的专业证书',2,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(3,'COMMERCIAL','商业认证','知名商业健身机构的认证证书',3,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(4,'ACADEMIC','学历证书','体育院校相关专业的学历证明',4,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(5,'EXPERIENCE','经验认证','基于工作经验的内部等级认证',5,1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1);
/*!40000 ALTER TABLE `jeez_dict_coach_qualification_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_coach_skill_level`
--

DROP TABLE IF EXISTS `jeez_dict_coach_skill_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_coach_skill_level` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '技能等级ID',
  `level_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '等级编码',
  `level_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '等级名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '等级描述',
  `level_order` int DEFAULT '0' COMMENT '等级排序',
  `min_experience_years` int DEFAULT '0' COMMENT '最低经验年限',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '等级要求(JSON格式)',
  `benefits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '等级权益(JSON格式)',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `level_code` (`level_code`),
  KEY `created_by` (`created_by`),
  KEY `updated_by` (`updated_by`),
  CONSTRAINT `jeez_dict_coach_skill_level_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_dict_coach_skill_level_ibfk_2` FOREIGN KEY (`updated_by`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练技能等级字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_coach_skill_level`
--

LOCK TABLES `jeez_dict_coach_skill_level` WRITE;
/*!40000 ALTER TABLE `jeez_dict_coach_skill_level` DISABLE KEYS */;
INSERT INTO `jeez_dict_coach_skill_level` VALUES (1,'TRAINEE','实习教练','刚开始执教，需要在指导教练监督下工作',1,0,'{\"min_age\": 18, \"basic_cert\": true}','{\"max_daily_sessions\": 3, \"session_price_range\": [50, 150]}',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(2,'JUNIOR','初级教练','具备基本教学能力，可独立授课',2,1,'{\"min_age\": 20, \"experience_hours\": 500}','{\"max_daily_sessions\": 6, \"session_price_range\": [100, 200]}',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(3,'INTERMEDIATE','中级教练','教学经验丰富，可处理复杂需求',3,3,'{\"min_age\": 22, \"experience_hours\": 2000}','{\"max_daily_sessions\": 8, \"session_price_range\": [150, 300]}',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(4,'SENIOR','高级教练','专业能力强，可培训其他教练',4,5,'{\"min_age\": 25, \"experience_hours\": 5000}','{\"max_daily_sessions\": 10, \"session_price_range\": [200, 400]}',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(5,'EXPERT','专家教练','行业专家，可参与课程研发',5,8,'{\"min_age\": 28, \"experience_hours\": 8000}','{\"max_daily_sessions\": 12, \"session_price_range\": [300, 600]}',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1),(6,'MASTER','大师级教练','顶级专家，可担任考官和培训师',6,10,'{\"min_age\": 30, \"experience_hours\": 12000}','{\"max_daily_sessions\": 15, \"session_price_range\": [400, 800]}',1,'2025-11-05 18:33:55','2025-11-05 18:33:55',1,1);
/*!40000 ALTER TABLE `jeez_dict_coach_skill_level` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_coach_specialty`
--

DROP TABLE IF EXISTS `jeez_dict_coach_specialty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_coach_specialty` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '专业领域ID',
  `specialty_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '领域编码',
  `specialty_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '领域名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `icon_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标URL',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `specialty_code` (`specialty_code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练专业领域字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_coach_specialty`
--

LOCK TABLES `jeez_dict_coach_specialty` WRITE;
/*!40000 ALTER TABLE `jeez_dict_coach_specialty` DISABLE KEYS */;
INSERT INTO `jeez_dict_coach_specialty` VALUES (1,'STRENGTH','力量训练','健身','包括器械训练、自重训练等力量提升',NULL,1,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(2,'CARDIO','有氧训练','健身','跑步、游泳、单车等有氧运动',NULL,2,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(3,'YOGA','瑜伽','身心','各种流派瑜伽教学',NULL,3,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(4,'PILATES','普拉提','身心','核心力量与体态矫正',NULL,4,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(5,'SWIMMING','游泳','水上','各种泳姿教学和水上安全',NULL,5,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(6,'MARTIAL_ARTS','武术/格斗','对抗','拳击、搏击、武术等',NULL,6,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(7,'FUNCTIONAL','功能性训练','健身','提升日常生活功能的训练',NULL,7,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(8,'REHABILITATION','康复训练','康复','运动损伤康复和体态矫正',NULL,8,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(9,'NUTRITION','营养指导','咨询','饮食计划和营养咨询',NULL,9,1,'2025-12-12 09:56:02','2025-12-12 09:56:02'),(10,'WEIGHT_LOSS','减脂塑形','健身','科学减脂和身材塑造',NULL,10,1,'2025-12-12 09:56:02','2025-12-12 09:56:02');
/*!40000 ALTER TABLE `jeez_dict_coach_specialty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_course_difficulty`
--

DROP TABLE IF EXISTS `jeez_dict_course_difficulty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_course_difficulty` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '难度ID',
  `difficulty_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '难度编码',
  `difficulty_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '难度名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `level_number` int DEFAULT '1' COMMENT '难度等级(1-10)',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `difficulty_code` (`difficulty_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程难度字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_course_difficulty`
--

LOCK TABLES `jeez_dict_course_difficulty` WRITE;
/*!40000 ALTER TABLE `jeez_dict_course_difficulty` DISABLE KEYS */;
INSERT INTO `jeez_dict_course_difficulty` VALUES (1,'BEGINNER','初级','适合零基础或新手',1,1,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(2,'ELEMENTARY','基础','适合有一定基础的学员',3,2,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(3,'INTERMEDIATE','中级','适合有经验的学员',5,3,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(4,'ADVANCED','高级','适合有丰富经验的学员',7,4,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(5,'EXPERT','专家','适合专业水平学员',10,5,1,'2025-11-05 18:06:35','2025-11-05 18:06:35');
/*!40000 ALTER TABLE `jeez_dict_course_difficulty` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_course_type`
--

DROP TABLE IF EXISTS `jeez_dict_course_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_course_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '课程类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_course_type`
--

LOCK TABLES `jeez_dict_course_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_course_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_course_type` VALUES (1,'YOGA','瑜伽课程','各种瑜伽练习课程',1,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(2,'PILATES','普拉提','普拉提训练课程',2,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(3,'SPINNING','动感单车','室内单车训练课程',3,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(4,'SWIMMING','游泳课程','游泳教学和训练',4,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(5,'DANCE','舞蹈课程','各种舞蹈健身课程',5,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(6,'MARTIAL_ARTS','武术课程','武术、拳击等课程',6,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(7,'STRENGTH','力量训练','器械力量训练课程',7,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(8,'CARDIO','有氧训练','有氧运动训练课程',8,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(9,'GROUP_CLASS','团体课程','多人参与的团体课程',9,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(10,'PERSONAL_TRAINING','私教课程','一对一私人教练课程',10,1,'2025-11-05 18:06:35','2025-11-05 18:06:35');
/*!40000 ALTER TABLE `jeez_dict_course_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_employee_type`
--

DROP TABLE IF EXISTS `jeez_dict_employee_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_employee_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '员工类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '员工类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_employee_type`
--

LOCK TABLES `jeez_dict_employee_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_employee_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_employee_type` VALUES (1,'MANAGER','店长','门店负责人，全面管理门店运营',1,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(2,'COACH','教练','健身教练，提供专业健身指导',2,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(3,'STAFF','普通员工','前台、清洁等后勤支持人员',3,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(4,'TRAINER','私人教练','一对一专业训练指导',4,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(5,'INSTRUCTOR','课程讲师','团体课程专业讲师',5,1,'2025-11-05 17:54:03','2025-11-05 17:54:03');
/*!40000 ALTER TABLE `jeez_dict_employee_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_facility_type`
--

DROP TABLE IF EXISTS `jeez_dict_facility_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_facility_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '设施类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设施类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设施类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设施类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_facility_type`
--

LOCK TABLES `jeez_dict_facility_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_facility_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_facility_type` VALUES (1,'CARDIO','有氧器械区','跑步机、椭圆机、动感单车等有氧运动设备',1,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(2,'STRENGTH','力量训练区','哑铃、杠铃、复合力量器械等',2,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(3,'POOL','游泳池','恒温泳池、儿童池等',3,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(4,'YOGA','瑜伽室','专业瑜伽练习场所',4,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(5,'PILATES','普拉提室','普拉提器械训练室',5,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(6,'SPINNING','动感单车房','室内单车训练室',6,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(7,'LOCKER','更衣室','男女更衣室、储物柜等',7,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(8,'SHOWER','淋浴间','洗浴设施',8,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(9,'LOUNGE','休息区','客户休息、饮料吧台等',9,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(10,'CLASSROOM','团体课室','舞蹈、瑜伽等团体课程专用室',10,1,'2025-11-05 17:54:03','2025-11-05 17:54:03');
/*!40000 ALTER TABLE `jeez_dict_facility_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_member_status`
--

DROP TABLE IF EXISTS `jeez_dict_member_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_member_status` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '状态ID',
  `status_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态编码',
  `status_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `status_code` (`status_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员状态字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_member_status`
--

LOCK TABLES `jeez_dict_member_status` WRITE;
/*!40000 ALTER TABLE `jeez_dict_member_status` DISABLE KEYS */;
INSERT INTO `jeez_dict_member_status` VALUES (1,'ACTIVE','正常','会员正常在籍，可以正常使用服务',1,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(2,'EXPIRED','过期','会员卡已过期，需要续费',2,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(3,'FROZEN','冻结','会员账户被冻结，暂时无法使用服务',3,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(4,'SUSPENDED','停卡','会员主动申请暂停会员卡',4,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(5,'TERMINATED','退会','会员已退会，不再享受服务',5,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(6,'PENDING','待激活','新注册会员，等待激活',6,1,'2025-11-05 18:06:35','2025-11-05 18:06:35');
/*!40000 ALTER TABLE `jeez_dict_member_status` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_member_type`
--

DROP TABLE IF EXISTS `jeez_dict_member_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_member_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '会员类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_member_type`
--

LOCK TABLES `jeez_dict_member_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_member_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_member_type` VALUES (1,'VIP','VIP会员','高端VIP会员，享受专属服务',1,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(2,'PREMIUM','高级会员','高级会员，享受优质服务',2,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(3,'STANDARD','标准会员','标准会员，享受基础服务',3,1,'2025-11-05 18:06:35','2025-11-05 18:06:35'),(4,'TRIAL','体验会员','体验会员，短期体验',4,1,'2025-11-05 18:06:35','2025-11-05 18:06:35');
/*!40000 ALTER TABLE `jeez_dict_member_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_position`
--

DROP TABLE IF EXISTS `jeez_dict_position`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_position` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '职位ID',
  `position_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '职位编码',
  `position_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '职位名称',
  `employee_type_id` tinyint NOT NULL COMMENT '员工类型ID',
  `level_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职级编码',
  `level_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职级名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职位描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `position_code` (`position_code`),
  KEY `employee_type_id` (`employee_type_id`),
  CONSTRAINT `jeez_dict_position_ibfk_1` FOREIGN KEY (`employee_type_id`) REFERENCES `jeez_dict_employee_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_position`
--

LOCK TABLES `jeez_dict_position` WRITE;
/*!40000 ALTER TABLE `jeez_dict_position` DISABLE KEYS */;
INSERT INTO `jeez_dict_position` VALUES (1,'STORE_MANAGER','店长',1,'L1','一级店长','负责整个门店的运营管理',1,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(2,'SENIOR_MANAGER','高级店长',1,'L2','二级店长','管理多个门店的高级店长',2,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(3,'HEAD_COACH','总教练',2,'S1','一级教练','资深健身教练，负责教练团队管理',1,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(4,'SENIOR_COACH','高级教练',2,'S2','二级教练','经验丰富的健身教练',2,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(5,'COACH','教练',2,'S3','三级教练','普通健身教练',3,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(6,'YOGA_INSTRUCTOR','瑜伽教练',5,'Y1','一级瑜伽师','专业瑜伽课程讲师',1,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(7,'SWIMMING_COACH','游泳教练',2,'SW1','一级游泳教练','专业游泳教学指导',4,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(8,'PERSONAL_TRAINER','私人教练',4,'PT1','一级私教','一对一专业训练指导',5,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(9,'FRONT_DESK','前台接待',3,'F1','一级前台','负责客户接待和基本事务处理',6,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(10,'CLEANER','保洁员',3,'C1','一级保洁','负责门店清洁卫生工作',7,1,'2025-11-05 17:54:03','2025-11-05 17:54:03');
/*!40000 ALTER TABLE `jeez_dict_position` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_pricing_strategy_type`
--

DROP TABLE IF EXISTS `jeez_dict_pricing_strategy_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_pricing_strategy_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '策略类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格策略类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_pricing_strategy_type`
--

LOCK TABLES `jeez_dict_pricing_strategy_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_pricing_strategy_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_pricing_strategy_type` VALUES (1,'STANDARD','标准定价','统一标准价格，适用于所有门店',1,1,'2025-11-05 18:28:16','2025-11-05 18:28:16'),(2,'LOCATION_BASED','地域定价','根据门店地理位置制定差异化价格',2,1,'2025-11-05 18:28:16','2025-11-05 18:28:16'),(3,'TIERED','分级定价','根据服务等级和质量分级定价',3,1,'2025-11-05 18:28:16','2025-11-05 18:28:16'),(4,'DYNAMIC','动态定价','根据需求和时间动态调整价格',4,1,'2025-11-05 18:28:16','2025-11-05 18:28:16'),(5,'MEMBERSHIP','会员定价','基于会员等级的差异化定价',5,1,'2025-11-05 18:28:16','2025-11-05 18:28:16'),(6,'PROMOTIONAL','促销定价','特定时期的促销价格策略',6,1,'2025-11-05 18:28:16','2025-11-05 18:28:16');
/*!40000 ALTER TABLE `jeez_dict_pricing_strategy_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_redemption_code_type`
--

DROP TABLE IF EXISTS `jeez_dict_redemption_code_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_redemption_code_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '兑换码类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换码类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_redemption_code_type`
--

LOCK TABLES `jeez_dict_redemption_code_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_redemption_code_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_redemption_code_type` VALUES (1,'TRIAL_PASS','体验通行证','新用户体验健身房设施的免费通行证',1,1,'2025-11-05 18:24:26','2025-11-05 18:24:26'),(2,'FREE_CLASS','免费课程','指定课程的免费体验券',2,1,'2025-11-05 18:24:26','2025-11-05 18:24:26'),(3,'GUEST_PASS','游客通行证','临时游客身份，可使用基础服务',3,1,'2025-11-05 18:24:26','2025-11-05 18:24:26'),(4,'PROMOTIONAL','促销兑换码','各类营销活动兑换码',4,1,'2025-11-05 18:24:26','2025-11-05 18:24:26'),(5,'MEMBERSHIP_UPGRADE','会员升级券','会员等级升级凭证',5,1,'2025-11-05 18:24:26','2025-11-05 18:24:26'),(6,'SERVICE_EXTENSION','服务延期券','会员服务期限延长凭证',6,1,'2025-11-05 18:24:26','2025-11-05 18:24:26');
/*!40000 ALTER TABLE `jeez_dict_redemption_code_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_sales_channel`
--

DROP TABLE IF EXISTS `jeez_dict_sales_channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_sales_channel` (
  `id` int NOT NULL AUTO_INCREMENT,
  `channel_code` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道编码(WECHAT_MINI / MEITUAN / DOUYIN 等)',
  `channel_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '渠道名称',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1启用 0停用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_code` (`channel_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售渠道字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_sales_channel`
--

LOCK TABLES `jeez_dict_sales_channel` WRITE;
/*!40000 ALTER TABLE `jeez_dict_sales_channel` DISABLE KEYS */;
INSERT INTO `jeez_dict_sales_channel` VALUES (1,'WECHAT_MINI','WeChat Mini Program',1,1,'2026-02-26 23:46:18','2026-02-26 23:46:18'),(2,'MEITUAN','Meituan',2,1,'2026-02-26 23:46:18','2026-02-26 23:46:18'),(3,'DOUYIN','Douyin',3,1,'2026-02-26 23:46:18','2026-02-26 23:46:18'),(4,'OFFLINE','Offline Store',4,1,'2026-02-26 23:46:18','2026-02-26 23:46:18');
/*!40000 ALTER TABLE `jeez_dict_sales_channel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_store_group`
--

DROP TABLE IF EXISTS `jeez_dict_store_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_store_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `group_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组编码',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分组名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父分组ID(支持多级分组)',
  `group_type` tinyint DEFAULT '1' COMMENT '分组类型(1:区域分组 2:业态分组 3:等级分组 4:自定义分组)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分组描述',
  `icon_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分组图标URL',
  `color_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分组颜色代码',
  `manager_id` bigint DEFAULT NULL COMMENT '分组负责人ID',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分组联系电话',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_code` (`group_code`),
  KEY `idx_group_code` (`group_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_group_type` (`group_type`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_is_deleted` (`is_deleted`),
  KEY `fk_store_group_manager` (`manager_id`),
  CONSTRAINT `fk_store_group_manager` FOREIGN KEY (`manager_id`) REFERENCES `jeez_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店分组字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_store_group`
--

LOCK TABLES `jeez_dict_store_group` WRITE;
/*!40000 ALTER TABLE `jeez_dict_store_group` DISABLE KEYS */;
INSERT INTO `jeez_dict_store_group` VALUES (1,'NORTH_CHINA','华北区域',NULL,1,'华北地区所有门店（北京、天津、河北等）',NULL,'#E74C3C',NULL,NULL,1,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(2,'EAST_CHINA','华东区域',NULL,1,'华东地区所有门店（上海、江苏、浙江等）',NULL,'#3498DB',NULL,NULL,2,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(3,'SOUTH_CHINA','华南区域',NULL,1,'华南地区所有门店（广东、广西、海南等）',NULL,'#2ECC71',NULL,NULL,3,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(4,'CENTRAL_CHINA','华中区域',NULL,1,'华中地区所有门店（湖北、湖南、河南等）',NULL,'#F39C12',NULL,NULL,4,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(5,'BEIJING','北京区域',1,1,'北京地区所有门店',NULL,'#E74C3C',NULL,NULL,1,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(6,'TIANJIN','天津区域',1,1,'天津地区所有门店',NULL,'#E74C3C',NULL,NULL,2,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(7,'SHANGHAI','上海区域',2,1,'上海地区所有门店',NULL,'#3498DB',NULL,NULL,1,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(8,'HANGZHOU','杭州区域',2,1,'杭州地区所有门店',NULL,'#3498DB',NULL,NULL,2,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(9,'GUANGZHOU','广州区域',3,1,'广州地区所有门店',NULL,'#2ECC71',NULL,NULL,1,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(10,'SHENZHEN','深圳区域',3,1,'深圳地区所有门店',NULL,'#2ECC71',NULL,NULL,2,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(11,'FLAGSHIP_STORES','旗舰店群',NULL,2,'所有旗舰店',NULL,'#9B59B6',NULL,NULL,10,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(12,'STANDARD_STORES','标准店群',NULL,2,'所有标准店',NULL,'#1ABC9C',NULL,NULL,11,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0),(13,'COMMUNITY_STORES','社区店群',NULL,2,'所有社区店',NULL,'#34495E',NULL,NULL,12,1,'2025-12-12 09:47:03','2025-12-12 09:47:03',1,1,0);
/*!40000 ALTER TABLE `jeez_dict_store_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_dict_store_type`
--

DROP TABLE IF EXISTS `jeez_dict_store_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_dict_store_type` (
  `id` tinyint NOT NULL AUTO_INCREMENT COMMENT '类型ID',
  `type_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型编码',
  `type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '类型名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_code` (`type_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店类型字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_dict_store_type`
--

LOCK TABLES `jeez_dict_store_type` WRITE;
/*!40000 ALTER TABLE `jeez_dict_store_type` DISABLE KEYS */;
INSERT INTO `jeez_dict_store_type` VALUES (1,'FLAGSHIP','旗舰店','大型综合性健身中心，设施齐全，服务全面',1,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(2,'STANDARD','标准店','中等规模健身房，主要服务区域客户',2,1,'2025-11-05 17:54:03','2025-11-05 17:54:03'),(3,'COMMUNITY','社区店','小型健身房，服务社区居民',3,1,'2025-11-05 17:54:03','2025-11-05 17:54:03');
/*!40000 ALTER TABLE `jeez_dict_store_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_face_sync_task`
--

DROP TABLE IF EXISTS `jeez_face_sync_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_face_sync_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `face_version` int NOT NULL,
  `total_devices` int NOT NULL DEFAULT '0',
  `success_devices` int NOT NULL DEFAULT '0',
  `failed_devices` int NOT NULL DEFAULT '0',
  `pending_devices` int NOT NULL DEFAULT '0',
  `task_status` tinyint NOT NULL DEFAULT '0',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT NULL,
  `duration_ms` int DEFAULT NULL,
  `error_summary` varchar(1500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`task_status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_face_version` (`face_version`),
  CONSTRAINT `fk_sync_task_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='face sync task';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_face_sync_task`
--

LOCK TABLES `jeez_face_sync_task` WRITE;
/*!40000 ALTER TABLE `jeez_face_sync_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_face_sync_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_face_sync_task_detail`
--

DROP TABLE IF EXISTS `jeez_face_sync_task_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_face_sync_task_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_id` bigint NOT NULL,
  `device_id` bigint NOT NULL,
  `device_sn` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `face_version` int NOT NULL,
  `sync_status` tinyint NOT NULL DEFAULT '0',
  `send_time` datetime DEFAULT NULL,
  `response_time` datetime DEFAULT NULL,
  `duration_ms` int DEFAULT NULL,
  `error_reason` int DEFAULT NULL,
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `retry_count` tinyint NOT NULL DEFAULT '0',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_device_sn` (`device_sn`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_sync_status` (`sync_status`),
  KEY `fk_sync_detail_device` (`device_id`),
  CONSTRAINT `fk_sync_detail_device` FOREIGN KEY (`device_id`) REFERENCES `jeez_device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sync_detail_task` FOREIGN KEY (`task_id`) REFERENCES `jeez_face_sync_task` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sync_detail_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='face sync task detail';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_face_sync_task_detail`
--

LOCK TABLES `jeez_face_sync_task_detail` WRITE;
/*!40000 ALTER TABLE `jeez_face_sync_task_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_face_sync_task_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_guest_access_log`
--

DROP TABLE IF EXISTS `jeez_guest_access_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_guest_access_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID(关联jeez_user表)',
  `permission_id` bigint DEFAULT NULL COMMENT '权限ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `access_type` tinyint NOT NULL COMMENT '访问类型(1:进入 2:离开 3:设备使用 4:服务使用)',
  `access_time` datetime NOT NULL COMMENT '访问时间',
  `access_duration` int DEFAULT NULL COMMENT '访问时长(分钟)',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源类型',
  `resource_id` bigint DEFAULT NULL COMMENT '资源ID',
  `device_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备信息',
  `location_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '位置信息',
  `access_status` tinyint DEFAULT '1' COMMENT '访问状态(1:成功 2:失败 3:异常)',
  `failure_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `staff_id` bigint DEFAULT NULL COMMENT '接待员工ID',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_permission_id` (`permission_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_access_time` (`access_time`),
  KEY `idx_access_type` (`access_type`),
  KEY `idx_access_status` (`access_status`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `staff_id` (`staff_id`),
  CONSTRAINT `jeez_guest_access_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_guest_access_log_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `jeez_guest_permission` (`id`),
  CONSTRAINT `jeez_guest_access_log_ibfk_3` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_guest_access_log_ibfk_4` FOREIGN KEY (`staff_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游客访问记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_guest_access_log`
--

LOCK TABLES `jeez_guest_access_log` WRITE;
/*!40000 ALTER TABLE `jeez_guest_access_log` DISABLE KEYS */;
INSERT INTO `jeez_guest_access_log` VALUES (1,4,1,1,1,'2023-10-06 07:30:00',NULL,'facility',1,'人脸识别门禁','健身房主入口',1,NULL,5,'游客首次访问','2025-11-05 18:24:26'),(2,4,1,1,2,'2023-10-06 09:15:00',105,'facility',1,'人脸识别门禁','健身房主入口',1,NULL,5,'游客离开，访问时长1小时45分钟','2025-11-05 18:24:26'),(3,5,2,1,1,'2023-10-12 18:20:00',NULL,'facility',1,'二维码扫码','健身房前台',1,NULL,1,'游客通行证使用','2025-11-05 18:24:26'),(4,6,3,1,3,'2023-10-15 19:00:00',60,'course',1,'课程签到系统','瑜伽室A',1,NULL,3,'参加体验瑜伽课程','2025-11-05 18:24:26');
/*!40000 ALTER TABLE `jeez_guest_access_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_guest_permission`
--

DROP TABLE IF EXISTS `jeez_guest_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_guest_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `user_id` bigint NOT NULL COMMENT '用户ID(关联jeez_user表)',
  `permission_type` tinyint NOT NULL COMMENT '权限类型(1:基础设施 2:特定区域 3:时间段限制 4:设备使用)',
  `resource_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型',
  `resource_id` bigint DEFAULT NULL COMMENT '资源ID',
  `permission_level` tinyint DEFAULT '1' COMMENT '权限级别(1:只读 2:基础使用 3:高级使用)',
  `access_count` int DEFAULT '0' COMMENT '访问次数',
  `max_access_count` int DEFAULT NULL COMMENT '最大访问次数',
  `last_access_time` datetime DEFAULT NULL COMMENT '最后访问时间',
  `access_duration_minutes` int DEFAULT NULL COMMENT '单次访问时长限制(分钟)',
  `total_access_duration` int DEFAULT '0' COMMENT '累计访问时长(分钟)',
  `start_time` time DEFAULT NULL COMMENT '每日开始时间',
  `end_time` time DEFAULT NULL COMMENT '每日结束时间',
  `allowed_days` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '允许访问日期(1,2,3,4,5表示周一到周五)',
  `granted_by` bigint DEFAULT NULL COMMENT '授权人ID',
  `grant_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '授权原因',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期',
  `is_permanent` tinyint(1) DEFAULT '0' COMMENT '是否永久有效',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:有效 2:已暂停 3:已过期 4:已撤销)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_permission_type` (`permission_type`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_status` (`status`),
  KEY `idx_effective_date` (`effective_date`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_last_access_time` (`last_access_time`),
  KEY `granted_by` (`granted_by`),
  CONSTRAINT `jeez_guest_permission_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_guest_permission_ibfk_2` FOREIGN KEY (`granted_by`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='游客权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_guest_permission`
--

LOCK TABLES `jeez_guest_permission` WRITE;
/*!40000 ALTER TABLE `jeez_guest_permission` DISABLE KEYS */;
INSERT INTO `jeez_guest_permission` VALUES (1,4,1,'facility',1,2,0,7,NULL,120,0,'06:00:00','22:00:00','1,2,3,4,5',1,'体验卡兑换','2023-10-05','2023-10-12',0,1,'2025-11-05 18:24:26','2025-11-05 18:24:26',1,1,0),(2,5,2,'facility',1,2,0,1,NULL,90,0,'06:00:00','22:00:00',NULL,1,'游客通行证','2023-10-12','2023-10-12',0,3,'2025-11-05 18:24:26','2025-11-05 18:24:26',1,1,0),(3,6,3,'course',1,1,0,2,NULL,60,0,'07:00:00','21:00:00','1,2,3,4,5',3,'新用户体验','2023-10-15','2023-10-22',0,1,'2025-11-05 18:24:26','2025-11-05 18:24:26',3,3,0);
/*!40000 ALTER TABLE `jeez_guest_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_login_log`
--

DROP TABLE IF EXISTS `jeez_login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_login_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `login_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录类型',
  `identifier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录标识（手机号、邮箱、第三方平台等）',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '用户代理',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '登录状态 0-失败 1-成功',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_login_type` (`login_type`),
  KEY `idx_identifier` (`identifier`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_login_log_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_login_log`
--

LOCK TABLES `jeez_login_log` WRITE;
/*!40000 ALTER TABLE `jeez_login_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_login_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_member`
--

DROP TABLE IF EXISTS `jeez_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `user_id` bigint NOT NULL COMMENT '用户ID(关联jeez_user表)',
  `member_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会员编号',
  `member_type_id` tinyint NOT NULL DEFAULT '3' COMMENT '会员类型ID',
  `store_id` bigint NOT NULL COMMENT '所属门店ID',
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '身份证号',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0-未知 1-男 2-女',
  `height` decimal(5,2) DEFAULT NULL COMMENT '身高(cm)',
  `weight` decimal(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `blood_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '血型',
  `emergency_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '紧急联系电话',
  `health_status` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '健康状况说明',
  `fitness_goal` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '健身目标',
  `join_date` date NOT NULL COMMENT '入会日期',
  `expiry_date` date DEFAULT NULL COMMENT '到期日期',
  `total_visits` int DEFAULT '0' COMMENT '累计到店次数',
  `last_visit_time` datetime DEFAULT NULL COMMENT '最后到店时间',
  `member_status_id` tinyint DEFAULT '1' COMMENT '会员状态ID',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `member_no` (`member_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_member_no` (`member_no`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_member_type_id` (`member_type_id`),
  KEY `idx_member_status_id` (`member_status_id`),
  KEY `idx_join_date` (`join_date`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_real_name` (`real_name`),
  CONSTRAINT `jeez_member_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_member_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_member_ibfk_3` FOREIGN KEY (`member_type_id`) REFERENCES `jeez_dict_member_type` (`id`),
  CONSTRAINT `jeez_member_ibfk_4` FOREIGN KEY (`member_status_id`) REFERENCES `jeez_dict_member_status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_member`
--

LOCK TABLES `jeez_member` WRITE;
/*!40000 ALTER TABLE `jeez_member` DISABLE KEYS */;
INSERT INTO `jeez_member` VALUES (1,3,'M2025010001',2,1,'王会员','110101199001011234','1990-01-01',2,165.50,55.20,'A','王小明','13900139001','身体健康，无运动禁忌','减脂塑形，提高心肺功能','2024-01-15','2025-01-15',45,'2025-10-23 19:30:00',1,'活跃会员，经常参加瑜伽课程','2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(2,5,'M2025010002',3,1,'李健身','110101199202025678','1992-02-02',1,178.00,75.80,'O','李大明','13900139002','健康良好，有健身基础','增肌减脂，提高体能','2024-03-20','2025-03-20',68,'2025-10-23 18:00:00',1,'力量训练爱好者','2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(3,6,'M2025010003',1,2,'张瑜伽','110101198803033456','1988-03-03',2,168.00,52.30,'B','张小花','13900139003','身体健康，柔韧性好','提升瑜伽技巧，改善体态','2024-02-10','2025-02-10',32,'2025-10-22 07:30:00',1,'瑜伽达人，经常参加早课','2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0);
/*!40000 ALTER TABLE `jeez_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_member_card`
--

DROP TABLE IF EXISTS `jeez_member_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_member_card` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会员卡ID',
  `card_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡号',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `card_type_id` bigint NOT NULL COMMENT '卡种ID',
  `store_id` bigint NOT NULL COMMENT '发卡门店ID',
  `purchase_date` datetime NOT NULL COMMENT '购卡日期',
  `activate_date` datetime DEFAULT NULL COMMENT '激活日期',
  `start_date` date DEFAULT NULL COMMENT '开始生效日期',
  `expiry_date` date DEFAULT NULL COMMENT '到期日期',
  `remaining_times` int DEFAULT NULL COMMENT '剩余次数',
  `used_times` int DEFAULT '0' COMMENT '已使用次数',
  `total_times` int DEFAULT NULL COMMENT '总次数',
  `freeze_start_date` date DEFAULT NULL COMMENT '冻结开始日期',
  `freeze_end_date` date DEFAULT NULL COMMENT '冻结结束日期',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:正常 2:冻结 3:过期 4:退卡)',
  `price` decimal(10,2) DEFAULT NULL COMMENT '实付金额',
  `deposit` decimal(8,2) DEFAULT '0.00' COMMENT '押金',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `source_order_no` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'source card purchase order number',
  `source_order_seq` int DEFAULT NULL COMMENT 'per-order issuance sequence, starts from 1',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `card_no` (`card_no`),
  UNIQUE KEY `uk_source_order_seq` (`source_order_no`,`source_order_seq`),
  KEY `idx_card_no` (`card_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_card_type_id` (`card_type_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_purchase_date` (`purchase_date`),
  CONSTRAINT `jeez_member_card_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `jeez_member` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_member_card_ibfk_2` FOREIGN KEY (`card_type_id`) REFERENCES `jeez_membership_card_type` (`id`),
  CONSTRAINT `jeez_member_card_ibfk_3` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_member_card`
--

LOCK TABLES `jeez_member_card` WRITE;
/*!40000 ALTER TABLE `jeez_member_card` DISABLE KEYS */;
INSERT INTO `jeez_member_card` VALUES (1,'C2025010001',1,1,1,'2024-01-15 10:30:00','2024-01-15 10:35:00','2024-01-15','2024-07-15',3,7,10,NULL,NULL,1,880.00,0.00,'瑜伽次卡，还剩3次',NULL,NULL,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(2,'C2025010002',2,2,1,'2024-03-20 14:20:00','2024-03-20 14:25:00','2024-03-20','2025-03-20',NULL,245,NULL,NULL,NULL,1,2880.00,200.00,'健身年卡会员',NULL,NULL,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(3,'C2025010003',3,3,1,'2024-02-10 09:15:00','2024-02-10 09:20:00','2024-02-10','2024-03-10',NULL,28,NULL,NULL,NULL,3,480.00,100.00,'游泳月卡已过期',NULL,NULL,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(4,'C2025010004',2,4,1,'2024-06-15 11:00:00','2024-06-15 11:05:00','2024-06-15','2024-09-15',2,10,12,NULL,NULL,1,3600.00,0.00,'私教课程包，还剩2节',NULL,NULL,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0);
/*!40000 ALTER TABLE `jeez_member_card` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_member_coach_booking`
--

DROP TABLE IF EXISTS `jeez_member_coach_booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_member_coach_booking` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '约课ID',
  `booking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '约课编号',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `available_time_id` bigint NOT NULL COMMENT '可用时间段ID',
  `booking_date` date NOT NULL COMMENT '约课日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `booking_type` tinyint DEFAULT '1' COMMENT '约课类型(1:一对一私教 2:小班课 3:体验课)',
  `training_goal` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '训练目标',
  `fitness_level` tinyint DEFAULT '1' COMMENT '健身水平(1:新手 2:初级 3:中级 4:高级)',
  `health_conditions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '健康状况说明',
  `booking_status` tinyint DEFAULT '1' COMMENT '约课状态(1:已预约 2:已确认 3:已完成 4:会员取消 5:教练取消 6:未到场)',
  `booking_time` datetime NOT NULL COMMENT '预约时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取消原因',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `fee` decimal(8,2) DEFAULT '0.00' COMMENT '课程费用',
  `payment_status` tinyint DEFAULT '1' COMMENT '支付状态(1:未支付 2:已支付 3:已退款)',
  `payment_method` tinyint DEFAULT NULL COMMENT '支付方式(1:余额 2:微信 3:支付宝 4:银行卡 5:现金)',
  `card_deduct_type` tinyint DEFAULT NULL COMMENT '扣卡类型(1:次卡 2:期限卡 3:私教包 4:现金)',
  `member_card_id` bigint DEFAULT NULL COMMENT '使用的会员卡ID',
  `feedback_rating` tinyint DEFAULT NULL COMMENT '会员评分(1-5分)',
  `feedback_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '会员评价',
  `coach_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '教练备注',
  `training_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '课程总结',
  `next_plan` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下次训练计划',
  `reminder_sent` tinyint DEFAULT '0' COMMENT '是否已发送提醒(0:否 1:是)',
  `reminder_time` datetime DEFAULT NULL COMMENT '提醒发送时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `booking_no` (`booking_no`),
  KEY `idx_booking_no` (`booking_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_available_time_id` (`available_time_id`),
  KEY `idx_booking_date` (`booking_date`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_booking_status` (`booking_status`),
  KEY `idx_booking_time` (`booking_time`),
  KEY `idx_date_time` (`booking_date`,`start_time`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `member_card_id` (`member_card_id`),
  CONSTRAINT `jeez_member_coach_booking_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `jeez_member` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_member_coach_booking_ibfk_2` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_member_coach_booking_ibfk_3` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_member_coach_booking_ibfk_4` FOREIGN KEY (`available_time_id`) REFERENCES `jeez_coach_available_time` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_member_coach_booking_ibfk_5` FOREIGN KEY (`member_card_id`) REFERENCES `jeez_member_card` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员约课时间段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_member_coach_booking`
--

LOCK TABLES `jeez_member_coach_booking` WRITE;
/*!40000 ALTER TABLE `jeez_member_coach_booking` DISABLE KEYS */;
INSERT INTO `jeez_member_coach_booking` VALUES (1,'CB2025110601',2,2,1,1,'2025-11-06','09:00:00','10:00:00',1,'胸肌训练，增强力量',1,NULL,1,'2025-11-05 16:30:00',NULL,NULL,NULL,NULL,NULL,300.00,2,NULL,3,4,NULL,NULL,NULL,NULL,NULL,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(2,'CB2025110602',1,3,1,4,'2025-11-06','07:00:00','08:00:00',2,'晨练瑜伽，放松身心',1,NULL,2,'2025-11-05 18:20:00',NULL,NULL,NULL,NULL,NULL,88.00,2,NULL,1,1,NULL,NULL,NULL,NULL,NULL,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',1,1,0),(3,'CB2025110603',5,4,1,6,'2025-11-06','06:00:00','07:00:00',1,'自由泳技术提升',1,NULL,1,'2025-11-05 20:15:00',NULL,NULL,NULL,NULL,NULL,280.00,1,NULL,1,5,NULL,NULL,NULL,NULL,NULL,0,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',5,5,0);
/*!40000 ALTER TABLE `jeez_member_coach_booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_membership_card_activity`
--

DROP TABLE IF EXISTS `jeez_membership_card_activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_membership_card_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `card_type_id` bigint NOT NULL COMMENT '卡种ID(关联会员卡种表)',
  `activity_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动名称',
  `activity_type` tinyint DEFAULT '1' COMMENT '活动类型(1:折扣 2:满减 3:赠品 4:套餐优惠 5:限时秒杀 6:新客专享)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '活动描述',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:进行中 2:未开始 3:已结束 4:已取消)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_card_type_id` (`card_type_id`),
  KEY `idx_activity_type` (`activity_type`),
  KEY `idx_status` (`status`),
  KEY `idx_start_end_time` (`start_time`,`end_time`),
  CONSTRAINT `jeez_membership_card_activity_ibfk_1` FOREIGN KEY (`card_type_id`) REFERENCES `jeez_membership_card_type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_membership_card_activity`
--

LOCK TABLES `jeez_membership_card_activity` WRITE;
/*!40000 ALTER TABLE `jeez_membership_card_activity` DISABLE KEYS */;
INSERT INTO `jeez_membership_card_activity` VALUES (1,5,'new_user_trial_offer',6,'first purchase discount for trial card users','2025-11-01 00:00:00','2025-12-31 23:59:59',1,'2025-11-05 19:00:00','2025-11-05 19:00:00',1,1,0),(2,2,'annual_card_flash_sale',5,'annual card flash sale with bundled trial session','2025-11-10 00:00:00','2025-11-11 23:59:59',2,'2025-11-05 19:00:00','2025-11-05 19:00:00',1,1,0),(3,1,'yoga_card_threshold_discount',2,'threshold discount for yoga times card purchases','2025-11-01 00:00:00','2025-11-30 23:59:59',1,'2025-11-05 19:00:00','2025-11-05 19:00:00',1,1,0);
/*!40000 ALTER TABLE `jeez_membership_card_activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_membership_card_type`
--

DROP TABLE IF EXISTS `jeez_membership_card_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_membership_card_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '卡种ID',
  `card_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡种名称',
  `card_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '卡种编码',
  `card_type_id` tinyint NOT NULL COMMENT '卡类型ID',
  `valid_type` tinyint NOT NULL COMMENT '有效期类型(1:次数卡 2:天数卡)',
  `store_scope` tinyint NOT NULL DEFAULT '1' COMMENT '适用门店范围(1:全门店通用 2:指定门店)',
  `store_id` bigint DEFAULT NULL COMMENT '门店ID(历史字段，单门店；NULL表示全门店通用)',
  `duration_days` int DEFAULT NULL COMMENT '有效天数',
  `total_times` int DEFAULT NULL COMMENT '总次数',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `deposit` decimal(8,2) DEFAULT '0.00' COMMENT '押金',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '卡种描述',
  `benefits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '会员权益(JSON格式)',
  `restrictions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '使用限制(JSON格式)',
  `is_sell_online` tinyint(1) DEFAULT '1' COMMENT '是否支持线上销售',
  `is_transferable` tinyint(1) DEFAULT '0' COMMENT '是否可转让',
  `is_refundable` tinyint(1) DEFAULT '0' COMMENT '是否可退款',
  `freeze_days` int DEFAULT '0' COMMENT '最大冻结天数',
  `has_activity` tinyint(1) DEFAULT '0' COMMENT '是否有活动(0:无 1:有)',
  `tag` int DEFAULT '0' COMMENT '标签(0:无标签 1:热销 2:推荐 3:新品 4:限时等)',
  `store_type` int DEFAULT '0' COMMENT '门店类型(0:通用 1:直营店 2:加盟店 3:合作店等)',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `card_code` (`card_code`),
  KEY `idx_card_code` (`card_code`),
  KEY `idx_card_type_id` (`card_type_id`),
  KEY `idx_valid_type` (`valid_type`),
  KEY `idx_store_scope` (`store_scope`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_sell_online` (`is_sell_online`),
  KEY `idx_has_activity` (`has_activity`),
  KEY `idx_tag` (`tag`),
  KEY `idx_store_type` (`store_type`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `jeez_membership_card_type_ibfk_1` FOREIGN KEY (`card_type_id`) REFERENCES `jeez_dict_card_type` (`id`),
  CONSTRAINT `jeez_membership_card_type_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡种表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_membership_card_type`
--

LOCK TABLES `jeez_membership_card_type` WRITE;
/*!40000 ALTER TABLE `jeez_membership_card_type` DISABLE KEYS */;
INSERT INTO `jeez_membership_card_type` VALUES (1,'瑜伽次卡10次','YOGA_10',1,1,1,NULL,NULL,10,880.00,1080.00,0.00,'瑜伽课程专用次卡，适合不定期训练','{\"courses\":[\"瑜伽课程\"],\"discounts\":[\"水杯8折\",\"瑜伽垫9折\"]}','{\"valid_months\":6,\"transfer_fee\":50}',1,1,0,30,0,1,0,1,1,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(2,'健身年卡','GYM_YEAR',2,2,1,NULL,365,NULL,2880.00,3680.00,200.00,'全年无限次健身，包含器械区','{\"areas\":[\"力量区\",\"有氧区\",\"淋浴区\"],\"discounts\":[\"私教9折\",\"商品95折\"]}','{\"freeze_max_days\":60,\"transfer_fee\":100}',1,0,1,60,0,2,0,2,1,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(3,'游泳月卡','SWIM_MONTH',2,2,1,NULL,30,NULL,480.00,580.00,100.00,'游泳专区月卡，恒温泳池无限畅游','{\"areas\":[\"游泳池\",\"淋浴区\"],\"discounts\":[\"泳具9折\"]}','{\"daily_max_hours\":4}',1,0,0,15,0,0,0,3,1,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(4,'私教包月12节','PT_12',1,1,1,NULL,NULL,12,3600.00,4200.00,0.00,'私教课程包月，一对一指导','{\"services\":[\"体质测试\",\"定制计划\",\"营养指导\"],\"validity\":90}','{\"advance_cancel_hours\":24,\"no_show_fee\":100}',1,0,1,0,0,2,0,4,1,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0),(5,'综合体验卡30天','TRIAL_30',3,2,1,NULL,30,NULL,299.00,399.00,0.00,'新用户体验套餐，包含基础服务','{\"areas\":[\"健身区\",\"游泳池\"],\"courses\":[\"团体课\"],\"services\":[\"体测1次\"]}','{\"no_renewal\":true,\"one_time_only\":true}',1,0,0,0,1,3,0,5,1,'2025-11-05 18:06:35','2025-11-05 18:06:35',1,1,0);
/*!40000 ALTER TABLE `jeez_membership_card_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_membership_card_type_channel`
--

DROP TABLE IF EXISTS `jeez_membership_card_type_channel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_membership_card_type_channel` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_type_id` bigint NOT NULL COMMENT '会员卡种ID',
  `channel_id` int NOT NULL COMMENT '销售渠道ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_type_channel` (`card_type_id`,`channel_id`),
  KEY `idx_channel_id` (`channel_id`),
  CONSTRAINT `fk_ctc_card_type` FOREIGN KEY (`card_type_id`) REFERENCES `jeez_membership_card_type` (`id`),
  CONSTRAINT `fk_ctc_channel` FOREIGN KEY (`channel_id`) REFERENCES `jeez_dict_sales_channel` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡种-销售渠道关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_membership_card_type_channel`
--

LOCK TABLES `jeez_membership_card_type_channel` WRITE;
/*!40000 ALTER TABLE `jeez_membership_card_type_channel` DISABLE KEYS */;
INSERT INTO `jeez_membership_card_type_channel` VALUES (1,1,1,'2026-02-26 23:46:18'),(2,4,1,'2026-02-26 23:46:18'),(3,2,1,'2026-02-26 23:46:18'),(4,3,1,'2026-02-26 23:46:18'),(5,5,1,'2026-02-26 23:46:18');
/*!40000 ALTER TABLE `jeez_membership_card_type_channel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_membership_card_type_store`
--

DROP TABLE IF EXISTS `jeez_membership_card_type_store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_membership_card_type_store` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_type_id` bigint NOT NULL COMMENT '会员卡种ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_type_store` (`card_type_id`,`store_id`),
  KEY `idx_store_id` (`store_id`),
  CONSTRAINT `fk_cts_card_type` FOREIGN KEY (`card_type_id`) REFERENCES `jeez_membership_card_type` (`id`),
  CONSTRAINT `fk_cts_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡种-适用门店关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_membership_card_type_store`
--

LOCK TABLES `jeez_membership_card_type_store` WRITE;
/*!40000 ALTER TABLE `jeez_membership_card_type_store` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_membership_card_type_store` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_order`
--

DROP TABLE IF EXISTS `jeez_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_type` tinyint NOT NULL COMMENT '订单类型 1-会员卡 2-课程 3-私教 4-商品',
  `product_id` bigint DEFAULT NULL COMMENT '商品ID',
  `product_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品名称',
  `product_description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品描述',
  `quantity` int DEFAULT '1' COMMENT '商品数量',
  `unit_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实际支付金额',
  `order_status` tinyint NOT NULL DEFAULT '1' COMMENT '订单状态 1-待支付 2-已支付 3-已完成 4-已取消 5-已退款',
  `payment_method` tinyint DEFAULT NULL COMMENT '支付方式 1-微信 2-支付宝 3-现金',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `payment_transaction_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付流水号',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款原因',
  `refund_transaction_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款流水号',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '订单备注',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_type` (`order_type`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_order`
--

LOCK TABLES `jeez_order` WRITE;
/*!40000 ALTER TABLE `jeez_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_permission`
--

DROP TABLE IF EXISTS `jeez_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限ID（0表示顶级）',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `permission_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限类型：DIR-目录，MENU-菜单，BUTTON-按钮',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `visible` tinyint DEFAULT '1' COMMENT '是否可见：0-隐藏，1-显示',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_permission_type` (`permission_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=60404 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_permission`
--

LOCK TABLES `jeez_permission` WRITE;
/*!40000 ALTER TABLE `jeez_permission` DISABLE KEYS */;
INSERT INTO `jeez_permission` VALUES (1,0,'store','门店管理','DIR','/store',NULL,'store',1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(2,0,'user','用户管理','DIR','/user',NULL,'user',2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(3,0,'order','订单管理','DIR','/order',NULL,'order',3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(4,0,'operation','运营配置','DIR','/operation',NULL,'setting',4,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(5,0,'statistics','数据统计','DIR','/statistics',NULL,'chart',5,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(6,0,'system','系统设置','DIR','/system',NULL,'system',6,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(101,1,'store:list','门店列表','MENU','/store/list','store/list',NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(102,101,'store:detail','门店详情页','MENU','/store/detail/:id','store/detail',NULL,2,0,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(201,2,'user:list','用户列表','MENU','/user/list','user/list',NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(202,201,'user:detail','用户详情页','MENU','/user/detail/:id','user/detail',NULL,2,0,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(301,3,'order:list','订单列表','MENU','/order/list','order/list',NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(302,301,'order:detail','订单详情页','MENU','/order/detail/:id','order/detail',NULL,2,0,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(401,4,'operation:miniprogram','小程序配置','MENU','/operation/miniprogram','operation/miniprogram',NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(402,4,'operation:product','产品配置','MENU','/operation/product','operation/product',NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(501,5,'statistics:data','数据统计','MENU','/statistics/data','statistics/data',NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(601,6,'system:structure','门店架构','MENU','/system/structure','system/structure',NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(602,6,'system:role','角色权限','MENU','/system/role','system/role',NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(603,6,'system:user','系统用户','MENU','/system/user','system/user',NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(604,6,'system:third-party','第三方配置','MENU','/system/third-party','system/third-party',NULL,4,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(605,6,'system:log','系统日志','MENU','/system/log','system/log',NULL,5,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10101,101,'store:list:add','新增','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10102,101,'store:list:export','导出','BUTTON',NULL,NULL,NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10103,101,'store:list:detail','查看详情','BUTTON',NULL,NULL,NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10104,101,'store:list:delete','删除','BUTTON',NULL,NULL,NULL,4,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10105,101,'store:list:edit:basic','编辑基本信息','BUTTON',NULL,NULL,NULL,5,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10106,101,'store:list:edit:product','编辑商品信息','BUTTON',NULL,NULL,NULL,6,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10107,101,'store:list:edit:group','编辑团购清单','BUTTON',NULL,NULL,NULL,7,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10108,101,'store:list:edit:operation','编辑操作清单','BUTTON',NULL,NULL,NULL,8,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(10201,102,'store:detail:edit','编辑','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(20101,201,'user:list:export','导出','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(20102,201,'user:list:detail','查看详情','BUTTON',NULL,NULL,NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(20103,201,'user:list:delete','删除','BUTTON',NULL,NULL,NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(30101,301,'order:list:export','导出','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(30102,301,'order:list:detail','查看详情','BUTTON',NULL,NULL,NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(30103,301,'order:list:delete','删除','BUTTON',NULL,NULL,NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(40101,401,'operation:miniprogram:add','新增','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(40102,401,'operation:miniprogram:edit','编辑','BUTTON',NULL,NULL,NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(40103,401,'operation:miniprogram:import','批量导入','BUTTON',NULL,NULL,NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(40104,401,'operation:miniprogram:delete','删除','BUTTON',NULL,NULL,NULL,4,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60101,601,'system:structure:edit:level','编辑架构层级','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60102,601,'system:structure:add:store','新增门店','BUTTON',NULL,NULL,NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60103,601,'system:structure:detail:store','查看门店详情','BUTTON',NULL,NULL,NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60104,601,'system:structure:delete:store','删除门店','BUTTON',NULL,NULL,NULL,4,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60201,602,'system:role:add','新增','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60202,602,'system:role:edit','编辑','BUTTON',NULL,NULL,NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60203,602,'system:role:delete','删除','BUTTON',NULL,NULL,NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60301,603,'system:user:add','新增','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60302,603,'system:user:edit','编辑','BUTTON',NULL,NULL,NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60303,603,'system:user:delete','删除','BUTTON',NULL,NULL,NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60401,604,'system:third-party:add','新增','BUTTON',NULL,NULL,NULL,1,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60402,604,'system:third-party:edit','编辑','BUTTON',NULL,NULL,NULL,2,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14'),(60403,604,'system:third-party:delete','删除','BUTTON',NULL,NULL,NULL,3,1,1,NULL,0,'2025-12-18 23:18:14','2025-12-18 23:18:14');
/*!40000 ALTER TABLE `jeez_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_personal_training_booking`
--

DROP TABLE IF EXISTS `jeez_personal_training_booking`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_personal_training_booking` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '私教预约ID',
  `booking_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '预约编号',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `training_date` date NOT NULL COMMENT '训练日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `training_duration` int DEFAULT '60' COMMENT '训练时长(分钟)',
  `training_goal` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '训练目标',
  `booking_status` tinyint DEFAULT '1' COMMENT '预约状态(1:已预约 2:已取消 3:已完成 4:未到场)',
  `booking_time` datetime NOT NULL COMMENT '预约时间',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取消原因',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `fee` decimal(8,2) DEFAULT '0.00' COMMENT '费用',
  `payment_status` tinyint DEFAULT '1' COMMENT '支付状态(1:未支付 2:已支付 3:已退款)',
  `feedback_rating` tinyint DEFAULT NULL COMMENT '评分(1-5分)',
  `feedback_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '评价内容',
  `training_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '训练计划',
  `training_summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '训练总结',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `booking_no` (`booking_no`),
  KEY `idx_booking_no` (`booking_no`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_training_date` (`training_date`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_booking_status` (`booking_status`),
  KEY `idx_booking_time` (`booking_time`),
  KEY `idx_training_date_time` (`training_date`,`start_time`),
  CONSTRAINT `jeez_personal_training_booking_ibfk_1` FOREIGN KEY (`member_id`) REFERENCES `jeez_member` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_personal_training_booking_ibfk_2` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_personal_training_booking_ibfk_3` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私教预约表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_personal_training_booking`
--

LOCK TABLES `jeez_personal_training_booking` WRITE;
/*!40000 ALTER TABLE `jeez_personal_training_booking` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_personal_training_booking` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_price_change_record`
--

DROP TABLE IF EXISTS `jeez_price_change_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_price_change_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '变动记录ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `service_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务类型',
  `service_id` bigint DEFAULT NULL COMMENT '服务ID',
  `service_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务名称',
  `old_price` decimal(10,2) DEFAULT NULL COMMENT '原价格',
  `new_price` decimal(10,2) DEFAULT NULL COMMENT '新价格',
  `price_change_amount` decimal(10,2) GENERATED ALWAYS AS ((coalesce(`new_price`,0) - coalesce(`old_price`,0))) STORED COMMENT '价格变动金额',
  `price_change_percentage` decimal(5,2) GENERATED ALWAYS AS ((case when ((`old_price` is null) or (`old_price` = 0)) then NULL else round((((`new_price` - `old_price`) / `old_price`) * 100),2) end)) STORED COMMENT '价格变动百分比',
  `change_type` tinyint NOT NULL COMMENT '变动类型(1:涨价 2:降价 3:新增 4:停用)',
  `change_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '变动原因',
  `market_factors` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '市场因素(JSON格式)',
  `cost_factors` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '成本因素(JSON格式)',
  `competitive_factors` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '竞争因素(JSON格式)',
  `expected_impact` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '预期影响(JSON格式)',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `announcement_date` date DEFAULT NULL COMMENT '公布日期',
  `approval_status` tinyint DEFAULT '1' COMMENT '审批状态(1:待审批 2:已批准 3:已拒绝)',
  `approved_by` bigint DEFAULT NULL COMMENT '审批人ID',
  `approved_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approval_notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批备注',
  `notification_sent` tinyint(1) DEFAULT '0' COMMENT '是否已发送通知',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:待生效 2:已生效 3:已取消)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_service_type` (`service_type`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_change_type` (`change_type`),
  KEY `idx_effective_date` (`effective_date`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_status` (`status`),
  KEY `idx_created_time` (`created_time`),
  KEY `approved_by` (`approved_by`),
  CONSTRAINT `jeez_price_change_record_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_price_change_record_ibfk_2` FOREIGN KEY (`approved_by`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格变动记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_price_change_record`
--

LOCK TABLES `jeez_price_change_record` WRITE;
/*!40000 ALTER TABLE `jeez_price_change_record` DISABLE KEYS */;
INSERT INTO `jeez_price_change_record` (`id`, `store_id`, `service_type`, `service_id`, `service_name`, `old_price`, `new_price`, `change_type`, `change_reason`, `market_factors`, `cost_factors`, `competitive_factors`, `expected_impact`, `effective_date`, `announcement_date`, `approval_status`, `approved_by`, `approved_time`, `approval_notes`, `notification_sent`, `status`, `created_time`, `updated_time`, `created_by`, `updated_by`, `is_deleted`) VALUES (1,1,'private_training',2,'一对一私教课程',400.00,450.00,1,'教练成本上涨及服务品质提升','{\"inflation_rate\": 0.03, \"market_demand\": \"high\"}','{\"coach_salary_increase\": 0.15, \"equipment_cost_increase\": 0.1}','{\"competitor_prices\": [420, 480, 500]}','{\"revenue_impact\": \"+12.5%\", \"demand_impact\": \"-5%\"}','2023-07-01','2023-06-20',2,1,'2023-06-15 14:00:00',NULL,1,2,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(2,2,'group_class',1,'瑜伽团体课程',100.00,120.00,1,'瑜伽教练薪资调整及课程升级','{\"yoga_trend\": \"growing\"}','{\"coach_certification_cost\": 0.2, \"smaller_class_size\": 0.1}','{\"competitor_prices\": [110, 130, 125]}','{\"participation_impact\": \"-8%\", \"revenue_impact\": \"+15%\"}','2023-06-15','2023-06-01',2,1,'2023-05-28 10:30:00',NULL,1,2,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0);
/*!40000 ALTER TABLE `jeez_price_change_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_pricing_analysis_report`
--

DROP TABLE IF EXISTS `jeez_pricing_analysis_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_pricing_analysis_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `report_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '报表名称',
  `report_type` tinyint NOT NULL COMMENT '报表类型(1:价格对比 2:利润分析 3:竞争分析 4:趋势分析)',
  `store_id` bigint DEFAULT NULL COMMENT '门店ID(NULL表示全部门店)',
  `service_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务类型',
  `analysis_period_start` date NOT NULL COMMENT '分析开始日期',
  `analysis_period_end` date NOT NULL COMMENT '分析结束日期',
  `report_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '报表数据(JSON格式)',
  `key_findings` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '关键发现',
  `recommendations` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '建议',
  `competitive_landscape` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '竞争格局分析',
  `market_trends` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '市场趋势分析',
  `price_elasticity` decimal(8,4) DEFAULT NULL COMMENT '价格弹性系数',
  `optimal_price_range` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '最优价格区间(JSON格式)',
  `generated_by` bigint DEFAULT NULL COMMENT '生成人ID',
  `generated_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `review_status` tinyint DEFAULT '1' COMMENT '审核状态(1:待审核 2:已通过 3:需修改)',
  `reviewed_by` bigint DEFAULT NULL COMMENT '审核人ID',
  `reviewed_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_comments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '审核意见',
  `is_published` tinyint(1) DEFAULT '0' COMMENT '是否已发布',
  `published_time` datetime DEFAULT NULL COMMENT '发布时间',
  `version` int DEFAULT '1' COMMENT '版本号',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_analysis_period` (`analysis_period_start`,`analysis_period_end`),
  KEY `idx_generated_time` (`generated_time`),
  KEY `idx_review_status` (`review_status`),
  KEY `idx_is_published` (`is_published`),
  KEY `generated_by` (`generated_by`),
  KEY `reviewed_by` (`reviewed_by`),
  CONSTRAINT `jeez_pricing_analysis_report_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_pricing_analysis_report_ibfk_2` FOREIGN KEY (`generated_by`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_pricing_analysis_report_ibfk_3` FOREIGN KEY (`reviewed_by`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格分析报表表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_pricing_analysis_report`
--

LOCK TABLES `jeez_pricing_analysis_report` WRITE;
/*!40000 ALTER TABLE `jeez_pricing_analysis_report` DISABLE KEYS */;
INSERT INTO `jeez_pricing_analysis_report` VALUES (1,'朝阳旗舰店Q3价格竞争力分析',3,1,'private_training','2023-07-01','2023-09-30','{\"total_services\": 450, \"avg_price\": 445, \"utilization_rate\": 0.78, \"revenue\": 200250}','价格处于中高水平，但服务品质获得客户认可','建议推出入门级私教套餐，扩大客户基础','{\"main_competitors\": [\"健身工厂\", \"星动力\"], \"price_position\": \"中高端\"}','{\"demand_trend\": \"stable\", \"price_sensitivity\": \"medium\"}',-0.8000,'{\"min\": 420, \"optimal\": 445, \"max\": 480}',1,'2025-11-05 18:28:16',2,1,'2023-10-15 14:00:00',NULL,1,'2023-10-16 09:00:00',1,'2025-11-05 18:28:16','2025-11-05 18:28:16',0),(2,'海淀标准店团体课程定价分析',1,2,'group_class','2023-07-01','2023-09-30','{\"total_classes\": 280, \"avg_participants\": 15, \"avg_price\": 95, \"total_revenue\": 39900}','团体课程定价有竞争力，参与率稳步提升','可考虑增加高端团体课程种类，提升客单价','{\"advantage\": \"price_leadership\", \"threat\": \"new_competitors\"}','{\"group_fitness\": \"growing\", \"specialized_classes\": \"demand_increase\"}',-1.2000,'{\"min\": 90, \"optimal\": 105, \"max\": 120}',2,'2025-11-05 18:28:16',2,1,'2023-10-10 16:30:00',NULL,1,'2023-10-11 08:30:00',1,'2025-11-05 18:28:16','2025-11-05 18:28:16',0);
/*!40000 ALTER TABLE `jeez_pricing_analysis_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_pricing_calculation_rule`
--

DROP TABLE IF EXISTS `jeez_pricing_calculation_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_pricing_calculation_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `strategy_id` bigint NOT NULL COMMENT '策略ID',
  `rule_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `rule_type` tinyint NOT NULL COMMENT '规则类型(1:基础价格 2:时段加价 3:会员折扣 4:数量折扣 5:服务附加费)',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型',
  `target_id` bigint DEFAULT NULL COMMENT '目标ID',
  `calculation_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '计算方法(fixed/percentage/multiplier)',
  `calculation_value` decimal(10,4) NOT NULL COMMENT '计算值',
  `conditions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '适用条件(JSON格式)',
  `priority` int DEFAULT '1' COMMENT '优先级',
  `is_stackable` tinyint(1) DEFAULT '1' COMMENT '是否可叠加',
  `max_discount_amount` decimal(10,2) DEFAULT NULL COMMENT '最大折扣金额',
  `min_order_amount` decimal(10,2) DEFAULT NULL COMMENT '最小订单金额',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '规则描述',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_strategy_id` (`strategy_id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_target_type` (`target_type`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_effective_date` (`effective_date`),
  KEY `idx_priority` (`priority`),
  CONSTRAINT `jeez_pricing_calculation_rule_ibfk_1` FOREIGN KEY (`strategy_id`) REFERENCES `jeez_store_pricing_strategy` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='价格计算规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_pricing_calculation_rule`
--

LOCK TABLES `jeez_pricing_calculation_rule` WRITE;
/*!40000 ALTER TABLE `jeez_pricing_calculation_rule` DISABLE KEYS */;
INSERT INTO `jeez_pricing_calculation_rule` VALUES (1,1,'VIP会员私教折扣',3,'member_level',1,'percentage',0.1500,'{\"member_level\": \"VIP\", \"service_type\": \"private_training\"}',2,1,NULL,NULL,'VIP会员私教课程15%折扣',1,'2023-01-01',NULL,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(2,1,'高峰时段附加费',2,'time_slot',NULL,'multiplier',1.2000,'{\"time_range\": [\"18:00-21:00\"], \"weekdays\": [\"周一至周五\"]}',3,0,NULL,NULL,'高峰时段20%附加费',1,'2023-01-01',NULL,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(3,2,'学生身份折扣',3,'customer_type',NULL,'percentage',0.2500,'{\"customer_type\": \"student\", \"verification\": \"student_id\"}',1,1,NULL,NULL,'学生身份25%折扣',1,'2023-01-01',NULL,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(4,3,'团体课程人数折扣',4,'group_class',NULL,'percentage',0.2000,'{\"min_participants\": 3, \"max_participants\": 8}',2,1,NULL,NULL,'3人以上团体课程20%折扣',1,'2023-01-01',NULL,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0);
/*!40000 ALTER TABLE `jeez_pricing_calculation_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_records`
--

DROP TABLE IF EXISTS `jeez_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_records` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'record id',
  `enroll_id` bigint DEFAULT NULL COMMENT 'device enroll id',
  `records_time` datetime DEFAULT NULL COMMENT 'record time',
  `mode` int DEFAULT NULL COMMENT 'verify mode (0 fingerprint,1 card,2 password,8 face)',
  `int_out` int DEFAULT NULL COMMENT 'in/out flag (0 in,1 out)',
  `event` int DEFAULT NULL COMMENT 'event code',
  `device_serial_num` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'device serial number',
  `temperature` double DEFAULT NULL COMMENT 'body temperature',
  `image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'snapshot image path',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_records_time` (`records_time`),
  KEY `idx_device_serial_num` (`device_serial_num`),
  KEY `idx_enroll_id` (`enroll_id`),
  CONSTRAINT `fk_jeez_records_device_sn` FOREIGN KEY (`device_serial_num`) REFERENCES `jeez_device` (`sn`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='attendance/access records';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_records`
--

LOCK TABLES `jeez_records` WRITE;
/*!40000 ALTER TABLE `jeez_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_redemption_code`
--

DROP TABLE IF EXISTS `jeez_redemption_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_redemption_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '兑换码ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `code_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '兑换码值',
  `code_type_id` tinyint NOT NULL COMMENT '兑换码类型ID',
  `store_id` bigint DEFAULT NULL COMMENT '适用门店ID',
  `specific_benefits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '特定权益(JSON格式)',
  `custom_restrictions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '自定义限制(JSON格式)',
  `expiry_date` datetime DEFAULT NULL COMMENT '过期时间',
  `max_uses` int DEFAULT '1' COMMENT '最大使用次数',
  `used_count` int DEFAULT '0' COMMENT '已使用次数',
  `remaining_uses` int GENERATED ALWAYS AS ((`max_uses` - `used_count`)) STORED COMMENT '剩余使用次数',
  `first_used_time` datetime DEFAULT NULL COMMENT '首次使用时间',
  `last_used_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  `generator_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '生成信息',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:未使用 2:已使用 3:已过期 4:已停用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_value` (`code_value`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_code_value` (`code_value`),
  KEY `idx_code_type_id` (`code_type_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_created_time` (`created_time`),
  CONSTRAINT `jeez_redemption_code_ibfk_1` FOREIGN KEY (`batch_id`) REFERENCES `jeez_redemption_code_batch` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_redemption_code_ibfk_2` FOREIGN KEY (`code_type_id`) REFERENCES `jeez_dict_redemption_code_type` (`id`),
  CONSTRAINT `jeez_redemption_code_ibfk_3` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换码表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_redemption_code`
--

LOCK TABLES `jeez_redemption_code` WRITE;
/*!40000 ALTER TABLE `jeez_redemption_code` DISABLE KEYS */;
INSERT INTO `jeez_redemption_code` (`id`, `batch_id`, `code_value`, `code_type_id`, `store_id`, `specific_benefits`, `custom_restrictions`, `expiry_date`, `max_uses`, `used_count`, `first_used_time`, `last_used_time`, `generator_info`, `status`, `created_time`, `updated_time`) VALUES (1,1,'GUEST2023A001',1,NULL,NULL,NULL,'2023-12-31 23:59:59',1,0,NULL,NULL,NULL,1,'2023-10-01 10:00:00','2025-11-05 18:24:26'),(2,1,'GUEST2023A002',1,NULL,NULL,NULL,'2023-12-31 23:59:59',1,0,NULL,NULL,NULL,2,'2023-10-01 10:00:00','2025-11-05 18:24:26'),(3,1,'GUEST2023A003',1,NULL,NULL,NULL,'2023-12-31 23:59:59',1,0,NULL,NULL,NULL,1,'2023-10-01 10:00:00','2025-11-05 18:24:26'),(4,2,'YOGA001',2,1,NULL,NULL,'2023-11-30 23:59:59',2,0,NULL,NULL,NULL,1,'2023-10-05 14:00:00','2025-11-05 18:24:26'),(5,2,'YOGA002',2,1,NULL,NULL,'2023-11-30 23:59:59',2,0,NULL,NULL,NULL,1,'2023-10-05 14:00:00','2025-11-05 18:24:26'),(6,3,'PASS001',3,1,NULL,NULL,'2023-10-31 23:59:59',1,0,NULL,NULL,NULL,1,'2023-10-10 09:00:00','2025-11-05 18:24:26');
/*!40000 ALTER TABLE `jeez_redemption_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_redemption_code_batch`
--

DROP TABLE IF EXISTS `jeez_redemption_code_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_redemption_code_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '批次ID',
  `batch_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次名称',
  `batch_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '批次编码',
  `code_type_id` tinyint NOT NULL COMMENT '兑换码类型ID',
  `store_id` bigint DEFAULT NULL COMMENT '适用门店ID(NULL表示全门店通用)',
  `total_codes` int NOT NULL DEFAULT '0' COMMENT '总数量',
  `used_codes` int DEFAULT '0' COMMENT '已使用数量',
  `remaining_codes` int GENERATED ALWAYS AS ((`total_codes` - `used_codes`)) STORED COMMENT '剩余数量',
  `prefix` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '码前缀',
  `code_length` int DEFAULT '12' COMMENT '码长度',
  `expiry_date` datetime DEFAULT NULL COMMENT '过期时间',
  `max_uses_per_user` int DEFAULT '1' COMMENT '每用户最大使用次数',
  `activate_date` datetime DEFAULT NULL COMMENT '激活时间',
  `deactivate_date` datetime DEFAULT NULL COMMENT '停用时间',
  `usage_restrictions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '使用限制(JSON格式)',
  `benefits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '兑换权益(JSON格式)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '批次描述',
  `internal_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '内部备注',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:正常 2:已停用 3:已过期 4:已用完)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `batch_code` (`batch_code`),
  KEY `idx_batch_code` (`batch_code`),
  KEY `idx_code_type_id` (`code_type_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_created_time` (`created_time`),
  CONSTRAINT `jeez_redemption_code_batch_ibfk_1` FOREIGN KEY (`code_type_id`) REFERENCES `jeez_dict_redemption_code_type` (`id`),
  CONSTRAINT `jeez_redemption_code_batch_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换码批次表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_redemption_code_batch`
--

LOCK TABLES `jeez_redemption_code_batch` WRITE;
/*!40000 ALTER TABLE `jeez_redemption_code_batch` DISABLE KEYS */;
INSERT INTO `jeez_redemption_code_batch` (`id`, `batch_name`, `batch_code`, `code_type_id`, `store_id`, `total_codes`, `used_codes`, `prefix`, `code_length`, `expiry_date`, `max_uses_per_user`, `activate_date`, `deactivate_date`, `usage_restrictions`, `benefits`, `description`, `internal_notes`, `status`, `created_time`, `updated_time`, `created_by`, `updated_by`, `is_deleted`) VALUES (1,'国庆节体验活动','NATDAY2023',1,NULL,1000,150,'GUEST',12,'2023-12-31 23:59:59',1,NULL,NULL,'{\"min_age\": 18, \"max_age\": 65}','{\"trial_days\": 7, \"facilities\": [\"健身房\", \"淋浴房\"]}','国庆节推广活动，7天免费体验',NULL,1,'2025-11-05 18:24:26','2025-11-05 18:24:26',1,1,0),(2,'瑜伽体验课程','YOGA_TRIAL',2,1,500,89,'YOGA',10,'2023-11-30 23:59:59',2,NULL,NULL,'{\"new_members_only\": true}','{\"free_classes\": 2, \"class_types\": [\"哈他瑜伽\", \"流瑜伽\"]}','瑜伽课程体验券，可参加2节免费课程',NULL,1,'2025-11-05 18:24:26','2025-11-05 18:24:26',1,1,0),(3,'游客通行证','GUEST_PASS',3,1,200,45,'PASS',8,'2023-10-31 23:59:59',1,NULL,NULL,'{\"id_required\": true}','{\"access_hours\": 24, \"facilities\": [\"基础器械区\"]}','单日游客通行证',NULL,1,'2025-11-05 18:24:26','2025-11-05 18:24:26',1,1,0);
/*!40000 ALTER TABLE `jeez_redemption_code_batch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_redemption_record`
--

DROP TABLE IF EXISTS `jeez_redemption_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_redemption_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '兑换记录ID',
  `redemption_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '兑换编号',
  `code_id` bigint NOT NULL COMMENT '兑换码ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `user_id` bigint NOT NULL COMMENT '用户ID(关联tb_user表)',
  `member_id` bigint DEFAULT NULL COMMENT '会员ID(兑换后生成的会员ID)',
  `store_id` bigint DEFAULT NULL COMMENT '使用门店ID',
  `redemption_device` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '兑换设备',
  `redemption_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '兑换IP地址',
  `redemption_time` datetime NOT NULL COMMENT '兑换时间',
  `redemption_channel` tinyint DEFAULT '1' COMMENT '兑换渠道(1:线上 2:前台 3:工作人员 4:自助终端)',
  `verification_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '验证码',
  `verification_method` tinyint DEFAULT '1' COMMENT '验证方式(1:无验证 2:短信验证 3:邮箱验证 4:人工验证)',
  `benefits_applied` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '应用权益(JSON格式)',
  `created_member_id` bigint DEFAULT NULL COMMENT '创建的会员ID',
  `created_card_id` bigint DEFAULT NULL COMMENT '创建的会员卡ID',
  `created_booking_id` bigint DEFAULT NULL COMMENT '创建的预约ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作员ID',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '备注',
  `redemption_status` tinyint DEFAULT '1' COMMENT '兑换状态(1:成功 2:失败 3:已取消)',
  `failure_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `redemption_no` (`redemption_no`),
  KEY `idx_redemption_no` (`redemption_no`),
  KEY `idx_code_id` (`code_id`),
  KEY `idx_batch_id` (`batch_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_redemption_time` (`redemption_time`),
  KEY `idx_redemption_status` (`redemption_status`),
  KEY `idx_redemption_channel` (`redemption_channel`),
  CONSTRAINT `jeez_redemption_record_ibfk_1` FOREIGN KEY (`code_id`) REFERENCES `jeez_redemption_code` (`id`),
  CONSTRAINT `jeez_redemption_record_ibfk_2` FOREIGN KEY (`batch_id`) REFERENCES `jeez_redemption_code_batch` (`id`),
  CONSTRAINT `jeez_redemption_record_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_redemption_record_ibfk_4` FOREIGN KEY (`member_id`) REFERENCES `jeez_member` (`id`),
  CONSTRAINT `jeez_redemption_record_ibfk_5` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_redemption_record`
--

LOCK TABLES `jeez_redemption_record` WRITE;
/*!40000 ALTER TABLE `jeez_redemption_record` DISABLE KEYS */;
INSERT INTO `jeez_redemption_record` VALUES (1,'RD2023100101',2,1,4,4,1,NULL,NULL,'2023-10-05 16:30:00',1,NULL,1,'{\"trial_days\": 7, \"member_type\": \"体验会员\"}',4,4,NULL,1,'游客兑换7天体验卡',1,NULL,'2023-10-05 16:30:00','2025-11-05 18:24:26'),(2,'RD2023100502',4,2,3,NULL,1,NULL,NULL,'2023-10-08 10:15:00',1,NULL,1,'{\"free_classes\": 2, \"class_types\": [\"哈他瑜伽\"]}',NULL,NULL,NULL,3,'瑜伽会员兑换免费课程',1,NULL,'2023-10-08 10:15:00','2025-11-05 18:24:26'),(3,'RD2023101003',6,3,5,NULL,1,NULL,NULL,'2023-10-12 19:45:00',2,NULL,1,'{\"access_hours\": 24, \"facilities\": [\"基础器械区\"]}',NULL,NULL,NULL,1,'前台兑换游客通行证',1,NULL,'2023-10-12 19:45:00','2025-11-05 18:24:26');
/*!40000 ALTER TABLE `jeez_redemption_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_refund_order`
--

DROP TABLE IF EXISTS `jeez_refund_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_refund_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款订单号',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原支付订单号',
  `transaction_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信支付订单号',
  `refund_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信退款单号',
  `total_amount` int NOT NULL COMMENT '原订单金额(分)',
  `refund_amount` int NOT NULL COMMENT '退款金额(分)',
  `refund_amount_yuan` decimal(10,2) NOT NULL COMMENT '退款金额(元)',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'CNY' COMMENT '货币类型',
  `reason` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款原因',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-退款中 1-退款成功 2-退款失败',
  `refund_success_time` datetime DEFAULT NULL COMMENT '退款成功时间',
  `notify_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款回调地址',
  `attach` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附加数据',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_refund_order_main_order` FOREIGN KEY (`order_no`) REFERENCES `jeez_order` (`order_no`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_refund_order`
--

LOCK TABLES `jeez_refund_order` WRITE;
/*!40000 ALTER TABLE `jeez_refund_order` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_refund_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_role`
--

DROP TABLE IF EXISTS `jeez_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `description` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT '1' COMMENT '角色状态 0-禁用 1-启用',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_role`
--

LOCK TABLES `jeez_role` WRITE;
/*!40000 ALTER TABLE `jeez_role` DISABLE KEYS */;
INSERT INTO `jeez_role` VALUES (1,'admin','管理员','系统管理员，拥有所有权限',1,0,'2025-10-23 10:48:42','2025-10-23 10:48:42'),(2,'coach','教练','健身教练，可以管理会员和课程',1,0,'2025-10-23 10:48:42','2025-10-23 10:48:42'),(3,'member','普通会员','普通健身房会员',1,0,'2025-10-23 10:48:42','2025-10-23 10:48:42'),(4,'guest','游客','游客用户，权限受限',1,0,'2025-10-23 10:48:42','2025-10-23 10:48:42'),(5,'manager','店长','店长拥有该店的所有修改查询权限',1,0,'2025-10-23 16:35:32','2025-10-23 16:35:32');
/*!40000 ALTER TABLE `jeez_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_role_permission`
--

DROP TABLE IF EXISTS `jeez_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `data_scope` tinyint DEFAULT '1' COMMENT '数据权限范围：1-全部数据，2-本部门数据，3-本人数据，4-自定义',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `jeez_permission` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `jeez_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_role_permission`
--

LOCK TABLES `jeez_role_permission` WRITE;
/*!40000 ALTER TABLE `jeez_role_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_sales_channel_data`
--

DROP TABLE IF EXISTS `jeez_sales_channel_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_sales_channel_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` bigint DEFAULT NULL COMMENT '门店ID',
  `channel_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '渠道名称',
  `data_date` date DEFAULT NULL COMMENT '数据日期',
  `revenue` decimal(10,2) DEFAULT NULL COMMENT '渠道收入（元）',
  `order_count` int DEFAULT NULL COMMENT '订单数量',
  `unique_visitors` int DEFAULT NULL COMMENT '访客数量',
  `conversion_rate` decimal(5,4) DEFAULT NULL COMMENT '转化率',
  `avg_order_value` decimal(10,2) DEFAULT NULL COMMENT '平均订单价值（元）',
  `customer_value` decimal(10,2) DEFAULT NULL COMMENT '客单价（元）',
  `repurchase_rate` decimal(5,4) DEFAULT NULL COMMENT '复购率',
  `growth_rate` decimal(5,4) DEFAULT NULL COMMENT '增长率',
  `status` tinyint DEFAULT '1' COMMENT '数据状态：1-有效，0-无效',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建者',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新者',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_channel_name` (`channel_name`),
  KEY `idx_data_date` (`data_date`),
  KEY `idx_store_channel` (`store_id`,`channel_name`),
  KEY `idx_store_date` (`store_id`,`data_date`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_sales_channel_data_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售渠道数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_sales_channel_data`
--

LOCK TABLES `jeez_sales_channel_data` WRITE;
/*!40000 ALTER TABLE `jeez_sales_channel_data` DISABLE KEYS */;
INSERT INTO `jeez_sales_channel_data` VALUES (1,1,'微信小程序','2025-12-01',800.00,25,150,0.1667,32.00,28.50,0.3500,0.1200,1,'微信渠道销售数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0),(2,1,'线下门店','2025-12-01',700.00,20,80,0.2500,35.00,42.00,0.4500,0.0800,1,'门店销售数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0),(3,1,'官方网站','2025-12-01',350.00,10,120,0.0833,35.00,30.00,0.2000,-0.0500,1,'官网销售数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0),(4,2,'微信小程序','2025-12-01',600.00,20,130,0.1538,30.00,26.00,0.3000,0.1000,1,'微信渠道销售数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0),(5,2,'线下门店','2025-12-01',600.00,18,70,0.2571,33.33,38.00,0.4200,0.1500,1,'门店销售数据','2025-12-18 23:18:19','2025-12-18 23:18:19',NULL,NULL,0);
/*!40000 ALTER TABLE `jeez_sales_channel_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store`
--

DROP TABLE IF EXISTS `jeez_store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '门店名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '门店编码',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '门店地址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:正常 2:暂停 3:关闭)',
  `store_type_id` tinyint NOT NULL DEFAULT '2' COMMENT '门店类型ID',
  `business_hours` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业时间(简要描述)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '门店描述',
  `area` decimal(10,2) DEFAULT NULL COMMENT '门店面积(平方米)',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `tags` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '门店标签(JSON格式)',
  `business_status` tinyint DEFAULT '1' COMMENT '营业状态(1:营业中 2:已休息 3:装修中)',
  `max_capacity` int DEFAULT '0' COMMENT '最大容纳人数',
  `rating` decimal(2,1) DEFAULT '0.0' COMMENT '评分(0-5分)',
  `review_count` int DEFAULT '0' COMMENT '评论数量',
  `is_recommended` tinyint(1) DEFAULT '0' COMMENT '是否推荐门店',
  `parent_id` bigint DEFAULT NULL COMMENT '父门店ID(用于连锁/加盟层级管理)',
  `store_level` tinyint DEFAULT '3' COMMENT '门店层级(1:总部 2:区域中心 3:普通门店)',
  `store_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '层级路径(如:/1/2/3/)',
  `group_id` bigint DEFAULT NULL COMMENT '所属分组ID(关联jeez_dict_store_group)',
  `chain_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '连锁编码(同一连锁品牌使用相同编码)',
  `business_license_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '营业执照图片URL',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '门店图片(JSON格式)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_name` (`name`),
  KEY `idx_code` (`code`),
  KEY `idx_status` (`status`),
  KEY `idx_store_type_id` (`store_type_id`),
  KEY `idx_business_status` (`business_status`),
  KEY `idx_rating` (`rating`),
  KEY `idx_is_recommended` (`is_recommended`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_longitude_latitude` (`longitude`,`latitude`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_store_level` (`store_level`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_chain_code` (`chain_code`),
  CONSTRAINT `fk_store_group` FOREIGN KEY (`group_id`) REFERENCES `jeez_dict_store_group` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_store_parent` FOREIGN KEY (`parent_id`) REFERENCES `jeez_store` (`id`) ON DELETE SET NULL,
  CONSTRAINT `jeez_store_ibfk_1` FOREIGN KEY (`store_type_id`) REFERENCES `jeez_dict_store_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store`
--

LOCK TABLES `jeez_store` WRITE;
/*!40000 ALTER TABLE `jeez_store` DISABLE KEYS */;
INSERT INTO `jeez_store` VALUES (1,'Jeez健身中心(朝阳旗舰店)','JZ_STORE_CY_001','北京市朝阳区建国路88号SOHO现代城','010-12345678',1,1,'周一至周日 06:00-22:00','大型综合性健身中心，设备齐全，环境优雅，提供全方位健身服务',1500.50,116.432321,39.928753,'[\"健身\", \"瑜伽\", \"游泳\", \"普拉提\", \"动感单车\"]',1,500,4.8,156,1,4,3,'/4/1/',5,'JEEZ',NULL,'[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]','2025-11-05 17:54:03','2025-12-12 09:56:01',1,1,0),(2,'Jeez健身中心(海淀标准店)','JZ_STORE_HD_001','北京市海淀区中关村大街1号','010-87654321',1,2,'周一至周日 05:30-23:00','科技园区健身首选，智能化设备，专业教练团队',2000.00,116.297511,39.987654,'[\"健身\", \"瑜伽\", \"动感单车\", \"团课\"]',1,300,4.6,89,0,4,3,'/4/2/',5,'JEEZ',NULL,'[\"https://example.com/image3.jpg\"]','2025-11-05 17:54:03','2025-12-12 09:56:01',1,1,0),(3,'Jeez健身中心(望京社区店)','JZ_STORE_WJ_001','北京市朝阳区望京SOHO T1','010-11223344',2,3,'周一至周五 07:00-21:00 周末 08:00-20:00','现代化设计，适合白领健身，提供瑜伽普拉提课程',1200.75,116.475832,39.997123,'[\"瑜伽\", \"普拉提\", \"轻器械\"]',3,100,4.7,45,0,4,3,'/4/3/',5,'JEEZ',NULL,'[\"https://example.com/image4.jpg\"]','2025-11-05 17:54:03','2025-12-12 09:56:01',1,1,0),(4,'Jeez健身集团总部','JZ_STORE_HQ','北京市朝阳区CBD国贸中心','010-88888888',1,1,'周一至周五 09:00-18:00','Jeez健身品牌总部，负责全国运营管理',NULL,NULL,NULL,NULL,1,0,0.0,0,0,NULL,1,'/4/',NULL,'JEEZ',NULL,NULL,'2025-12-12 09:56:01','2025-12-12 09:56:01',NULL,NULL,0);
/*!40000 ALTER TABLE `jeez_store` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_about_us`
--

DROP TABLE IF EXISTS `jeez_store_about_us`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_about_us` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标题',
  `subtitle` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '副标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关于我们正文内容',
  `content_html` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '富文本内容(HTML格式)',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '图片URL列表（JSON格式存储）',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频URL',
  `video_cover_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频封面URL',
  `features` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '门店特色（JSON格式）',
  `highlights` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '门店亮点（JSON格式）',
  `awards` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '获得荣誉（JSON格式）',
  `seo_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SEO标题',
  `seo_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SEO描述',
  `seo_keywords` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SEO关键词',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_store_about_us_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店关于我们信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_about_us`
--

LOCK TABLES `jeez_store_about_us` WRITE;
/*!40000 ALTER TABLE `jeez_store_about_us` DISABLE KEYS */;
INSERT INTO `jeez_store_about_us` VALUES (1,1,'Jeez健身 · 朝阳旗舰店','您的专属健身空间，开启健康生活新篇章','Jeez健身朝阳旗舰店位于北京市朝阳区建国路88号SOHO现代城，是Jeez健身品牌在北京的标杆门店。\n\n我们拥有1500平方米的健身空间，配备国际顶级健身设备，包括力量训练区、有氧器械区、瑜伽室、动感单车房、恒温游泳池等完善设施。\n\n专业的教练团队将为您提供一对一私教服务、团体课程、以及科学的健身方案定制。\n\n无论您是健身新手还是资深爱好者，我们都将为您提供最优质的健身体验。','<h2>关于我们</h2><p>Jeez健身朝阳旗舰店位于北京市朝阳区建国路88号SOHO现代城，是Jeez健身品牌在北京的标杆门店。</p><h3>设施介绍</h3><p>我们拥有1500平方米的健身空间，配备国际顶级健身设备。</p>','[\"https://example.com/store1/about1.jpg\", \"https://example.com/store1/about2.jpg\", \"https://example.com/store1/about3.jpg\"]',NULL,NULL,'[\"1500平方米超大空间\", \"恒温游泳池\", \"专业瑜伽室\", \"智能化设备\", \"24小时热水淋浴\"]','[\"北京市健身行业示范单位\", \"五星级服务标准\", \"环保绿色健身空间\"]','[\"2024年北京市优秀健身场馆\", \"2023年消费者信赖品牌\"]','Jeez健身朝阳旗舰店 - 北京朝阳区高端健身房','Jeez健身朝阳旗舰店，1500平方米健身空间，恒温泳池、瑜伽室、力量区一应俱全，专业教练团队为您服务。','Jeez健身,朝阳健身房,北京健身房,游泳健身,瑜伽培训',1,'2025-12-12 09:47:43','2025-12-12 09:47:43','2025-12-12 09:47:43',1,1,0),(2,2,'Jeez健身 · 海淀标准店','科技园区的健康驿站','Jeez健身海淀标准店位于中关村科技园区核心位置，专为IT从业者和白领人群打造。\n\n我们了解久坐办公的您需要什么：针对性的颈肩腰康复训练、高效的午间健身方案、以及灵活的营业时间。\n\n门店配备智能化健身设备，支持APP预约、数据记录、训练追踪等功能，让健身更科学更高效。',NULL,'[\"https://example.com/store2/about1.jpg\", \"https://example.com/store2/about2.jpg\"]',NULL,NULL,'[\"智能化健身设备\", \"APP数据追踪\", \"颈肩康复专区\", \"弹性营业时间\"]','[\"白领健身首选\", \"康复训练专业\"]','[]','Jeez健身海淀标准店 - 中关村科技园健身房','Jeez健身海淀店位于中关村核心区域，智能健身设备，专业康复训练，IT白领健身首选。','Jeez健身,海淀健身房,中关村健身,白领健身,康复训练',1,'2025-12-12 09:47:43','2025-12-12 09:47:43','2025-12-12 09:47:43',1,1,0);
/*!40000 ALTER TABLE `jeez_store_about_us` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_banner`
--

DROP TABLE IF EXISTS `jeez_store_banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'BannerID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Banner标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner图片URL',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转链接',
  `banner_type` tinyint NOT NULL DEFAULT '1' COMMENT 'Banner类型(1:首页轮播 2:活动推广 3:课程推荐 4:其他)',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序序号(数字越小越靠前)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT 'Banner状态(0:禁用 1:启用)',
  `start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `background_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '背景颜色',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述信息',
  `click_count` int DEFAULT '0' COMMENT '点击次数',
  `view_count` int DEFAULT '0' COMMENT '展示次数',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  `version` int DEFAULT '1' COMMENT '版本号(乐观锁)',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_banner_type` (`banner_type`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_start_end_time` (`start_time`,`end_time`),
  KEY `idx_created_time` (`created_time`),
  CONSTRAINT `fk_banner_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店Banner表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_banner`
--

LOCK TABLES `jeez_store_banner` WRITE;
/*!40000 ALTER TABLE `jeez_store_banner` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_store_banner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_business_hours`
--

DROP TABLE IF EXISTS `jeez_store_business_hours`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_business_hours` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `day_of_week` tinyint NOT NULL COMMENT '星期几(1:周一 2:周二 ... 7:周日)',
  `open_time` time DEFAULT NULL COMMENT '开始营业时间',
  `close_time` time DEFAULT NULL COMMENT '结束营业时间',
  `is_closed` tinyint(1) DEFAULT '0' COMMENT '是否休息',
  `special_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '特殊说明',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_day` (`store_id`,`day_of_week`),
  KEY `idx_store_id` (`store_id`),
  CONSTRAINT `jeez_store_business_hours_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店营业时间表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_business_hours`
--

LOCK TABLES `jeez_store_business_hours` WRITE;
/*!40000 ALTER TABLE `jeez_store_business_hours` DISABLE KEYS */;
INSERT INTO `jeez_store_business_hours` VALUES (1,1,1,'06:00:00','22:00:00',0,'正常营业','2025-11-05 17:54:03','2025-11-05 17:54:03'),(2,1,2,'06:00:00','22:00:00',0,'正常营业','2025-11-05 17:54:03','2025-11-05 17:54:03'),(3,1,3,'06:00:00','22:00:00',0,'正常营业','2025-11-05 17:54:03','2025-11-05 17:54:03'),(4,1,4,'06:00:00','22:00:00',0,'正常营业','2025-11-05 17:54:03','2025-11-05 17:54:03'),(5,1,5,'06:00:00','22:00:00',0,'正常营业','2025-11-05 17:54:03','2025-11-05 17:54:03'),(6,1,6,'06:00:00','22:00:00',0,'周末营业','2025-11-05 17:54:03','2025-11-05 17:54:03'),(7,1,7,'06:00:00','22:00:00',0,'周末营业','2025-11-05 17:54:03','2025-11-05 17:54:03'),(8,2,1,'05:30:00','23:00:00',0,'早开晚闭','2025-11-05 17:54:03','2025-11-05 17:54:03'),(9,2,2,'05:30:00','23:00:00',0,'早开晚闭','2025-11-05 17:54:03','2025-11-05 17:54:03'),(10,3,1,'07:00:00','21:00:00',0,'标准营业','2025-11-05 17:54:03','2025-11-05 17:54:03'),(11,3,2,'07:00:00','21:00:00',0,'标准营业','2025-11-05 17:54:03','2025-11-05 17:54:03');
/*!40000 ALTER TABLE `jeez_store_business_hours` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_employee`
--

DROP TABLE IF EXISTS `jeez_store_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `user_id` bigint NOT NULL COMMENT '用户ID(关联jeez_user表)',
  `employee_type_id` tinyint NOT NULL COMMENT '员工类型ID',
  `position_id` int NOT NULL COMMENT '职位ID',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `fire_date` date DEFAULT NULL COMMENT '离职日期',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:在职 2:离职 3:休假 4:调离)',
  `work_number` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工号',
  `contract_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同编号',
  `monthly_salary` decimal(10,2) DEFAULT NULL COMMENT '月薪',
  `hourly_rate` decimal(6,2) DEFAULT NULL COMMENT '时薪',
  `work_schedule` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工作时间安排',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_user_active` (`store_id`,`user_id`,`status`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_employee_type_id` (`employee_type_id`),
  KEY `idx_position_id` (`position_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `jeez_store_employee_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_store_employee_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_store_employee_ibfk_3` FOREIGN KEY (`employee_type_id`) REFERENCES `jeez_dict_employee_type` (`id`),
  CONSTRAINT `jeez_store_employee_ibfk_4` FOREIGN KEY (`position_id`) REFERENCES `jeez_dict_position` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店员工表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_employee`
--

LOCK TABLES `jeez_store_employee` WRITE;
/*!40000 ALTER TABLE `jeez_store_employee` DISABLE KEYS */;
INSERT INTO `jeez_store_employee` VALUES (1,1,1,1,1,'2024-01-01',NULL,1,'M001',NULL,15000.00,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(2,1,2,2,4,'2024-01-15',NULL,1,'C001',NULL,12000.00,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(3,1,3,5,6,'2024-02-01',NULL,1,'Y001',NULL,10000.00,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(4,2,2,1,1,'2024-01-01',NULL,1,'M002',NULL,14000.00,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(5,2,6,2,3,'2024-02-10',NULL,1,'C002',NULL,8000.00,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(6,3,3,1,1,'2024-01-01',NULL,1,'M003',NULL,13000.00,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0);
/*!40000 ALTER TABLE `jeez_store_employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_facility`
--

DROP TABLE IF EXISTS `jeez_store_facility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_facility` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设施ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `facility_type_id` tinyint NOT NULL COMMENT '设施类型ID',
  `facility_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设施名称',
  `facility_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设施编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '设施描述',
  `equipment_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备类型(如: 跑步机、哑铃等)',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '型号',
  `purchase_date` date DEFAULT NULL COMMENT '购买日期',
  `warranty_expiry_date` date DEFAULT NULL COMMENT '保修到期日期',
  `quantity` int DEFAULT '1' COMMENT '数量',
  `capacity` int DEFAULT NULL COMMENT '容纳人数',
  `area` decimal(8,2) DEFAULT NULL COMMENT '设施面积(平方米)',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设施位置',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:正常 2:维修中 3:停用)',
  `maintenance_date` date DEFAULT NULL COMMENT '最近维护日期',
  `next_maintenance_date` date DEFAULT NULL COMMENT '下次维护日期',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_facility_code` (`store_id`,`facility_code`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_facility_type_id` (`facility_type_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `jeez_store_facility_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_store_facility_ibfk_2` FOREIGN KEY (`facility_type_id`) REFERENCES `jeez_dict_facility_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店设施表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_facility`
--

LOCK TABLES `jeez_store_facility` WRITE;
/*!40000 ALTER TABLE `jeez_store_facility` DISABLE KEYS */;
INSERT INTO `jeez_store_facility` VALUES (1,1,1,'跑步机区','CARDIO_001','多品牌跑步机，配备心率监测系统',NULL,NULL,NULL,NULL,NULL,1,50,200.00,'1楼东侧',1,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(2,1,2,'力量训练区','STRENGTH_001','哑铃、杠铃、力量器械等',NULL,NULL,NULL,NULL,NULL,1,80,300.00,'1楼西侧',1,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(3,1,4,'瑜伽室A','YOGA_A','专业瑜伽教室，配备瑜伽垫和道具',NULL,NULL,NULL,NULL,NULL,4,30,80.00,'2楼',1,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(4,1,3,'游泳池','POOL_MAIN','25米标准恒温泳池',NULL,NULL,NULL,NULL,NULL,3,1,500.00,'3楼',1,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(5,2,1,'健身房主区域','CARDIO_MAIN','综合健身区域',NULL,NULL,NULL,NULL,NULL,1,25,150.00,'1楼',1,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(6,3,2,'轻器械区','STRENGTH_WJ','小型健身器械区域',NULL,NULL,NULL,NULL,NULL,2,1,60.00,'1楼',1,NULL,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0);
/*!40000 ALTER TABLE `jeez_store_facility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_manager`
--

DROP TABLE IF EXISTS `jeez_store_manager`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_manager` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '店长信息ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `user_id` bigint NOT NULL COMMENT '用户ID(关联jeez_user表)',
  `manager_start_date` date DEFAULT NULL COMMENT '上任日期',
  `manager_end_date` date DEFAULT NULL COMMENT '离任日期',
  `manager_status` tinyint DEFAULT '1' COMMENT '店长状态(1:在职 2:离任 3:调离)',
  `manager_type` tinyint DEFAULT '1' COMMENT '店长类型(1:正式店长 2:代理店长)',
  `contract_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '合同编号',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_active_manager` (`store_id`,`manager_status`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_manager_status` (`manager_status`),
  CONSTRAINT `jeez_store_manager_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_store_manager_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店长信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_manager`
--

LOCK TABLES `jeez_store_manager` WRITE;
/*!40000 ALTER TABLE `jeez_store_manager` DISABLE KEYS */;
INSERT INTO `jeez_store_manager` VALUES (1,1,1,'2024-01-01',NULL,1,1,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(2,2,2,'2024-01-01',NULL,1,1,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0),(3,3,3,'2024-01-01',NULL,1,1,NULL,'2025-11-05 17:54:03','2025-11-05 17:54:03',1,1,0);
/*!40000 ALTER TABLE `jeez_store_manager` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_notice`
--

DROP TABLE IF EXISTS `jeez_store_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '公告内容',
  `content_html` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '公告HTML内容',
  `subtitle` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '副标题',
  `notice_type` tinyint NOT NULL DEFAULT '1' COMMENT '公告类型：1-系统通知，2-活动公告，3-优惠信息，4-临时通知，5-其他',
  `priority` tinyint NOT NULL DEFAULT '2' COMMENT '公告优先级：1-低，2-中，3-高，4-紧急',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '公告状态：0-草稿，1-已发布，2-已撤回，3-已过期',
  `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶：0-否，1-是',
  `publish_channel` tinyint NOT NULL DEFAULT '5' COMMENT '发布方式：1-APP推送，2-门店展示，3-邮件通知，4-短信通知，5-全渠道',
  `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `read_count` int NOT NULL DEFAULT '0' COMMENT '阅读次数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞次数',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人姓名',
  `publisher_id` bigint DEFAULT NULL COMMENT '发布人ID',
  `publisher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '发布人姓名',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `image_urls` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '图片URLs（JSON数组格式）',
  `attachments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '附件URL（JSON数组格式，存储图片、文档等）',
  `target_audience` tinyint NOT NULL DEFAULT '1' COMMENT '目标用户群体：1-全部用户，2-会员，3-员工，4-VIP会员，5-特定用户组',
  `tags` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签（用于分类和搜索）',
  `seo_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SEO标题',
  `seo_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SEO描述',
  `seo_keywords` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'SEO关键词',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号（乐观锁）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标识：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_notice` (`store_id`,`title`,`created_time`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_notice_type` (`notice_type`),
  KEY `idx_status` (`status`),
  KEY `idx_is_top` (`is_top`),
  KEY `idx_effective_time` (`effective_time`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_is_deleted` (`is_deleted`),
  KEY `fk_store_notice_creator_user` (`creator_id`),
  KEY `fk_store_notice_publisher_user` (`publisher_id`),
  CONSTRAINT `fk_store_notice_creator_user` FOREIGN KEY (`creator_id`) REFERENCES `jeez_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_store_notice_publisher_user` FOREIGN KEY (`publisher_id`) REFERENCES `jeez_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_store_notice_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_notice`
--

LOCK TABLES `jeez_store_notice` WRITE;
/*!40000 ALTER TABLE `jeez_store_notice` DISABLE KEYS */;
INSERT INTO `jeez_store_notice` VALUES (1,1,'春节营业时间调整通知','尊敬的会员朋友们：\n\n为了让大家度过一个愉快的春节，本店在春节期间的营业时间调整如下：\n\n2月9日（除夕）：09:00-14:00\n2月10日（初一）：休息\n2月11日（初二）：10:00-18:00\n2月12日（初三）起恢复正常营业时间\n\n祝大家新春快乐，龙年大吉！','<h3>尊敬的会员朋友们：</h3><p>为了让大家度过一个愉快的春节，本店在春节期间的营业时间调整如下：</p><ul><li>2月9日（除夕）：09:00-14:00</li><li>2月10日（初一）：休息</li><li>2月11日（初二）：10:00-18:00</li><li>2月12日（初三）起恢复正常营业时间</li></ul><p>祝大家新春快乐，龙年大吉！</p>','春节期间营业时间安排',2,3,1,1,5,'2024-02-01 00:00:00','2024-02-15 23:59:59',157,23,1,'店长老李',1,'店长老李','2024-02-01 09:00:00','https://example.com/spring-festival.jpg','[\"https://example.com/new-year.jpg\", \"https://example.com/event.jpg\"]',2,'春节,营业时间,通知','春节营业时间调整 - Jeez健身朝阳店','Jeez健身朝阳店春节营业时间调整通知','Jeez健身,朝阳店,春节,营业时间',1,NULL,'2025-12-14 12:58:43','2025-12-14 13:25:28',1,NULL,0),(2,1,'新会员专享优惠','新年新气象！新会员专享福利来啦！\n\n凡是首次到店的新会员，即可享受：\n? 月卡立减100元\n? 季卡立减300元\n? 年卡立减1000元\n? 赠送2节私教体验课\n\n活动时间：即日起至2月29日\n快来开启你的健身之旅吧！','<h2>新年新气象！新会员专享福利来啦！</h2><p>凡是首次到店的新会员，即可享受：</p><ul><li>月卡立减100元</li><li>季卡立减300元</li><li>年卡立减1000元</li><li>赠送2节私教体验课</li></ul><p><strong>活动时间：即日起至2月29日</strong></p><p>快来开启你的健身之旅吧！</p>','首次体验立减100元',3,2,1,0,5,'2024-01-15 00:00:00','2024-02-29 23:59:59',289,45,2,'经理王芳',2,'经理王芳','2024-01-15 10:00:00','[\"https://example.com/new-member1.jpg\", \"https://example.com/new-member2.jpg\"]',NULL,1,'新会员,优惠,活动','新会员专享优惠 - Jeez健身朝阳店','Jeez健身朝阳店新会员专享优惠活动','Jeez健身,朝阳店,新会员,优惠,活动',1,NULL,'2025-12-14 12:58:43','2025-12-14 13:14:19',2,NULL,0),(3,1,'器械维护通知','各位会员：\n\n为确保大家的健身体验，本店将于以下时间进行器械维护：\n\n2月15日（周四）14:00-17:00\n维护区域：力量训练区\n\n期间部分器械可能无法使用，请各位会员合理安排训练时间。给您带来的不便，敬请谅解！','<h3>各位会员：</h3><p>为确保大家的健身体验，本店将于以下时间进行器械维护：</p><p><strong>2月15日（周四）14:00-17:00</strong></p><p><strong>维护区域：力量训练区</strong></p><p>期间部分器械可能无法使用，请各位会员合理安排训练时间。给您带来的不便，敬请谅解！</p>','部分器械临时维护中',1,2,1,0,2,'2024-02-10 00:00:00','2024-02-20 23:59:59',67,5,3,'教练小张',3,'教练小张','2024-02-10 15:00:00',NULL,NULL,2,'维护,器械,通知','器械维护通知 - Jeez健身朝阳店','Jeez健身朝阳店器械维护通知','Jeez健身,朝阳店,维护,器械',1,NULL,'2025-12-14 12:58:43','2025-12-14 12:58:43',3,NULL,0),(4,2,'瑜伽课程升级','好消息！海淀店瑜伽区全面升级完成！\n\n新增设施：\n? 专业瑜伽教室2间\n? 进口瑜伽垫和辅具\n? 恒温恒湿系统\n? 独立更衣室和淋浴间\n\n新课程安排：\n? 哈他瑜伽（初级/中级）\n? 流瑜伽\n? 阴瑜伽\n? 热瑜伽\n\n欢迎会员朋友们预约体验！','<h2>好消息！海淀店瑜伽区全面升级完成！</h2><h3>新增设施：</h3><ul><li>专业瑜伽教室2间</li><li>进口瑜伽垫和辅具</li><li>恒温恒湿系统</li><li>独立更衣室和淋浴间</li></ul><h3>新课程安排：</h3><ul><li>哈他瑜伽（初级/中级）</li><li>流瑜伽</li><li>阴瑜伽</li><li>热瑜伽</li></ul><p>欢迎会员朋友们预约体验！</p>','全新瑜伽教室正式开放',2,3,1,1,5,'2024-02-01 00:00:00','2024-12-31 23:59:59',412,89,1,'店长老李',1,'店长老李','2024-02-01 08:00:00','https://example.com/yoga-room.jpg,https://example.com/yoga-class.jpg',NULL,2,'[\"瑜伽\", \"课程\", \"升级\", \"新教室\"]','瑜伽课程升级 - Jeez健身海淀店','Jeez健身海淀店瑜伽教室全面升级，新课程开放预约','Jeez健身,海淀店,瑜伽,课程升级',1,NULL,'2025-12-14 12:58:43','2025-12-14 13:14:19',1,NULL,0),(5,2,'会员生日福利','亲爱的会员朋友们：\n\n为了感谢大家一直以来的支持，本店推出会员生日福利活动：\n\n生日当月可享受：\n? 免费健身30天\n? 赠送生日礼品一份\n? 私教课程8折优惠\n? 可带3位朋友免费体验\n\n请在生日当月到前台领取您的生日礼包！','<h2>亲爱的会员朋友们：</h2><p>为了感谢大家一直以来的支持，本店推出会员生日福利活动：</p><h3>生日当月可享受：</h3><ul><li>免费健身30天</li><li>赠送生日礼品一份</li><li>私教课程8折优惠</li><li>可带3位朋友免费体验</li></ul><p>请在生日当月到前台领取您的生日礼包！</p>','生日当月免费健身',3,2,1,0,3,'2024-01-01 00:00:00','2024-12-31 23:59:59',198,34,2,'经理王芳',2,'经理王芳','2024-01-01 09:00:00','https://example.com/birthday-gift.jpg',NULL,2,'生日,福利,会员','会员生日福利 - Jeez健身海淀店','Jeez健身海淀店会员生日当月免费健身福利','Jeez健身,海淀店,会员,生日,福利',1,NULL,'2025-12-14 12:58:43','2025-12-14 12:58:43',2,NULL,0),(6,3,'游泳池开放通知','五角店游泳馆正式开放啦！\n\n游泳池规格：\n? 25米标准泳道\n? 恒温28-30度\n? 专业水处理系统\n? 救生员全程值守\n\n开放时间：\n周一至周五：06:00-22:00\n周六周日：07:00-21:00\n\n欢迎各位会员前来体验！','<h2>五角店游泳馆正式开放啦！</h2><h3>游泳池规格：</h3><ul><li>25米标准泳道</li><li>恒温28-30度</li><li>专业水处理系统</li><li>救生员全程值守</li></ul><h3>开放时间：</h3><p><strong>周一至周五：06:00-22:00</strong></p><p><strong>周六周日：07:00-21:00</strong></p><p>欢迎各位会员前来体验！</p>','恒温游泳池正式开放',2,4,1,1,5,'2024-02-15 00:00:00',NULL,523,112,1,'店长老李',1,'店长老李','2024-02-15 08:30:00','https://example.com/swimming-pool.jpg',NULL,2,'[\"游泳\", \"开放\", \"通知\", \"新设施\"]','游泳池开放 - Jeez健身五角场店','Jeez健身五角店恒温游泳池正式开放','Jeez健身,五角场店,游泳,游泳池',1,NULL,'2025-12-14 12:58:43','2025-12-14 13:14:19',1,NULL,0),(7,4,'系统升级维护','尊敬的各位会员：\n\n为了提供更好的服务体验，我们将于以下时间进行系统升级：\n\n升级时间：2月20日（周二）02:00-06:00\n\n影响范围：\n? 会员卡刷卡\n? 课程预约\n? 储值消费\n\n请各位会员提前安排好健身计划。升级期间如有紧急情况，请联系前台工作人员。\n\n感谢您的理解与支持！','<h3>尊敬的各位会员：</h3><p>为了提供更好的服务体验，我们将于以下时间进行系统升级：</p><p><strong>升级时间：2月20日（周二）02:00-06:00</strong></p><h4>影响范围：</h4><ul><li>会员卡刷卡</li><li>课程预约</li><li>储值消费</li></ul><p>请各位会员提前安排好健身计划。升级期间如有紧急情况，请联系前台工作人员。</p><p>感谢您的理解与支持！</p>','会员系统将于2月20日凌晨升级',1,4,1,1,4,'2024-02-10 00:00:00','2024-02-25 23:59:59',856,67,1,'店长老李',1,'店长老李','2024-02-10 10:00:00',NULL,NULL,1,'系统,升级,维护','系统升级维护 - Jeez健身','Jeez健身会员系统升级维护通知','Jeez健身,系统升级,维护',1,NULL,'2025-12-14 12:58:43','2025-12-14 12:58:43',1,NULL,0);
/*!40000 ALTER TABLE `jeez_store_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_pricing_strategy`
--

DROP TABLE IF EXISTS `jeez_store_pricing_strategy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_pricing_strategy` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `strategy_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '策略名称',
  `strategy_type_id` tinyint NOT NULL COMMENT '策略类型ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '策略描述',
  `base_price_multiplier` decimal(5,3) DEFAULT '1.000' COMMENT '基础价格倍数',
  `service_categories` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '适用服务类别(JSON格式)',
  `customer_segments` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '适用客户群体(JSON格式)',
  `time_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '时间规则(JSON格式)',
  `quantity_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '数量规则(JSON格式)',
  `discount_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '折扣规则(JSON格式)',
  `special_conditions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '特殊条件(JSON格式)',
  `priority_level` int DEFAULT '1' COMMENT '优先级(数字越大优先级越高)',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `expiry_date` date DEFAULT NULL COMMENT '过期日期',
  `approval_status` tinyint DEFAULT '1' COMMENT '审批状态(1:待审批 2:已批准 3:已拒绝)',
  `approved_by` bigint DEFAULT NULL COMMENT '审批人ID',
  `approved_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approval_notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_strategy_type_id` (`strategy_type_id`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_effective_date` (`effective_date`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_priority_level` (`priority_level`),
  KEY `approved_by` (`approved_by`),
  CONSTRAINT `jeez_store_pricing_strategy_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_store_pricing_strategy_ibfk_2` FOREIGN KEY (`strategy_type_id`) REFERENCES `jeez_dict_pricing_strategy_type` (`id`),
  CONSTRAINT `jeez_store_pricing_strategy_ibfk_3` FOREIGN KEY (`approved_by`) REFERENCES `jeez_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺价格策略表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_pricing_strategy`
--

LOCK TABLES `jeez_store_pricing_strategy` WRITE;
/*!40000 ALTER TABLE `jeez_store_pricing_strategy` DISABLE KEYS */;
INSERT INTO `jeez_store_pricing_strategy` VALUES (1,1,'朝阳旗舰店高端定价策略',3,'针对高端客户群体的高品质服务定价策略',1.500,'[\"私教课程\", \"特色课程\", \"VIP服务\"]','[\"VIP会员\", \"高端商务人士\"]','{\"peak_hours\": {\"surcharge\": 0.2, \"times\": [\"18:00-21:00\"]}}','{\"bulk_discount\": {\"threshold\": 10, \"discount\": 0.1}}','{\"membership_discount\": {\"VIP\": 0.15, \"Premium\": 0.1}}',NULL,3,1,'2023-01-01',NULL,2,1,'2023-01-01 09:00:00',NULL,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(2,2,'海淀标准店竞争定价策略',2,'根据周边竞争对手制定的有竞争力价格',1.200,'[\"基础健身\", \"团体课程\"]','[\"学生\", \"上班族\", \"普通会员\"]','{\"weekday_discount\": {\"rate\": 0.1, \"days\": [\"周一至周五\"]}}','{\"session_package\": {\"sessions\": 20, \"discount\": 0.15}}','{\"early_bird\": {\"hours\": 6, \"discount\": 0.2}}',NULL,2,1,'2023-01-01',NULL,2,1,'2023-01-01 10:00:00',NULL,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(3,3,'望京社区店亲民定价策略',1,'社区导向的亲民价格策略',1.000,'[\"基础服务\", \"社区活动\"]','[\"社区居民\", \"体验用户\"]','{\"community_hours\": {\"discount\": 0.15, \"times\": [\"09:00-17:00\"]}}','{\"group_discount\": {\"min_people\": 3, \"discount\": 0.2}}','{\"resident_discount\": {\"verification\": \"required\", \"discount\": 0.1}}',NULL,1,1,'2023-01-01',NULL,2,1,'2023-01-01 11:00:00',NULL,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0);
/*!40000 ALTER TABLE `jeez_store_pricing_strategy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_service`
--

DROP TABLE IF EXISTS `jeez_store_service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_service` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `service_type` tinyint NOT NULL COMMENT '服务类型(1:共享充电宝 2:自动贩卖机 3:休息区 4:淋浴服务 5:储物柜 6:WiFi服务 7:停车服务 8:其他)',
  `service_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务名称',
  `service_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务编码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '服务描述',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '服务价格(0表示免费)',
  `price_unit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '价格单位(次/小时/月)',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务位置',
  `availability_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务可用时间(如: 06:00-22:00)',
  `capacity` int DEFAULT NULL COMMENT '容量/数量',
  `current_usage` int DEFAULT '0' COMMENT '当前使用量',
  `is_free_for_members` tinyint(1) DEFAULT '0' COMMENT '是否会员免费',
  `member_discount` decimal(5,2) DEFAULT '1.00' COMMENT '会员折扣率',
  `requires_booking` tinyint(1) DEFAULT '0' COMMENT '是否需要预约',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:正常 2:维护中 3:停用)',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_service_type` (`service_type`),
  KEY `idx_status` (`status`),
  KEY `idx_is_deleted` (`is_deleted`),
  CONSTRAINT `fk_store_service_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店服务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_service`
--

LOCK TABLES `jeez_store_service` WRITE;
/*!40000 ALTER TABLE `jeez_store_service` DISABLE KEYS */;
INSERT INTO `jeez_store_service` VALUES (1,1,1,'共享充电宝','SVC_CHARGER_001','街电共享充电宝服务，扫码即借',2.00,'小时','前台旁','06:00-22:00',20,0,0,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(2,1,2,'自动贩卖机','SVC_VENDING_001','饮料、健康零食自动贩卖机',0.00,'次','休息区','24小时',1,0,0,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(3,1,3,'休息区','SVC_REST_001','舒适的休息等候区域',0.00,'次','一楼大厅','06:00-22:00',30,0,1,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(4,1,4,'淋浴服务','SVC_SHOWER_001','独立淋浴间，提供洗浴用品',0.00,'次','更衣室','06:00-22:00',20,0,1,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(5,1,5,'智能储物柜','SVC_LOCKER_001','密码锁储物柜，免费使用',0.00,'次','更衣室','06:00-22:00',200,0,1,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(6,1,6,'免费WiFi','SVC_WIFI_001','高速无线网络覆盖',0.00,'次','全馆','24小时',NULL,0,1,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(7,1,7,'地下停车场','SVC_PARKING_001','会员享受2小时免费停车',5.00,'小时','B1层','24小时',100,0,0,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(8,2,1,'共享充电宝','SVC_CHARGER_002','小电共享充电宝',1.50,'小时','前台','05:30-23:00',15,0,0,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(9,2,3,'休息区','SVC_REST_002','小型休息区',0.00,'次','入口处','05:30-23:00',15,0,1,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(10,2,4,'淋浴服务','SVC_SHOWER_002','淋浴间',0.00,'次','更衣室','05:30-23:00',10,0,1,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(11,2,5,'储物柜','SVC_LOCKER_002','密码储物柜',0.00,'次','更衣室','05:30-23:00',100,0,1,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0),(12,2,6,'免费WiFi','SVC_WIFI_002','无线网络',0.00,'次','全馆','24小时',NULL,0,1,1.00,0,1,0,'2025-12-12 09:43:26','2025-12-12 09:43:26',1,1,0);
/*!40000 ALTER TABLE `jeez_store_service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_store_service_price`
--

DROP TABLE IF EXISTS `jeez_store_service_price`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_store_service_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '价格ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `service_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务类型',
  `service_id` bigint DEFAULT NULL COMMENT '服务ID',
  `service_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务名称',
  `base_price` decimal(10,2) NOT NULL COMMENT '基础价格',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'CNY' COMMENT '货币单位',
  `billing_unit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '计价单位',
  `duration_unit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '时长单位',
  `price_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '价格说明',
  `cost_components` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '成本构成(JSON格式)',
  `profit_margin` decimal(5,2) DEFAULT NULL COMMENT '利润率(%)',
  `market_positioning` tinyint DEFAULT '2' COMMENT '市场定位(1:低端 2:中端 3:高端)',
  `competitor_price` decimal(10,2) DEFAULT NULL COMMENT '竞品价格',
  `price_change_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '价格变更历史(JSON格式)',
  `last_price_change_date` date DEFAULT NULL COMMENT '最后调价日期',
  `next_review_date` date DEFAULT NULL COMMENT '下次评估日期',
  `special_pricing_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '特殊定价规则(JSON格式)',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:有效 2:暂停 3:即将调整)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_service_type` (`service_type`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_base_price` (`base_price`),
  KEY `idx_status` (`status`),
  KEY `idx_market_positioning` (`market_positioning`),
  KEY `idx_next_review_date` (`next_review_date`),
  CONSTRAINT `jeez_store_service_price_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店服务价格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_store_service_price`
--

LOCK TABLES `jeez_store_service_price` WRITE;
/*!40000 ALTER TABLE `jeez_store_service_price` DISABLE KEYS */;
INSERT INTO `jeez_store_service_price` VALUES (1,1,'private_training',2,'一对一私教课程',450.00,'CNY','次','60分钟','专业私人教练一对一指导','{\"coach_fee\": 270, \"facility_fee\": 90, \"equipment_fee\": 45, \"overhead\": 45}',40.00,3,480.00,NULL,NULL,'2023-12-31',NULL,1,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(2,1,'group_class',1,'瑜伽团体课程',120.00,'CNY','次','60分钟','专业瑜伽教练团体指导','{\"coach_fee\": 60, \"facility_fee\": 30, \"equipment_fee\": 15, \"overhead\": 15}',33.30,2,100.00,NULL,NULL,'2023-12-31',NULL,1,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(3,2,'private_training',2,'一对一私教课程',320.00,'CNY','次','60分钟','专业私人教练一对一指导','{\"coach_fee\": 192, \"facility_fee\": 64, \"equipment_fee\": 32, \"overhead\": 32}',37.50,2,350.00,NULL,NULL,'2023-12-31',NULL,1,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0),(4,3,'group_class',1,'基础健身课程',80.00,'CNY','次','45分钟','基础健身团体课程','{\"coach_fee\": 40, \"facility_fee\": 20, \"equipment_fee\": 10, \"overhead\": 10}',37.50,1,90.00,NULL,NULL,'2023-12-31',NULL,1,'2025-11-05 18:28:16','2025-11-05 18:28:16',1,1,0);
/*!40000 ALTER TABLE `jeez_store_service_price` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_system_log`
--

DROP TABLE IF EXISTS `jeez_system_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_system_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模块名称',
  `action` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作动作',
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作者名称',
  `user_id` bigint DEFAULT NULL COMMENT '操作者用户ID',
  `status` tinyint DEFAULT NULL COMMENT '操作状态：1-成功，0-失败',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `created_at` datetime DEFAULT NULL COMMENT '操作时间',
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '操作详情',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_module` (`module`),
  KEY `idx_action` (`action`),
  KEY `idx_operator` (`operator`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_deleted` (`deleted`),
  KEY `idx_module_action` (`module`,`action`),
  KEY `idx_operator_created` (`operator`,`created_at`),
  CONSTRAINT `fk_system_log_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_system_log`
--

LOCK TABLES `jeez_system_log` WRITE;
/*!40000 ALTER TABLE `jeez_system_log` DISABLE KEYS */;
INSERT INTO `jeez_system_log` VALUES (1,'用户管理','用户登录','admin',NULL,1,'192.168.1.100','2025-12-18 09:00:00','管理员admin登录系统','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(2,'用户管理','用户登录','operator1',NULL,1,'192.168.1.101','2025-12-18 09:05:00','操作员operator1登录系统','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(3,'用户管理','用户登录','admin',NULL,1,'192.168.1.100','2025-12-18 10:30:00','管理员admin再次登录系统','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(4,'用户管理','用户退出','admin',NULL,1,'192.168.1.100','2025-12-18 12:00:00','管理员admin退出系统','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(5,'订单管理','查询订单','operator1',NULL,1,'192.168.1.101','2025-12-18 09:15:00','操作员operator1查询订单列表','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(6,'会员管理','新增会员','operator1',NULL,1,'192.168.1.101','2025-12-18 09:30:00','操作员operator1新增会员：张三','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(7,'用户管理','用户登录','admin',NULL,0,'192.168.1.102','2025-12-18 11:00:00','用户尝试登录失败：密码错误','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(8,'财务管理','查看报表','admin',NULL,1,'192.168.1.100','2025-12-18 14:00:00','管理员查看财务报表','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(9,'课程管理','创建课程','operator2',NULL,1,'192.168.1.103','2025-12-18 15:00:00','操作员operator2创建新课程：瑜伽初级班','2025-12-18 23:18:19','2025-12-18 23:18:19',0),(10,'用户管理','修改密码','admin',NULL,1,'192.168.1.100','2025-12-18 16:00:00','管理员修改密码','2025-12-18 23:18:19','2025-12-18 23:18:19',0);
/*!40000 ALTER TABLE `jeez_system_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_third_party_config`
--

DROP TABLE IF EXISTS `jeez_third_party_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_third_party_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台类型:MEITUAN-美团,DOUYIN-抖音',
  `store_id` bigint NOT NULL COMMENT '关联的门店ID(关联jeez_store表)',
  `app_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台应用ID(AppKey/ClientKey)',
  `app_secret` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台应用密钥(AppSecret/ClientSecret)',
  `access_token` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '访问令牌',
  `token_expire_time` datetime DEFAULT NULL COMMENT '令牌过期时间',
  `refresh_token` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '刷新令牌',
  `poi_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台门店ID(美团shopId/抖音poi_id)',
  `account_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台账户ID(美团accountId/抖音open_id)',
  `auth_status` tinyint DEFAULT '0' COMMENT '授权状态:0-未授权,1-已授权,2-授权过期',
  `auth_time` datetime DEFAULT NULL COMMENT '授权时间',
  `auth_scope` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '授权范围(逗号分隔)',
  `callback_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回调地址',
  `sign_key` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '签名密钥',
  `extra_config` json DEFAULT NULL COMMENT '扩展配置(JSON格式)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-禁用,1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除:0-否,1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_store` (`platform`,`store_id`,`is_deleted`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_platform` (`platform`),
  KEY `idx_poi_id` (`poi_id`),
  CONSTRAINT `fk_third_party_config_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方平台配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_third_party_config`
--

LOCK TABLES `jeez_third_party_config` WRITE;
/*!40000 ALTER TABLE `jeez_third_party_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_third_party_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_third_party_verify_record`
--

DROP TABLE IF EXISTS `jeez_third_party_verify_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_third_party_verify_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台类型:MEITUAN-美团,DOUYIN-抖音',
  `coupon_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '券码(用户出示的券码)',
  `encrypted_code` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加密券码(部分平台返回的加密数据)',
  `verify_token` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '验券Token(抖音验券准备接口返回)',
  `store_id` bigint NOT NULL COMMENT '核销门店ID(关联jeez_store表)',
  `poi_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '平台门店ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID(员工ID)',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人姓名',
  `verify_time` datetime DEFAULT NULL COMMENT '核销时间',
  `verify_status` tinyint DEFAULT '0' COMMENT '核销状态:0-待核销,1-核销成功,2-核销失败,3-已撤销',
  `member_id` bigint DEFAULT NULL COMMENT '关联的会员ID(关联jeez_member表)',
  `member_card_id` bigint DEFAULT NULL COMMENT '关联的会员卡ID(核销成功后发放的会员卡,关联jeez_member_card表)',
  `third_party_order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方订单号',
  `third_party_trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方交易流水号',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '团购商品名称',
  `product_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '团购商品ID',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '团购原价',
  `sale_price` decimal(10,2) DEFAULT NULL COMMENT '团购售价',
  `verify_count` int DEFAULT '1' COMMENT '核销数量',
  `coupon_info` json DEFAULT NULL COMMENT '券码详情(JSON格式，存储第三方返回的完整信息)',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `error_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误码',
  `response_data` json DEFAULT NULL COMMENT '第三方响应数据(JSON格式)',
  `cancel_time` datetime DEFAULT NULL COMMENT '撤销时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '撤销原因',
  `cancel_operator_id` bigint DEFAULT NULL COMMENT '撤销操作人ID',
  `verify_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'SCAN' COMMENT '核销来源:SCAN-扫码核销,INPUT-输入核销,BATCH-批量核销',
  `device_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备信息',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除:0-否,1-是',
  PRIMARY KEY (`id`),
  KEY `idx_coupon_code` (`coupon_code`),
  KEY `idx_platform` (`platform`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_member_id` (`member_id`),
  KEY `idx_verify_time` (`verify_time`),
  KEY `idx_verify_status` (`verify_status`),
  KEY `idx_third_party_order` (`third_party_order_no`),
  KEY `fk_third_party_verify_member_card` (`member_card_id`),
  CONSTRAINT `fk_third_party_verify_member` FOREIGN KEY (`member_id`) REFERENCES `jeez_member` (`id`),
  CONSTRAINT `fk_third_party_verify_member_card` FOREIGN KEY (`member_card_id`) REFERENCES `jeez_member_card` (`id`),
  CONSTRAINT `fk_third_party_verify_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方券核销记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_third_party_verify_record`
--

LOCK TABLES `jeez_third_party_verify_record` WRITE;
/*!40000 ALTER TABLE `jeez_third_party_verify_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_third_party_verify_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_time_slot_template`
--

DROP TABLE IF EXISTS `jeez_time_slot_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_time_slot_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `coach_id` bigint NOT NULL COMMENT '教练ID(关联jeez_user表)',
  `template_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `day_of_week` tinyint NOT NULL COMMENT '星期几(1:周一 2:周二 ... 7:周日)',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `time_slot_duration` int DEFAULT '60' COMMENT '时间段长度(分钟)',
  `max_bookings` int DEFAULT '1' COMMENT '最大预约数量',
  `booking_type` tinyint DEFAULT '1' COMMENT '预约类型(1:一对一 2:小组课 3:团体课)',
  `price_per_session` decimal(8,2) DEFAULT NULL COMMENT '单次课程价格',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上课地点',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '课程要求',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `effective_start_date` date DEFAULT NULL COMMENT '有效开始日期',
  `effective_end_date` date DEFAULT NULL COMMENT '有效结束日期',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  KEY `idx_coach_id` (`coach_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_day_of_week` (`day_of_week`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_effective_dates` (`effective_start_date`,`effective_end_date`),
  CONSTRAINT `jeez_time_slot_template_ibfk_1` FOREIGN KEY (`coach_id`) REFERENCES `jeez_user` (`id`),
  CONSTRAINT `jeez_time_slot_template_ibfk_2` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间段模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_time_slot_template`
--

LOCK TABLES `jeez_time_slot_template` WRITE;
/*!40000 ALTER TABLE `jeez_time_slot_template` DISABLE KEYS */;
INSERT INTO `jeez_time_slot_template` VALUES (1,2,'工作日上午私教时段',1,1,'09:00:00','12:00:00',60,1,1,300.00,'力量训练区','适合增肌减脂',1,NULL,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(2,2,'工作日上午私教时段',1,2,'09:00:00','12:00:00',60,1,1,300.00,'力量训练区','适合增肌减脂',1,NULL,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(3,2,'工作日上午私教时段',1,3,'09:00:00','12:00:00',60,1,1,300.00,'力量训练区','适合增肌减脂',1,NULL,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',2,2,0),(4,3,'每日早瑜伽时段',1,1,'07:00:00','09:00:00',60,3,2,88.00,'瑜伽室A','适合所有水平',1,NULL,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',3,3,0),(5,3,'每日早瑜伽时段',1,2,'07:00:00','09:00:00',60,3,2,88.00,'瑜伽室A','适合所有水平',1,NULL,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',3,3,0),(6,3,'每日早瑜伽时段',1,3,'07:00:00','09:00:00',60,3,2,88.00,'瑜伽室A','适合所有水平',1,NULL,NULL,'2025-11-05 17:54:22','2025-11-05 17:54:22',3,3,0);
/*!40000 ALTER TABLE `jeez_time_slot_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_trial_card_template`
--

DROP TABLE IF EXISTS `jeez_trial_card_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_trial_card_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板编码',
  `store_id` bigint DEFAULT NULL COMMENT '适用门店ID',
  `card_type_id` bigint NOT NULL COMMENT '关联卡种ID',
  `duration_days` int NOT NULL COMMENT '体验天数',
  `max_usage_times` int DEFAULT NULL COMMENT '最大使用次数',
  `daily_access_limit` int DEFAULT NULL COMMENT '每日访问次数限制',
  `time_restrictions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '时间限制(JSON格式)',
  `facility_access` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '可使用设施(JSON格式)',
  `class_access` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '可参加课程(JSON格式)',
  `restrictions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '使用限制(JSON格式)',
  `benefits` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '体验权益(JSON格式)',
  `welcome_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '欢迎信息',
  `terms_conditions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '条款说明',
  `is_auto_activate` tinyint(1) DEFAULT '1' COMMENT '是否自动激活',
  `grace_period_hours` int DEFAULT '24' COMMENT '宽限期(小时)',
  `follow_up_required` tinyint(1) DEFAULT '0' COMMENT '是否需要跟进',
  `conversion_incentive` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '转化激励(JSON格式)',
  `status` tinyint DEFAULT '1' COMMENT '状态(1:启用 0:禁用)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除(0:否 1:是)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `template_code` (`template_code`),
  KEY `idx_template_code` (`template_code`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_card_type_id` (`card_type_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `jeez_trial_card_template_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`),
  CONSTRAINT `jeez_trial_card_template_ibfk_2` FOREIGN KEY (`card_type_id`) REFERENCES `jeez_membership_card_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='体验卡模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_trial_card_template`
--

LOCK TABLES `jeez_trial_card_template` WRITE;
/*!40000 ALTER TABLE `jeez_trial_card_template` DISABLE KEYS */;
INSERT INTO `jeez_trial_card_template` VALUES (1,'7天基础体验卡','TRIAL_BASIC_7D',NULL,5,7,NULL,1,'{\"start_time\": \"06:00\", \"end_time\": \"22:00\", \"allowed_days\": \"1,2,3,4,5,6,7\"}','[\"力量训练区\", \"有氧器械区\", \"淋浴房\"]','[\"团体课基础课程\"]','{\"no_guest_pass\": true, \"no_private_training\": true}','{\"facilities_access\": true, \"group_class\": true, \"shower_access\": true}','欢迎体验Jeez健身！享受7天无限次健身体验。','本体验卡仅限新用户首次使用，不可转让，不可延期。',1,24,0,NULL,1,'2025-11-05 18:24:26','2025-11-05 18:24:26',1,1,0),(2,'3次瑜伽体验卡','TRIAL_YOGA_3',1,1,30,3,1,'{\"start_time\": \"06:00\", \"end_time\": \"21:00\"}','[\"瑜伽室\"]','[\"哈他瑜伽\", \"流瑜伽\", \"阴瑜伽\"]','{\"new_members_only\": true, \"booking_required\": true}','{\"yoga_classes\": 3, \"equipment_rental\": true}','开启您的瑜伽之旅！3节专业瑜伽课程等您体验。','需提前24小时预约课程，请穿着舒适运动服装。',1,24,0,NULL,1,'2025-11-05 18:24:26','2025-11-05 18:24:26',1,1,0),(3,'单日通行证','PASS_1DAY',1,5,1,1,1,'{\"start_time\": \"06:00\", \"end_time\": \"22:00\"}','[\"基础器械区\", \"有氧器械区\"]','[]','{\"no_class_access\": true, \"id_required\": true}','{\"basic_facilities\": true, \"day_access\": true}','单日健身通行证，体验基础健身设施。','需携带有效身份证件登记，仅限当日使用。',1,24,0,NULL,1,'2025-11-05 18:24:26','2025-11-05 18:24:26',1,1,0);
/*!40000 ALTER TABLE `jeez_trial_card_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_user`
--

DROP TABLE IF EXISTS `jeez_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱地址',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码（加密后）',
  `nickname` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '头像（支持URL或Base64）',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0-未知 1-男 2-女',
  `wechat_openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信 OpenID',
  `wechat_unionid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信 UnionID',
  `status` tinyint DEFAULT '1' COMMENT '用户状态 0-禁用 1-正常',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_user`
--

LOCK TABLES `jeez_user` WRITE;
/*!40000 ALTER TABLE `jeez_user` DISABLE KEYS */;
INSERT INTO `jeez_user` VALUES (1,'13800138001',NULL,'$2a$10$Sv3VEVe6G9cHv3TnqJ8hqu9Jnr1pqSBp/z9ba4EXeDp4RCV8FuLAS','张店长',NULL,1,NULL,NULL,1,0,'2025-10-23 10:48:42','2025-10-23 14:56:45'),(2,'13800138002',NULL,'$2a$10$Sv3VEVe6G9cHv3TnqJ8hqu9Jnr1pqSBp/z9ba4EXeDp4RCV8FuLAS','李教练',NULL,2,NULL,NULL,1,0,'2025-10-23 10:48:42','2025-10-23 14:56:45'),(3,'13800138003',NULL,'$2a$10$Sv3VEVe6G9cHv3TnqJ8hqu9Jnr1pqSBp/z9ba4EXeDp4RCV8FuLAS','王会员',NULL,2,NULL,NULL,1,0,'2025-10-23 10:48:42','2025-10-23 14:56:45'),(4,'13800138000',NULL,'$2a$10$Sv3VEVe6G9cHv3TnqJ8hqu9Jnr1pqSBp/z9ba4EXeDp4RCV8FuLAS','小夏','data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAIAAAAiOjnJAAAIcklEQVR4Xu3deXfUVByHcd7/m5hSoKC1KHuhclhkUxFc2BS0gggomzBb22kn3mS4IU3m3kna+Q69yfM7n7/qFOXkMcncZDL7olYbmLp9xR8Bu0dYkCAsSBAWJAgLEoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQYKwIEFYkCAsSBAWJAgLEoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQYKwIEFYkCAsSBAWJAgLEoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQYKwIEFYkCAsSBAWJAgLEoQFifqGtdKP3m9Fq4Po5np0thcd7uRfoPBgI3LNnY38i2utvmH9uJ7ftJ1h9HgQ3Uo6K75+KgjLqm9Yf2/mN206/27mXzwthGXVN6z1YX7TpvPzev7F00JYVk3DOtrNb9fsrHAolKtpWNfW8ts1HbMjW+7lf1hmvlmL/+RFb7LTmuLfKDQ1DeuPQX5TpfNqKzqxozgIq4qahtV1n2CZQxJh6dUxrCXvtj/fJ6wZqGNYN7wnWAsdwpqBOob116QVLMLSq11Y8+3IfeIeL7u3CGsWahfWWe9SwvFu/JrdhNVK2nX51b2OdXcj/+J595uM4t8rNLUL65570/aGH16zP9nxFF3q538lO6dKLKtWXSAlrGB0HJvKzMNxmzY1145eb+V/JZ0/B/nXj0VYVr3C8h/jLvTzr8+67n4vad4MfJEcQyciLKteYf1UuFUmHbMzOuS+JetwJ1pzbOOoykVrwrLqFdY797Gsa0+wxvKcdJszs4NJkZ7bJXTzrX3HEJoaheU/DnrCMr/oaeaq3bSEVUWNwrrj3utE3rBeuhdUzen8nH0ZYVVRl7Dm3Ocro3GFddV9zm7mTGaJgbCqqEtYE2+xGhuWOZ3vu3PJLTEQVhV1Cet3z3WcZMaGddfffCwuMRBWFbUIy+x43KdJH6YY1jHvOXtxiYGwqqhFWJ61zXSKYb1wx5guMWR91Y1b9Ft17zgfDfIvNlwH4vQFs/k4pEAtwnrlXr5KJxfWZe9lwSs73U+wQGqFH9Zx7/JVOtmwzN6o59ii0fYlhqoIywo/rIfuo092smH5V7yySwxVEZYVeFgLJU7bR5OGtdSNrxu6ZuJdDOcmrWtMZYr/3tAEHtZN91Xn3KRhfe09u3LN260Pv05Y5YQc1v521HYcSopDWLMVclj+Gz5zQ1izFXJYZVYZ0iGs2Qo2rDMVNzBhzVawYXmezjB2CGu2gg3Lc/147OwyrDc2rLnCR7h28/Evl+LfNzTBhuW/j6o4aVhml7OZ3LwwlutdZsmHAFZdIK2vYMM6UfGQlLtW6PLIcYR9RljVBBuWOV649i5jp2RYrtsTnkxakR8hLCvYsIz/drTc4PfEEdaqDatSzTue9BMcwQo5rOIbw8eFn6QzNqzlXnR7+w19zx2XHn+z+xvCKifksH7YfqGwM/StBRTDurYWX43uD+NLQ+kPXXvBe4RVTchhrWxfODAbw7Nqmg1rbvudM9mHKLvuP/7e7tgc/3zKQ1if0uedj1vi3Va84ykT1sFO/kQqvVXmUOYPzE363AfCKifksFqZHcylZMNPDMu0+KZwsDM/GN1afsr96yfsJ3YIq5zAw/onOdd+aZfF/WF57ki+mRzprrgXXRfshxocf8CUh7A+sdG6UfpINH9Y5gVvC7ur0bST+9xdq1AbmfMzc0y86Ob5Ah9z/C2+3qXkU5P2sMDDurH2cYWpVSIszx2n5/vO7F7bPeJErjQjFkjDYvZV2f+5J4aVPd/PTfHcK50HpZsgLCvwsHImhmU8cx+tXHPZ+yjALMKymhfWheq3zYx2iv5vFJv63A07xOaFtb/tfG84dvr2FwmriuaF1Rr3rb6eeWzfHBBWFY0M60jpj7mauWaXlAirikaG1fLeQ5ydoV2UbxFWNU0Na8n7cKx0sjeOElYVTQ2r5b4LOTvZSyuEVUWDw1r0Ph0kylycHjnYyV94KZrWJR3jWNhXdRocVsv7jU5R8qGd5YqPNGKB1Gp2WE/dO5jRbCbXEIu/6EJYVoPDKv9MEfMWsvhI0rEIy2pqWEe8T3gvjvn162uTP6NMWFYjw5pr+86yPdMbxqv2R92n1YRlNTKs21Uu6YydN1vRLxvxYyByX1VHWFbzwrpY+tSq5LS34jcBdzfiY+WFfvzfYHZp5lC70IkOdLZ9tqxJGhbW6eSJIAFNu/TNq3tMk8I62YvvXvdPpTtqZjC3Ct+8EojGhDUYTq5qkNzTdz35hPQemUX3G4W9rTFhlZn0G5HMn9PeA3GVfCjXnkRYdl5sbvumk/nkfsBPe0L2XcCfLiSsZPrD6LNxa+vmzZ3Jy/MF99JZCvU4GDUorNGTIMeOyeas92LzfLJIsTqIT9RmNu9DfT840piwzLtC1wpW+iSZieaTR5iafdjzTXlk5T/MuCc1KaxW4ZFa0S6231xyG+pKLz7lv78Rf+bCnGub3czE954lZ/SYk2A1LKzW9htHnwx2/tWEHubPPJB8M5k5b1vsxv0d7UZfdj9+R+vxbvz4mpO9+JPcpxPmv3y5Fx+RzyVWCheLQtO8sA7YR4M83Zx8twJ2ql5hlWT2H+bIRVVKjQwLeoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQYKwIEFYkCAsSBAWJAgLEoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQYKwIEFYkCAsSBAWJAgLEoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQYKwIEFYkCAsSBAWJAgLEoQFifqGtdKP3m9Fq4Po5np0thcd7uRfoPBgI3LNnY38i2utvmH9uJ7ftJ1h9HgQ3Uo6K75+KgjLqm9Yf2/mN206/27mXzwthGXVN6z1YX7TpvPzev7F00JYVk3DOtrNb9fsrHAolKtpWNfW8ts1HbMjW+7lf1hmvlmL/+RFb7LTmuLfKDQ1DeuPQX5TpfNqKzqxozgIq4qahtV1n2CZQxJh6dUxrCXvtj/fJ6wZqGNYN7wnWAsdwpqBOob116QVLMLSq11Y8+3IfeIeL7u3CGsWahfWWe9SwvFu/JrdhNVK2nX51b2OdXcj/+J595uM4t8rNLUL65570/aGH16zP9nxFF3q538lO6dKLKtWXSAlrGB0HJvKzMNxmzY1145eb+V/JZ0/B/nXj0VYVr3C8h/jLvTzr8+67n4vad4MfJEcQyciLKteYf1UuFUmHbMzOuS+JetwJ1pzbOOoykVrwrLqFdY797Gsa0+wxvKcdJszs4NJkZ7bJXTzrX3HEJoaheU/DnrCMr/oaeaq3bSEVUWNwrrj3utE3rBeuhdUzen8nH0ZYVVRl7Dm3Ocro3GFddV9zm7mTGaJgbCqqEtYE2+xGhuWOZ3vu3PJLTEQVhV1Cet3z3WcZMaGdd999CwuMRBWFbUIy+x43KdJH6YY1jHvOXtxiYGwqqhFWJ61zXSKYb1wx5guMWR91Y1b9Ft17zgfDfIvNlwH4vQFs/k4pEAtwnrlXr5KJxfWZe9lwSs73U+wQGqFH9Zx7/JVOtmwzN6o59ii0fYlhqoIywo/rIfuo092smH5V7yySwxVEZYVeFgLJU7bR5OGtdSNrxu6ZuJdDOcmrWtMZYr/3tAEHtZN91Xn3KRhfe09u3LN260Pv05Y5YQc1v521HYcSopDWLMVclj+Gz5zQ1izFXJYZVYZ0iGs2Qo2rDMVNzBhzVawYXmezjB2CGu2gg3Lc/147OwyrDc2rLnCR7h28/Evl+LfNzTBhuW/j6o4aVhml7OZ3LwwlutdZsmHAFZdIK2vYMM6UfGQlLtW6PLIcYR9RljVBBuWOV649i5jp2RYrtsTnkxakR8hLCvYsIz/drTc4PfEEdaqDatSzTue9BMcwQo5rOIbw8eFn6QzNqzlXnR7+w19zx2XHn+z+xvCKifksH7YfqGwM/StBRTDurYWX43uD+NLQ+kPXXvBe4RVTchhrWxfODAbw7Nqmg1rbvudM9mHKLvuP/7e7tgc/3zKQ1if0uedj1vi3Va84ykT1sFO/kQqvVXmUOYPzE363AfCKifksFqZHcylZMNPDMu0+KZwsDM/GN1afsr96yfsJ3YIq5zAw/onOdd+aZfF/WF57ki+mRzprrgXXRfshxocf8CUh7A+sdG6UfpINH9Y5gVvC7ur0bST+9xdq1AbmfMzc0y86Ob5Ah9z/C2+3qXkU5P2sMDDurH2cYWpVSIszx2n5/vO7F7bPeJErjQjFkjDYvZV2f+5J4aVPd/PTfHcK50HpZsgLCvwsHImhmU8cx+tXHPZ+yjALMKymhfWheq3zYx2iv5vFJv63A07xOaFtb/tfG84dvr2FwmriuaF1Rr3rb6eeWzfHBBWFY0M60jpj7mauWaXlAirikaG1fLeQ5ydoV2UbxFWNU0Na8n7cKx0sjeOElYVTQ2r5b4LOTvZSyuEVUWDw1r0Ph0kylycHjnYyV94KZrWJR3jWNhXdRocVsv7jU5R8qGd5YqPNGKB1Gp2WE/dO5jRbCbXEIu/6EJYVoPDKv9MEfMWsvhI0rEIy2pqWEe8T3gvjvn162uTP6NMWFYjw5pr+86yPdMbxqv2R92n1YRlNTKs21Uu6YydN1vRLxvxYyByX1VHWFbzwrpY+tSq5LS34jcBdzfiY+WFfvzfYHZp5lC70IkOdLZ9tqxJGhbW6eSJIAFNu/TNq3tMk8I62YvvXvdPpTtqZjC3Ct+8EojGhDUYTq5qkNzTdz35hPQemUX3G4W9rTFhlZn0G5HMn9PeA3GVfCjXnkRYdl5sbvumk/nkfsBPe0L2XcCfLiSsZPrD6LNxa+vmzZ3Jy/MF99JZCvU4GDUorNGTIMeOyeas92LzfLJIsTqIT9RmNu9DfT840piwzLtC1wpW+iSZieaTR5iafdjzTXlk5T/MuCc1KaxW4ZFa0S6231xyG+pKLz7lv78Rf+bCnGub3czE954lZ/SYk2A1LKzW9htHnwx2/tWEHubPPJB8M5k5b1vsxv0d7UZfdj9+R+vxbvz4mpO9+JPcpxPmv3y5Fx+RzyVWCheLQtO8sA7YR4M83Zx8twJ2ql5hlWT2H+bIRVVKjQwLeoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQYKwIEFYkCAsSBAWJAgLEoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQYKwIEFYkCAsSBAWJAgLEoQFCcKCBGFBgrAgQViQICxIEBYkCAsShAUJwoIEYUGCsCBBWJAgLEgQFiQICxKEBQnCggRhQeJ/BH9FlTk9Er0AAAAASUVORK5CYII=',1,NULL,NULL,1,0,'2025-10-23 13:18:24','2025-10-23 14:56:45'),(5,NULL,'user@example.com','$2a$10$lPvahnxXp23P0Q49fRCbLur8wMpN5F2URUqDA99SXDxAP2iVrGmv2','用户user',NULL,0,NULL,NULL,1,0,'2025-12-03 11:14:31','2025-12-03 11:14:31');
/*!40000 ALTER TABLE `jeez_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_user_face`
--

DROP TABLE IF EXISTS `jeez_user_face`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_user_face` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `face_data` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  `version` int NOT NULL DEFAULT '1',
  `img_size` int DEFAULT NULL,
  `minio_object_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'MinIO object key for traceability',
  `minio_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'MinIO object public URL',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_version` (`version`),
  CONSTRAINT `fk_user_face_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='user face table';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_user_face`
--

LOCK TABLES `jeez_user_face` WRITE;
/*!40000 ALTER TABLE `jeez_user_face` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_user_face` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_user_location`
--

DROP TABLE IF EXISTS `jeez_user_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_user_location` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联jeez_user表）',
  `longitude` decimal(10,6) NOT NULL COMMENT '经度',
  `latitude` decimal(10,6) NOT NULL COMMENT '纬度',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '详细地址',
  `city_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市编码',
  `city_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市名称',
  `district_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域编码',
  `district_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区域名称',
  `location_source` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'manual' COMMENT '位置来源（manual:手动定位, gps:GPS定位, network:网络定位, ip:IP定位）',
  `confirmation_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'confirmed' COMMENT '用户确认状态（pending:待确认, confirmed:已确认, rejected:已拒绝）',
  `accuracy` double DEFAULT NULL COMMENT '定位精确度（米）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除 0:否 1:是）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_location_source` (`location_source`),
  KEY `idx_confirmation_status` (`confirmation_status`),
  KEY `idx_update_time` (`update_time`),
  KEY `idx_user_latest` (`user_id`,`update_time`),
  KEY `idx_is_deleted` (`is_deleted`),
  KEY `idx_coordinates` (`longitude`,`latitude`),
  CONSTRAINT `fk_user_location_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户位置信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_user_location`
--

LOCK TABLES `jeez_user_location` WRITE;
/*!40000 ALTER TABLE `jeez_user_location` DISABLE KEYS */;
INSERT INTO `jeez_user_location` VALUES (1,3,116.432321,39.928753,'北京市朝阳区建国路88号SOHO现代城','110000','北京市','110105','朝阳区','gps','confirmed',10.5,'2025-12-15 10:30:00','2025-12-15 10:30:00',3,3,0),(2,5,116.297511,39.987654,'北京市海淀区中关村大街1号','110000','北京市','110108','海淀区','gps','confirmed',15.2,'2025-12-15 09:20:00','2025-12-15 09:20:00',5,5,0),(3,6,116.475832,39.997123,'北京市朝阳区望京SOHO T1','110000','北京市','110105','朝阳区','manual','confirmed',25.8,'2025-12-15 08:45:00','2025-12-15 08:45:00',6,6,0),(4,4,116.397428,39.909693,'北京市朝阳区三里屯太古里','110000','北京市','110105','朝阳区','network','pending',50,'2025-12-14 19:30:00','2025-12-14 19:30:00',4,4,0),(5,1,116.407526,39.904034,'北京市东城区王府井大街138号','110000','北京市','110101','东城区','gps','confirmed',8.3,'2025-12-15 11:15:00','2025-12-15 11:15:00',1,1,0);
/*!40000 ALTER TABLE `jeez_user_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_user_role`
--

DROP TABLE IF EXISTS `jeez_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `jeez_user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `jeez_user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `jeez_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_user_role`
--

LOCK TABLES `jeez_user_role` WRITE;
/*!40000 ALTER TABLE `jeez_user_role` DISABLE KEYS */;
INSERT INTO `jeez_user_role` VALUES (1,1,1,'2025-10-23 10:48:42'),(2,2,2,'2025-10-23 10:48:42'),(3,3,3,'2025-10-23 10:48:42'),(4,4,4,'2025-10-23 10:48:42'),(5,1,5,'2025-10-23 16:35:32'),(6,5,4,'2025-12-03 11:14:31');
/*!40000 ALTER TABLE `jeez_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_user_store_relation`
--

DROP TABLE IF EXISTS `jeez_user_store_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_user_store_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `can_access_all_devices` tinyint NOT NULL DEFAULT '1',
  `status` tinyint NOT NULL DEFAULT '1',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_store` (`user_id`,`store_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_user_store_store` FOREIGN KEY (`store_id`) REFERENCES `jeez_store` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_store_user` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='user store relation';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_user_store_relation`
--

LOCK TABLES `jeez_user_store_relation` WRITE;
/*!40000 ALTER TABLE `jeez_user_store_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_user_store_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_user_third_auth`
--

DROP TABLE IF EXISTS `jeez_user_third_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_user_third_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `platform` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方平台',
  `openid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '第三方平台用户唯一标识',
  `unionid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方平台用户统一标识',
  `nickname` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方昵称',
  `avatar` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '第三方头像',
  `extra_info` json DEFAULT NULL COMMENT '额外信息（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_platform_openid` (`user_id`,`platform`,`openid`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_platform` (`platform`),
  KEY `idx_openid` (`openid`),
  CONSTRAINT `jeez_user_third_auth_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户第三方授权登录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_user_third_auth`
--

LOCK TABLES `jeez_user_third_auth` WRITE;
/*!40000 ALTER TABLE `jeez_user_third_auth` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_user_third_auth` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `jeez_wechat_mini_user`
--

DROP TABLE IF EXISTS `jeez_wechat_mini_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `jeez_wechat_mini_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '微信小程序 OpenID',
  `unionid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信 UnionID',
  `session_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话密钥',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint DEFAULT '0' COMMENT '性别 0-未知 1-男 2-女',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '城市',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省份',
  `country` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国家',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_unionid` (`unionid`),
  CONSTRAINT `jeez_wechat_mini_user_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `jeez_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信小程序用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jeez_wechat_mini_user`
--

LOCK TABLES `jeez_wechat_mini_user` WRITE;
/*!40000 ALTER TABLE `jeez_wechat_mini_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `jeez_wechat_mini_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_coach_detail`
--

DROP TABLE IF EXISTS `v_coach_detail`;
/*!50001 DROP VIEW IF EXISTS `v_coach_detail`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_coach_detail` AS SELECT 
 1 AS `coach_info_id`,
 1 AS `user_id`,
 1 AS `coach_code`,
 1 AS `coach_name`,
 1 AS `phone`,
 1 AS `email`,
 1 AS `avatar`,
 1 AS `gender`,
 1 AS `skill_level_id`,
 1 AS `level_code`,
 1 AS `skill_level_name`,
 1 AS `specialty_domains`,
 1 AS `work_years`,
 1 AS `introduction`,
 1 AS `specialties`,
 1 AS `certification_status`,
 1 AS `certification_status_name`,
 1 AS `work_status`,
 1 AS `work_status_name`,
 1 AS `hire_date`,
 1 AS `last_active_time`,
 1 AS `current_experience_years`,
 1 AS `certification_count`,
 1 AS `training_count`,
 1 AS `total_sessions`,
 1 AS `total_students`,
 1 AS `average_rating`,
 1 AS `price_multiplier`,
 1 AS `min_session_price`,
 1 AS `max_session_price`,
 1 AS `level_icon_url`,
 1 AS `level_color_code`,
 1 AS `level_effective_date`,
 1 AS `can_upgrade`,
 1 AS `benefits`,
 1 AS `created_time`,
 1 AS `updated_time`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_store_hierarchy`
--

DROP TABLE IF EXISTS `v_store_hierarchy`;
/*!50001 DROP VIEW IF EXISTS `v_store_hierarchy`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_store_hierarchy` AS SELECT 
 1 AS `store_id`,
 1 AS `store_name`,
 1 AS `store_code`,
 1 AS `store_level`,
 1 AS `level_name`,
 1 AS `store_path`,
 1 AS `parent_id`,
 1 AS `parent_store_name`,
 1 AS `group_id`,
 1 AS `group_name`,
 1 AS `chain_code`,
 1 AS `store_type_name`,
 1 AS `status`,
 1 AS `status_name`,
 1 AS `address`,
 1 AS `phone`,
 1 AS `rating`,
 1 AS `review_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping routines for database 'jeez_fitness'
--

--
-- Final view structure for view `v_coach_detail`
--

/*!50001 DROP VIEW IF EXISTS `v_coach_detail`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_coach_detail` AS select `ci`.`id` AS `coach_info_id`,`ci`.`user_id` AS `user_id`,`ci`.`coach_code` AS `coach_code`,`u`.`nickname` AS `coach_name`,`u`.`phone` AS `phone`,`u`.`email` AS `email`,`u`.`avatar` AS `avatar`,`u`.`gender` AS `gender`,`ci`.`skill_level_id` AS `skill_level_id`,`dsl`.`level_code` AS `level_code`,`dsl`.`level_name` AS `skill_level_name`,`ci`.`specialty_domains` AS `specialty_domains`,`ci`.`work_years` AS `work_years`,`ci`.`introduction` AS `introduction`,`ci`.`specialties` AS `specialties`,`ci`.`certification_status` AS `certification_status`,(case `ci`.`certification_status` when 0 then '未认证' when 1 then '已认证' when 2 then '认证中' else '未知' end) AS `certification_status_name`,`ci`.`work_status` AS `work_status`,(case `ci`.`work_status` when 0 then '离职' when 1 then '在职' when 2 then '休假' else '未知' end) AS `work_status_name`,`ci`.`hire_date` AS `hire_date`,`ci`.`last_active_time` AS `last_active_time`,`ci`.`current_experience_years` AS `current_experience_years`,`ci`.`certification_count` AS `certification_count`,`ci`.`training_count` AS `training_count`,`ci`.`total_sessions` AS `total_sessions`,`ci`.`total_students` AS `total_students`,`ci`.`average_rating` AS `average_rating`,`ci`.`price_multiplier` AS `price_multiplier`,`ci`.`min_session_price` AS `min_session_price`,`ci`.`max_session_price` AS `max_session_price`,`ci`.`level_icon_url` AS `level_icon_url`,`ci`.`level_color_code` AS `level_color_code`,`ci`.`level_effective_date` AS `level_effective_date`,`ci`.`can_upgrade` AS `can_upgrade`,`ci`.`benefits` AS `benefits`,`ci`.`created_time` AS `created_time`,`ci`.`updated_time` AS `updated_time` from ((`jeez_coach_info` `ci` left join `jeez_user` `u` on((`ci`.`user_id` = `u`.`id`))) left join `jeez_dict_coach_skill_level` `dsl` on((`ci`.`skill_level_id` = `dsl`.`id`))) where ((`ci`.`is_deleted` = 0) and (`u`.`deleted` = 0)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_store_hierarchy`
--

/*!50001 DROP VIEW IF EXISTS `v_store_hierarchy`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_store_hierarchy` AS select `s`.`id` AS `store_id`,`s`.`name` AS `store_name`,`s`.`code` AS `store_code`,`s`.`store_level` AS `store_level`,(case `s`.`store_level` when 1 then '总部' when 2 then '区域中心' when 3 then '普通门店' else '未知' end) AS `level_name`,`s`.`store_path` AS `store_path`,`s`.`parent_id` AS `parent_id`,`ps`.`name` AS `parent_store_name`,`s`.`group_id` AS `group_id`,`sg`.`group_name` AS `group_name`,`s`.`chain_code` AS `chain_code`,`st`.`type_name` AS `store_type_name`,`s`.`status` AS `status`,(case `s`.`status` when 1 then '正常' when 2 then '暂停' when 3 then '关闭' else '未知' end) AS `status_name`,`s`.`address` AS `address`,`s`.`phone` AS `phone`,`s`.`rating` AS `rating`,`s`.`review_count` AS `review_count` from (((`jeez_store` `s` left join `jeez_store` `ps` on((`s`.`parent_id` = `ps`.`id`))) left join `jeez_dict_store_group` `sg` on((`s`.`group_id` = `sg`.`id`))) left join `jeez_dict_store_type` `st` on((`s`.`store_type_id` = `st`.`id`))) where (`s`.`is_deleted` = 0) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-26 23:59:01

--
-- PMS workspace memo extension
--

CREATE TABLE IF NOT EXISTS `workspace_memo` (
  `memo_id` bigint NOT NULL COMMENT '备忘录ID',
  `camp_id` bigint NOT NULL COMMENT '门店ID',
  `user_id` bigint NOT NULL COMMENT '创建用户ID',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '备忘录内容',
  `is_handle` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态：0待处理 1已处理',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`memo_id`),
  KEY `idx_workspace_memo_camp_handle` (`camp_id`,`is_handle`,`is_deleted`,`created_at`),
  KEY `idx_workspace_memo_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作台备忘录';
