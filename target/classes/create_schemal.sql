CREATE TABLE wallet (
  id BIGINT,
  balance DECIMAL(18,2),
  status TINYINT,
  PRIMARY KEY (id)
);

CREATE TABLE wallet_history (
  id VARCHAR(64),
  wallet_id BIGINT,
  before DECIMAL(18,2),
  after DECIMAL(18,2),
  amount DECIMAL(18,2),
  ref VARCHAR(64),
  createtime TIMESTAMP,
  type VARCHAR(16),
  description VARCHAR(128),
  PRIMARY KEY (id),
  UNIQUE (ref, type)
);