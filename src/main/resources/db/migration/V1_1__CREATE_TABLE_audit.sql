-- THIS SHOULD ONLY BE RUN IN DEVELOPERS DATABASES - NOT ON ANY ENVIRONMENTS
-- IT SHOULD ALWAYS BE A COPY OF THE MASTER FILE IN pttg-ip-api, THEN REMOVED WHEN WE HAVE AN AUDIT SERVICE COMPONENT
CREATE TABLE IF NOT EXISTS audit
(
  id             BIGSERIAL PRIMARY KEY,
  uuid           VARCHAR(255) NOT NULL,
  timestamp      TIMESTAMP    NOT NULL,
  session_id     VARCHAR(255) NOT NULL,
  correlation_id VARCHAR(255) NOT NULL,
  user_id        VARCHAR(255) NOT NULL,
  deployment     VARCHAR(255) NOT NULL,
  namespace      VARCHAR(255) NOT NULL,
  type           VARCHAR(255) NOT NULL,
  detail         TEXT         NOT NULL,

  CONSTRAINT audit_idempotent UNIQUE (uuid,type)
);
