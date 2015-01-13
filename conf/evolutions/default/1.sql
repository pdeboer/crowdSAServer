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

CREATE TABLE highlights (
  id   INT PRIMARY KEY NOT NULL auto_increment,
  term VARCHAR(255)    NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE questions (
  id           INT PRIMARY KEY NOT NULL auto_increment,
  question     VARCHAR(2000)   NOT NULL,
  answerType   VARCHAR(100)    NOT NULL,
  reward       INT             NOT NULL,
  highlight_fk INT             NOT NULL,
  paper_fk     INT             NOT NULL,
  FOREIGN KEY (paper_fk) REFERENCES papers (id),
  FOREIGN KEY (highlight_fk) REFERENCES highlights (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE answers (
  id         INT PRIMARY KEY NOT NULL auto_increment,
  answer     VARCHAR(255),
  answerTime BIGINT          NOT NULL,
  turkerId   INT             NOT NULL,
  qId        INT             NOT NULL,
  FOREIGN KEY (qId) REFERENCES questions (id),
  FOREIGN KEY (turkerId) REFERENCES turkers (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;


INSERT INTO papers(id, pdfPath, pdfTitle, createdAt, budget) VALUES(1, "/tmp/test.pdf", "Awsome Title", 1213121231, 10);
INSERT INTO highlights(id, term) VALUES(1, "MANOVA");
INSERT INTO highlights(id, term) VALUES(2, "ANOVA");
INSERT INTO questions(id, question, answerType, reward, highlight_fk, paper_fk) VALUES(1, "How old are you?", "Integer", 1,1,1);
INSERT INTO questions(id, question, answerType, reward, highlight_fk, paper_fk) VALUES(2, "What is your name?", "String", 2,2,1);

# --- !Downs

DROP TABLE turkers;
DROP TABLE papers;
DROP TABLE highlights;
DROP TABLE questions;
DROP TABLE answers;