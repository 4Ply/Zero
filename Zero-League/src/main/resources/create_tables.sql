CREATE TABLE IF NOT EXISTS tracked_games (
  id INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  game_id BIGINT NOT NULL,
  processed TINYINT(1) NOT NULL,
  summoner_name VARCHAR(200) NOT NULL
);
ALTER TABLE `tracked_games` ADD UNIQUE `unique_index`(`game_id`, `summoner_name`);

CREATE TABLE IF NOT EXISTS tracked_summoners (
  id INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  summoner_name VARCHAR(200) NOT NULL,
  trackerJID VARCHAR(200) NOT NULL
);
ALTER TABLE `tracked_summoners` ADD UNIQUE `unique_index`(`summoner_name`, `trackerJID`);
