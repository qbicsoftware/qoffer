DROP TABLE IF EXISTS `offers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `offers` (
  `offer_id` int(11) NOT NULL AUTO_INCREMENT,
  `offer_number` varchar(45) DEFAULT NULL,
  `offer_project_reference` varchar(45) DEFAULT NULL,
  `offer_facility` varchar(45) DEFAULT NULL,
  `offer_name` varchar(500) DEFAULT '',
  `offer_description` varchar(2500) DEFAULT NULL,
  `offer_group` enum('Internal','External') DEFAULT NULL,
  `offer_price` float DEFAULT NULL,
  `offer_total` float DEFAULT NULL,
  `offer_date` timestamp NULL DEFAULT NULL,
  `offer_status` enum('In Progress','Sent','Rejected','Accepted') DEFAULT NULL,
  `last_edited` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `added_by` varchar(11) DEFAULT NULL,
  `discount` varchar(11) DEFAULT NULL,
  `internal` int(11) DEFAULT NULL,
  `offer_extra_price` varchar(45) DEFAULT NULL,
  `estimated_delivery_weeks` int(11) DEFAULT NULL,
  PRIMARY KEY (`offer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `packages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `packages` (
  `package_id` int(11) NOT NULL AUTO_INCREMENT,
  `package_name` varchar(45) DEFAULT '',
  `package_facility` varchar(45) DEFAULT '',
  `package_description` varchar(400) DEFAULT NULL,
  `package_group` varchar(45) DEFAULT NULL,
  `package_grp` enum('Bioinformatics Analysis','Sequencing','Project Management','Mass Spectrometry') DEFAULT NULL,
  `package_price_internal` float DEFAULT NULL,
  `package_price_external_academic` float DEFAULT NULL,
  `package_price_external_commercial` float DEFAULT NULL,
  `package_unit_type` varchar(11) DEFAULT NULL,
  `package_date` date DEFAULT NULL,
  `last_edited` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `added_by` varchar(11) DEFAULT NULL,
  PRIMARY KEY (`package_id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

DROP TABLE IF EXISTS `offers_packages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `offers_packages` (
  `offer_id` int(11) DEFAULT NULL,
  `package_id` int(11) DEFAULT NULL,
  `package_addon_price` double DEFAULT NULL,
  `package_count` varchar(45) DEFAULT NULL,
  `package_discount` varchar(45) DEFAULT NULL,
  `package_price_type` enum('internal','external_academic','external_commercial') NOT NULL DEFAULT 'internal',
  KEY `offer_id` (`offer_id`),
  KEY `package_id` (`package_id`),
  CONSTRAINT `offers_packages_ibfk_1` FOREIGN KEY (`offer_id`) REFERENCES `offers` (`offer_id`),
  CONSTRAINT `offers_packages_ibfk_2` FOREIGN KEY (`package_id`) REFERENCES `packages` (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

