CREATE TABLE IF NOT EXISTS balance(
  owner VARCHAR NOT NULL,
  symbol VARCHAR NOT NULL,
  amount NUMERIC NOT NULL,
  PRIMARY KEY (owner, symbol)
);