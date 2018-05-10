CREATE TABLE IF NOT EXISTS symbol_price(
  symbol VARCHAR NOT NULL,
  price_usd NUMERIC NOT NULL,
  price_btc NUMERIC NOT NULL,
  rank NUMERIC NOT NULL,
  PRIMARY KEY (symbol)
);