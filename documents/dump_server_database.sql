CREATE DATABASE  IF NOT EXISTS `bachelor` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `bachelor`;
-- MySQL dump 10.13  Distrib 5.6.19, for osx10.7 (i386)
--
-- Host: 127.0.0.1    Database: bachelor
-- ------------------------------------------------------
-- Server version	5.6.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `answers`
--

DROP TABLE IF EXISTS `answers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `answers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `answer` text,
  `created_at` bigint(20) NOT NULL,
  `accepted` bit(1) DEFAULT NULL,
  `bonus_cts` int(11) DEFAULT NULL,
  `rejected` bit(1) DEFAULT NULL,
  `assignments_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `assignments_id` (`assignments_id`),
  CONSTRAINT `answers_ibfk_1` FOREIGN KEY (`assignments_id`) REFERENCES `assignments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `assignments`
--

DROP TABLE IF EXISTS `assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `assignments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` bigint(20) NOT NULL,
  `expiration_time` bigint(20) NOT NULL,
  `is_cancelled` bit(1) NOT NULL,
  `questions_id` bigint(20) NOT NULL,
  `teams_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `teams_id` (`teams_id`),
  KEY `questions_id` (`questions_id`),
  CONSTRAINT `assignments_ibfk_1` FOREIGN KEY (`teams_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `assignments_ibfk_2` FOREIGN KEY (`questions_id`) REFERENCES `questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `datasets`
--

DROP TABLE IF EXISTS `datasets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datasets` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `statistical_method` varchar(255) NOT NULL,
  `dom_children` text NOT NULL,
  `name` varchar(255) NOT NULL,
  `url` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `datasets2papers`
--

DROP TABLE IF EXISTS `datasets2papers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datasets2papers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `datasets_id` bigint(20) NOT NULL,
  `papers_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `papers_id` (`papers_id`),
  KEY `datasets_id` (`datasets_id`),
  CONSTRAINT `datasets2papers_ibfk_1` FOREIGN KEY (`papers_id`) REFERENCES `papers` (`id`),
  CONSTRAINT `datasets2papers_ibfk_2` FOREIGN KEY (`datasets_id`) REFERENCES `datasets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `feedbacks`
--

DROP TABLE IF EXISTS `feedbacks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feedbacks` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `useful` int(11) NOT NULL,
  `answers_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `answers_id` (`answers_id`),
  CONSTRAINT `feedbacks_ibfk_1` FOREIGN KEY (`answers_id`) REFERENCES `answers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `highlights`
--

DROP TABLE IF EXISTS `highlights`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `highlights` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `assumption` varchar(255) NOT NULL,
  `terms` varchar(1000) NOT NULL,
  `questions_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `questions_id` (`questions_id`),
  CONSTRAINT `highlights_ibfk_1` FOREIGN KEY (`questions_id`) REFERENCES `questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `papers`
--

DROP TABLE IF EXISTS `papers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `papers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pdf_path` varchar(255) NOT NULL,
  `pdf_title` varchar(255) NOT NULL,
  `created_at` bigint(20) NOT NULL,
  `highlight_enabled` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `play_evolutions`
--

DROP TABLE IF EXISTS `play_evolutions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `play_evolutions` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) NOT NULL,
  `applied_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `apply_script` text,
  `revert_script` text,
  `state` varchar(255) DEFAULT NULL,
  `last_problem` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qualifications`
--

DROP TABLE IF EXISTS `qualifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qualifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `questions_id` bigint(20) NOT NULL,
  `teams_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `teams_id` (`teams_id`),
  KEY `questions_id` (`questions_id`),
  CONSTRAINT `qualifications_ibfk_1` FOREIGN KEY (`teams_id`) REFERENCES `teams` (`id`),
  CONSTRAINT `qualifications_ibfk_2` FOREIGN KEY (`questions_id`) REFERENCES `questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `questions`
--

DROP TABLE IF EXISTS `questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `questions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question` varchar(2000) NOT NULL,
  `question_type` varchar(100) NOT NULL,
  `reward_cts` int(11) NOT NULL,
  `created_at` bigint(20) NOT NULL,
  `disabled` bit(1) NOT NULL,
  `expiration_time_sec` bigint(20) DEFAULT NULL,
  `maximal_assignments` int(11) DEFAULT NULL,
  `papers_id` bigint(20) NOT NULL,
  `possible_answers` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `papers_id` (`papers_id`),
  CONSTRAINT `questions_ibfk_1` FOREIGN KEY (`papers_id`) REFERENCES `papers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teams`
--

DROP TABLE IF EXISTS `teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teams` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `turkers`
--

DROP TABLE IF EXISTS `turkers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `turkers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `turker_id` varchar(100) NOT NULL,
  `created_at` bigint(20) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `login_time` bigint(20) NOT NULL,
  `logout_time` bigint(20) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `layout_mode` int(11) DEFAULT NULL,
  `feedback` varchar(10000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `turker_id` (`turker_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `turkers2teams`
--

DROP TABLE IF EXISTS `turkers2teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `turkers2teams` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `turkers_id` bigint(20) NOT NULL,
  `teams_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `turkers_id` (`turkers_id`),
  KEY `teams_id` (`teams_id`),
  CONSTRAINT `turkers2teams_ibfk_1` FOREIGN KEY (`turkers_id`) REFERENCES `turkers` (`id`),
  CONSTRAINT `turkers2teams_ibfk_2` FOREIGN KEY (`teams_id`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-04-22 16:58:13
