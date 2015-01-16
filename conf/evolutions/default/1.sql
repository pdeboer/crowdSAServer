# --- !Ups

CREATE TABLE turkers (
  id        INT PRIMARY KEY NOT NULL auto_increment,
  turkerId  VARCHAR(100)    NOT NULL UNIQUE,
  email     VARCHAR(255)    NOT NULL,
  loginTime BIGINT          NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE papers (
  id        INT PRIMARY KEY NOT NULL auto_increment,
  pdfPath   VARCHAR(255)    NOT NULL,
  pdfTitle  VARCHAR(255)    NOT NULL,
  createdAt BIGINT          NOT NULL,
  budget    INT             NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE questions (
  id         INT PRIMARY KEY NOT NULL auto_increment,
  question   VARCHAR(2000)   NOT NULL,
  answerType VARCHAR(100)    NOT NULL,
  reward     INT             NOT NULL,
  paper_fk   INT             NOT NULL,
  FOREIGN KEY (paper_fk) REFERENCES papers (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE highlights (
  id          INT PRIMARY KEY NOT NULL auto_increment,
  assumption  VARCHAR(255)    NOT NULL,
  terms       VARCHAR(255)    NOT NULL,
  question_fk INT             NOT NULL,
  FOREIGN KEY (question_fk) REFERENCES questions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE datasets (
  id          INT PRIMARY KEY NOT NULL auto_increment,
  statMethod  VARCHAR(255)    NOT NULL,
  domChildren VARCHAR(255)    NULL,
  question_fk INT             NOT NULL,
  FOREIGN KEY (question_fk) REFERENCES questions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE answers (
  id         INT PRIMARY KEY NOT NULL auto_increment,
  answer     VARCHAR(255),
  answerTime BIGINT          NOT NULL,
  turkerId   INT             NOT NULL,
  question_fk        INT             NOT NULL,
  FOREIGN KEY (question_fk) REFERENCES questions (id),
  FOREIGN KEY (turkerId) REFERENCES turkers (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;


INSERT INTO turkers (id, turkerId, email, loginTime) VALUES (1, "mattia", "mattia.amato@gmail.com", 1421183695322);

INSERT INTO papers (id, pdfPath, pdfTitle, createdAt, budget) VALUES (1, "/pdfs/test.pdf", "Let’s Do It at My Place Instead? Attitudinal and Behavioral Study of Privacy in Client-Side Personalization", 1421182061982, 10);

INSERT INTO questions (id, question, answerType, reward, paper_fk) VALUES (1, "Is the D'Agostino's K-squared test used to test the normality assumption?", "Integer", 1, 1);
INSERT INTO questions (id, question, answerType, reward, paper_fk) VALUES (2, "Is the Jarque–Bera test used to test the normality?", "String", 2, 1);

INSERT INTO highlights (id, assumption, terms, question_fk) VALUES (1, "Normality", "D’Agostino’s K-squared,D’Agostino’s K2,D’Agostino’s K test,kurtosis,skewness", 1);
INSERT INTO highlights (id, assumption, terms, question_fk) VALUES (2, "Normality", "Jarque–Bera,JB test", 2);

INSERT INTO datasets (id, statMethod, domChildren, question_fk) VALUES (1, "MANOVA", "200,210", 1);
INSERT INTO datasets (id, statMethod, domChildren, question_fk) VALUES (2, "MANOVA", "123,125", 2);

# --- !Downs

DROP TABLE turkers;
DROP TABLE papers;
DROP TABLE highlights;
DROP TABLE datasets;
DROP TABLE answers;
DROP TABLE questions;
