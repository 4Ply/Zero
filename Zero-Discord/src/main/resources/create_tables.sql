CREATE TABLE IF NOT EXISTS discord_logs (
  id             INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  message        VARCHAR(800) NOT NULL,
  author_id      VARCHAR(80)  NOT NULL,
  direct_message TINYINT(1)   NOT NULL,
  type           VARCHAR(10)  NOT NULL,
  timestamp      TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS manga_items (
  id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  manga_name VARCHAR(30) NOT NULL
);
ALTER TABLE `manga_items` ADD UNIQUE `unique_index`(`manga_name`, `id`);

ALTER TABLE manga_items MODIFY COLUMN manga_name VARCHAR(20)
CHARACTER SET UTF8
COLLATE UTF8_GENERAL_CI;

CREATE TABLE IF NOT EXISTS manga_jids (
  id INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  manga_name VARCHAR(30) NOT NULL,
  jid VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS mangafox_chapter_stage (
  id INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  manga_id INT(11) NOT NULL,
  chapter_name VARCHAR(30) NOT NULL,
  url VARCHAR(200) NOT NULL,
  timestamp VARCHAR(50) NOT NULL,
  processed TINYINT(1) NOT NULL,
  created TIMESTAMP NOT NULL
);
ALTER TABLE `mangafox_chapter_stage` ADD UNIQUE `unique_index`(`manga_id`, `chapter_name`);

CREATE TABLE IF NOT EXISTS mangafox_urls (
  id INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  manga_id INT(11) NOT NULL,
  url VARCHAR(200) NOT NULL
);
ALTER TABLE `mangafox_urls` ADD UNIQUE `unique_index`(`manga_id`, `url`);

CREATE TABLE IF NOT EXISTS whatsapp_logs (
  id        INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  message   VARCHAR(800) NOT NULL,
  jid       VARCHAR(80)  NOT NULL,
  type      VARCHAR(10)  NOT NULL,
  timestamp TIMESTAMP    NOT NULL
);
