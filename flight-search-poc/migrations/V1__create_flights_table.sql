CREATE TABLE IF NOT EXISTS flights (
  _id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  flight_id       VARCHAR NOT NULL UNIQUE,
  airline         VARCHAR NOT NULL,
  departure_city  VARCHAR NOT NULL,
  arrival_city    VARCHAR NOT NULL,
  departure_time  TIMESTAMPTZ NOT NULL,
  base_price      NUMERIC(12,2) NOT NULL CHECK (base_price >= 0)
);

