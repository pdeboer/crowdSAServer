# --- !Ups

CREATE TABLE papers (
  id        INT PRIMARY KEY NOT NULL auto_increment,
  pdfPath   VARCHAR(255)    NOT NULL,
  pdfTitle  VARCHAR(255)    NOT NULL,
  createdAt BIGINT          NOT NULL,
  budget    INT             NOT NULL,
  highlight BIT             NOT NULL
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE datasets (
  id          INT PRIMARY KEY NOT NULL auto_increment,
  statMethod  VARCHAR(255)    NOT NULL,
  domChildren VARCHAR(255)    NULL,
  paper_fk    INT             NOT NULL,
  FOREIGN KEY (paper_fk) REFERENCES papers (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE questions (
  id           INT PRIMARY KEY NOT NULL auto_increment,
  question     VARCHAR(2000)   NOT NULL,
  questionType VARCHAR(100)    NOT NULL,
  reward       INT             NOT NULL,
  createdAt    BIGINT          NOT NULL,
  paper_fk     INT             NOT NULL,
  FOREIGN KEY (paper_fk) REFERENCES papers (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE highlights (
  id          INT PRIMARY KEY NOT NULL auto_increment,
  assumption  VARCHAR(255)    NOT NULL,
  terms       VARCHAR(255)    NOT NULL,
  question_fk INT             NOT NULL,
  FOREIGN KEY (question_fk) REFERENCES questions (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE teams (
  id        INT PRIMARY KEY NOT NULL auto_increment,
  createdAt BIGINT          NOT NULL,
  name      VARCHAR(255)    NOT NULL
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE assignments (
  id           INT PRIMARY KEY NOT NULL auto_increment,
  assumption   VARCHAR(255)    NOT NULL,
  assignedFrom BIGINT          NOT NULL,
  assignedTo   BIGINT          NOT NULL,
  acceptedTime BIGINT          NOT NULL,
  question_fk  INT             NOT NULL,
  team_fk      INT             NOT NULL,
  FOREIGN KEY (team_fk) REFERENCES teams (id),
  FOREIGN KEY (question_fk) REFERENCES questions (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE answers (
  id               INT PRIMARY KEY NOT NULL auto_increment,
  answer           VARCHAR(255),
  completedTime    BIGINT          NOT NULL,
  accepted         BIT             NULL,
  acceptedAndBonus BIT             NULL,
  rejected         BIT             NULL,
  assignment_fk    INT             NOT NULL,
  FOREIGN KEY (assignment_fk) REFERENCES assignments (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE ranking_groups (
  id        INT PRIMARY KEY NOT NULL auto_increment,
  groupName VARCHAR(255)    NOT NULL
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE rankings (
  id               INT PRIMARY KEY NOT NULL auto_increment,
  rank             INT             NOT NULL,
  ranking_group_fk INT             NOT NULL,
  answer_fk        INT             NOT NULL,
  FOREIGN KEY (answer_fk) REFERENCES answers (id),
  FOREIGN KEY (ranking_group_fk) REFERENCES ranking_groups (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE turkers (
  id         INT PRIMARY KEY NOT NULL auto_increment,
  turkerId   VARCHAR(100)    NOT NULL UNIQUE,
  email      VARCHAR(255)    NOT NULL,
  loginTime  BIGINT          NOT NULL,
  username   VARCHAR(100)    NOT NULL UNIQUE,
  password   VARCHAR(100)    NOT NULL,
  layoutMode INT             NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE turkers2teams (
  id        INT PRIMARY KEY NOT NULL auto_increment,
  turker_fk INT             NOT NULL,
  team_fk   INT             NOT NULL,
  FOREIGN KEY (turker_fk) REFERENCES turkers (id),
  FOREIGN KEY (team_fk) REFERENCES teams (id)
) ENGINE = InnoDB CHARSET = utf8;

INSERT INTO turkers (id, turkerId, email, loginTime, username, password, layoutMode)
VALUES (1, "mattia", "mattia.amato@gmail.com", 1421183695322, "mamato", "737eb8871f2ade70fea17fc8df76e691", 1);
INSERT INTO teams (id, createdAt, name) VALUES (1, 1421183695324, "mamato");
INSERT INTO turkers2teams (id, turker_fk, team_fk) VALUES (1, 1, 1);

INSERT INTO papers (id, pdfPath, pdfTitle, createdAt, budget, highlight) VALUES (1, "/pdfs/test.pdf",
                                                                                 "Let’s Do It at My Place Instead? Attitudinal and Behavioral Study of Privacy in Client-Side Personalization",
                                                                                 1421182061982, 10, 1);

INSERT INTO questions (id, question, questionType, reward, createdAt, paper_fk)
VALUES (1, "Is the D'Agostino's K-squared test used to test the normality assumption?", "Integer", 1, 1421182063982, 1);
INSERT INTO questions (id, question, questionType, reward, createdAt, paper_fk)
VALUES (2, "Is the Jarque–Bera test used to test the normality?", "String", 2, 1421182064982, 1);

INSERT INTO highlights (id, assumption, terms, question_fk)
VALUES (1, "Normality", "D’Agostino’s K-squared,D’Agostino’s K2,D’Agostino’s K test,kurtosis,skewness", 1);
INSERT INTO highlights (id, assumption, terms, question_fk) VALUES (2, "Normality", "Jarque–Bera,JB test", 2);

INSERT INTO datasets (id, statMethod, domChildren, paper_fk) VALUES (1, "MANOVA", "200,210", 1);
INSERT INTO datasets (id, statMethod, domChildren, paper_fk) VALUES (2, "MANOVA", "123,125", 1);

# --- !Downs

DROP TABLE turkers2teams;
DROP TABLE turkers;
DROP TABLE rankings;
DROP TABLE ranking_groups;
DROP TABLE assignments;
DROP TABLE answers;
DROP TABLE teams;
DROP TABLE highlights;
DROP TABLE questions;
DROP TABLE datasets;
DROP TABLE papers;