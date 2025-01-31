-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: quick-park-assist
-- ------------------------------------------------------
-- Server version	8.0.35
--
-- Table structure for table `addon_service`
--
DROP TABLE IF EXISTS `addon_service`;
CREATE TABLE `addon_service` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `duration` VARCHAR(255) NOT NULL,
    `name` VARCHAR(255) NOT NULL,
    `price` DOUBLE NOT NULL,
    `service_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FKtnlxmknrnputa66cqda1w4nkk` (`service_id`),
    KEY `FK8xd6fv2busxejdn0el22dvnsv` (`user_id`),
    CONSTRAINT `FK8xd6fv2busxejdn0el22dvnsv` FOREIGN KEY (`user_id`)
        REFERENCES `users` (`id`),
    CONSTRAINT `FKtnlxmknrnputa66cqda1w4nkk` FOREIGN KEY (`service_id`)
        REFERENCES `services` (`id`)
)  ENGINE=INNODB AUTO_INCREMENT=3 DEFAULT CHARSET=UTF8MB4 COLLATE = UTF8MB4_0900_AI_CI;

--
-- Dumping data for table `addon_service`
--

LOCK TABLES `addon_service` WRITE;
INSERT INTO `addon_service` VALUES (1,'1','EV Bike Full Service',80,3,1),(2,'1','EV Bike Full Service',80,3,1);
UNLOCK TABLES;

--
-- Table structure for table `booking_spot`
--

DROP TABLE IF EXISTS `booking_spot`;
CREATE TABLE `booking_spot` (
  `booking_id` bigint NOT NULL AUTO_INCREMENT,
  `booking_status` enum('CANCELLED','COMPLETED','CONFIRMED','PENDING') DEFAULT NULL,
  `duration` double DEFAULT NULL,
  `end_time` datetime(6) NOT NULL,
  `estimated_price` double DEFAULT NULL,
  `mobile_number` varchar(255) DEFAULT NULL,
  `payment_method` enum('CASH','CREDIT','UPI') DEFAULT NULL,
  `spot_location` varchar(255) DEFAULT NULL,
  `start_time` datetime(6) NOT NULL,
  `spot_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`booking_id`),
  KEY `FKsay5es561pvlsqjptboob2qyj` (`spot_id`),
  KEY `FKgn2ubjuiajplyekv8y5slh9us` (`user_id`),
  CONSTRAINT `FKgn2ubjuiajplyekv8y5slh9us` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKsay5es561pvlsqjptboob2qyj` FOREIGN KEY (`spot_id`) REFERENCES `parking_spot` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `booking_spot`
--

LOCK TABLES `booking_spot` WRITE;
INSERT INTO `booking_spot` VALUES (1,'CONFIRMED',3,'2025-01-30 11:56:00.000000',756,'9347305870','CASH','masabTank','2025-01-30 08:56:00.000000',1,1);

UNLOCK TABLES;

--
-- Table structure for table `otp`
--

DROP TABLE IF EXISTS `otp`;
CREATE TABLE `otp` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiration_time` datetime(6) DEFAULT NULL,
  `otp_code` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `otp`
--

LOCK TABLES `otp` WRITE;
UNLOCK TABLES;

--
-- Table structure for table `parking_spot`
--

DROP TABLE IF EXISTS `parking_spot`;

CREATE TABLE `parking_spot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `additional_instructions` varchar(255) NOT NULL,
  `availability` varchar(255) NOT NULL,
  `location` varchar(255) NOT NULL,
  `price_per_hour` double NOT NULL,
  `spot_location` varchar(255) NOT NULL,
  `spot_type` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9ufesnxvqulxwdo2fyskfv11u` (`user_id`),
  CONSTRAINT `FK9ufesnxvqulxwdo2fyskfv11u` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `parking_spot`
--

LOCK TABLES `parking_spot` WRITE;

INSERT INTO `parking_spot` VALUES (1,'Near tank','Available','Hanamakonda',252,'masabTank','INDOOR',2),(2,'near to minicipal Park','Available','Delhi',99,'india gate','EV_SPOT',2);

UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
CREATE TABLE `reservations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `slot` varchar(255) DEFAULT NULL,
  `charging_station` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `reservation_time` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `vehicle_number` varchar(255) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `spot_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb5g9io5h54iwl2inkno50ppln` (`user_id`),
  CONSTRAINT `FKb5g9io5h54iwl2inkno50ppln` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;

INSERT INTO `reservations` VALUES (5,'2','Delhi, india gate','Bhanu','2025-01-31 13:33:00.000000','CONFIRMED','TS-99-BT-9999',1,2);

UNLOCK TABLES;

--
-- Table structure for table `services`
--

DROP TABLE IF EXISTS `services`;
CREATE TABLE `services` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` double DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmauqobewmd57ylq7ck6wprgkt` (`user_id`),
  CONSTRAINT `FKmauqobewmd57ylq7ck6wprgkt` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--
-- Dumping data for table `services`
--

LOCK TABLES `services` WRITE;

INSERT INTO `services` VALUES (2,'Battery replacement for EV cars','EV Car Battery Replacement',153,2),(3,'Complete servicing for electric bikes','EV Bike Full Service',80,2),(4,'Charging station setup for EVs','EV Charging Station Installation',500,2),(5,'Motor repair and maintenance for e-bikes','E-Bike Motor Repair',120,2),(6,'Software updates for EV cars','EV Car Software Update',50,2),(7,'Brake system check and repair for e-bikes','E-Bike Brake Repair',40,2),(8,'Tire replacement for electric cars','EV Car Tire Replacement',100,2),(9,'Battery health check for electric bikes','E-Bike Battery Health Check',30,2),(10,'Suspension repair for EV cars','EV Car Suspension Repair',200,2),(11,'General diagnostics for e-bikes','E-Bike Diagnostics',60,2);

UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `address` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `user_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
INSERT INTO `users` VALUES (1,_binary '','Hyderabad','2025-01-27 21:41:50.616573','bhanutejakokkonda@gmail.com','Bhanu Teja','Bhanu@321','9347305870','VEHICLE_OWNER'),(2,_binary '','Hyderabad','2025-01-27 21:46:44.513292','srivanikokkonda@gmail.com','srivani','pmWkWSBCL51Bfkhn79xPuKBKHz//H6B+mY6G9/eieuM=','8977233617','SPOT_OWNER');
UNLOCK TABLES;

--
-- Table structure for table `vehicles`
--

DROP TABLE IF EXISTS `vehicles`;
CREATE TABLE `vehicles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color` varchar(255) NOT NULL,
  `ev` bit(1) NOT NULL,
  `manufacturer` varchar(255) NOT NULL,
  `model` varchar(255) NOT NULL,
  `registered_at` datetime(6) NOT NULL,
  `vehicle_number` varchar(255) NOT NULL,
  `vehicle_type` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo4u5y92lt2sx8y2dc1bb9sewc` (`user_id`),
  CONSTRAINT `FKo4u5y92lt2sx8y2dc1bb9sewc` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `vehicles`
--

LOCK TABLES `vehicles` WRITE;
INSERT INTO `vehicles` VALUES (1,'Blue',_binary '','HOND','LandCruiser','2025-01-27 22:33:21.072577','TS-99-BT-9999','BIKE',1);
UNLOCK TABLES;

-- Dump completed on 2025-01-30 19:32:35
