# --- !Ups

CREATE TABLE papers (
  id                BIGINT PRIMARY KEY NOT NULL auto_increment,
  pdf_path          VARCHAR(255)       NOT NULL,
  pdf_title         VARCHAR(255)       NOT NULL,
  created_at        BIGINT             NOT NULL,
  highlight_enabled BIT                NOT NULL
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE datasets (
  id                 BIGINT PRIMARY KEY NOT NULL auto_increment,
  statistical_method VARCHAR(255)       NOT NULL,
  dom_children       TEXT           NOT NULL,
  name               VARCHAR(255)       NOT NULL UNIQUE,
  url                VARCHAR(1000)      NULL
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE datasets2papers (
  id          BIGINT PRIMARY KEY NOT NULL auto_increment,
  datasets_id BIGINT             NOT NULL,
  papers_id   BIGINT             NOT NULL,
  FOREIGN KEY (papers_id) REFERENCES papers (id),
  FOREIGN KEY (datasets_id) REFERENCES datasets (id)
)ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE questions (
  id                  BIGINT PRIMARY KEY NOT NULL auto_increment,
  question            VARCHAR(2000)      NOT NULL,
  question_type       VARCHAR(100)       NOT NULL,
  reward_cts          INT                NOT NULL,
  created_at          BIGINT             NOT NULL,
  disabled            BIT                NOT NULL,
  expiration_time_sec BIGINT             NULL,
  maximal_assignments INT                NULL,
  papers_id           BIGINT             NOT NULL,
  possible_answers    VARCHAR(2000)      NULL,
  FOREIGN KEY (papers_id) REFERENCES papers (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE highlights (
  id           BIGINT PRIMARY KEY NOT NULL auto_increment,
  assumption   VARCHAR(255)       NOT NULL,
  terms        VARCHAR(1000)      NOT NULL,
  questions_id BIGINT             NOT NULL,
  FOREIGN KEY (questions_id) REFERENCES questions (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE teams (
  id         BIGINT PRIMARY KEY NOT NULL auto_increment,
  created_at BIGINT             NOT NULL,
  name       VARCHAR(255)       NOT NULL UNIQUE
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE qualifications (
  id           BIGINT PRIMARY KEY NOT NULL auto_increment,
  questions_id BIGINT             NOT NULL,
  teams_id     BIGINT             NOT NULL,
  FOREIGN KEY (teams_id) REFERENCES teams (id),
  FOREIGN KEY (questions_id) REFERENCES questions (id)
)ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE assignments (
  id              BIGINT PRIMARY KEY NOT NULL auto_increment,
  created_at      BIGINT             NOT NULL,
  expiration_time BIGINT             NOT NULL,
  is_cancelled    BIT                NOT NULL,
  questions_id    BIGINT             NOT NULL,
  teams_id        BIGINT             NOT NULL,
  FOREIGN KEY (teams_id) REFERENCES teams (id),
  FOREIGN KEY (questions_id) REFERENCES questions (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE answers (
  id             BIGINT PRIMARY KEY NOT NULL auto_increment,
  answer         TEXT,
  created_at     BIGINT             NOT NULL,
  accepted       BIT                NULL,
  bonus_cts      INT                NULL,
  rejected       BIT                NULL,
  assignments_id BIGINT             NOT NULL,
  FOREIGN KEY (assignments_id) REFERENCES assignments (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE feedbacks (
  id         BIGINT PRIMARY KEY NOT NULL auto_increment,
  useful     INT                NOT NULL,
  answers_id BIGINT             NOT NULL,
  FOREIGN KEY (answers_id) REFERENCES answers (id)
) ENGINE = InnoDB CHARSET = utf8;

CREATE TABLE turkers (
  id          BIGINT PRIMARY KEY NOT NULL auto_increment,
  turker_id   VARCHAR(100)       NOT NULL UNIQUE,
  created_at  BIGINT             NOT NULL,
  email       VARCHAR(255)       NULL,
  login_time  BIGINT             NOT NULL,
  logout_time BIGINT             NULL,
  username    VARCHAR(100)       NOT NULL UNIQUE,
  password    VARCHAR(100)       NOT NULL,
  layout_mode INT                NULL,
  feedback    VARCHAR(10000)     NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE turkers2teams (
  id         BIGINT PRIMARY KEY NOT NULL auto_increment,
  turkers_id BIGINT             NOT NULL,
  teams_id   BIGINT             NOT NULL,
  FOREIGN KEY (turkers_id) REFERENCES turkers (id),
  FOREIGN KEY (teams_id) REFERENCES teams (id)
) ENGINE = InnoDB CHARSET = utf8;

# --- !Downs

DROP TABLE turkers2teams;
DROP TABLE turkers;
DROP TABLE feedbacks;
DROP TABLE assignments;
DROP TABLE answers;
DROP TABLE teams;
DROP TABLE highlights;
DROP TABLE questions;
DROP TABLE datasets;
DROP TABLE papers;
DROP TABLE datasets2papers;
DROP TABLE qualifications;