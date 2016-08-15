CREATE TABLE IF NOT EXISTS league_games (
  id            INT(11) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  summoner_name VARCHAR(800) NOT NULL,
  game_id         VARCHAR(80)  NOT NULL,
  timestamp     TIMESTAMP    NOT NULL
);


INSERT INTO league_games (summoner_name, game_id, timestamp) VALUES ('Pawel', -1, NOW());
INSERT INTO league_games (summoner_name, game_id, timestamp) VALUES ('Pawel', '100L', NOW());
