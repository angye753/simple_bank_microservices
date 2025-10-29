DROP INDEX IF EXISTS idx_tx_account_id;
DROP INDEX IF EXISTS idx_tx_sender_account_id;
DROP INDEX IF EXISTS idx_tx_recipient_account_id;

DROP TABLE IF EXISTS ledger CASCADE;
DROP TABLE IF EXISTS ledger_view CASCADE;
DROP TABLE IF EXISTS bank_transaction CASCADE;
DROP TABLE IF EXISTS card CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS account_view CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

CREATE TABLE app_user (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    username VARCHAR(200) NOT NULL,
    password VARCHAR(200) NOT NULL,
    contact_number VARCHAR(50),
    address VARCHAR(255),
    role VARCHAR(20) NOT NULL
);

CREATE TABLE account (
    id VARCHAR(50) PRIMARY KEY,
    account_description VARCHAR(255),
    app_user_id VARCHAR(50) REFERENCES app_user(id) ON DELETE SET NULL,
    balance NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(20)
);

CREATE TABLE account_view (
    id VARCHAR(50) PRIMARY KEY,
    account_description VARCHAR(255),
    app_user_id VARCHAR(50) REFERENCES app_user(id) ON DELETE SET NULL,
    balance NUMERIC(18, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(20)
);

CREATE INDEX idx_account_app_user_id ON account(app_user_id);

CREATE TABLE card (
    card_number VARCHAR(50) PRIMARY KEY,
    card_type VARCHAR(20) NOT NULL,
    account_id VARCHAR(50) NOT NULL UNIQUE REFERENCES account(id) ON DELETE CASCADE
);

CREATE INDEX idx_card_account_id ON card(account_id);

CREATE TABLE bank_transaction (
    id BIGSERIAL PRIMARY KEY,
    account_id VARCHAR(50) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    amount NUMERIC(18, 2) NOT NULL CHECK (amount >= 0),
    sender_account_id VARCHAR(50) REFERENCES account(id) ON DELETE SET NULL,
    recipient_account_id VARCHAR(50) REFERENCES account(id) ON DELETE SET NULL,
    date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255),
    state VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE
);

CREATE INDEX idx_tx_account_id ON bank_transaction(account_id);
CREATE INDEX idx_tx_sender_account_id ON bank_transaction(sender_account_id);
CREATE INDEX idx_tx_recipient_account_id ON bank_transaction(recipient_account_id);

CREATE TABLE ledger (
    id BIGSERIAL PRIMARY KEY,
    owner_account_id VARCHAR(50) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    transaction_id BIGINT NOT NULL REFERENCES bank_transaction(id) ON DELETE CASCADE,
    amount NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'EUR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ledger_owner_account_id ON ledger(owner_account_id);
CREATE INDEX idx_ledger_transaction_id ON ledger(transaction_id);

CREATE TABLE ledger_view (
    id BIGSERIAL PRIMARY KEY,
    owner_account_id VARCHAR(50) NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    transaction_id BIGINT NOT NULL REFERENCES bank_transaction(id) ON DELETE CASCADE,
    amount NUMERIC(18, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'EUR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ledger_owner_account_id_view ON ledger_view(owner_account_id);
CREATE INDEX idx_ledger_transaction_id_view ON ledger_view(transaction_id);

INSERT INTO app_user (id, name, username, password, contact_number, address, role) VALUES
('U1', 'Alice Doe', 'alice.doe', '$2a$10$SCosLY.wbz0akrGtrPXoK.1rzSPEHNJxMXvv/vBvdkNduwB1VHsRi', '555-1001', 'Amsterdam', 'user'),
('U2', 'Bob Smith', 'bob.smith', '$2a$10$SCosLY.wbz0akrGtrPXoK.1rzSPEHNJxMXvv/vBvdkNduwB1VHsRi', '555-2002', 'Rotterdam', 'user'),
('U3', 'Pedro Suarez', 'pedro.suarez', '$2a$10$SCosLY.wbz0akrGtrPXoK.1rzSPEHNJxMXvv/vBvdkNduwB1VHsRi', '555-7654', 'Rotterdam', 'admin');

INSERT INTO account (id, account_description, app_user_id, balance, currency) VALUES
('RB12_12324', 'Alice Checking', 'U1', 5000.00, 'EU'),
('RB12_12335', 'Bob Savings', 'U2', 2000.00, 'EU'),
('RB12_12336', 'Bob Credit', 'U2', 2000.00, 'EU');

INSERT INTO account_view (id, account_description, app_user_id, balance, currency) VALUES
('RB12_12324', 'Alice Checking', 'U1', 5000.00, 'EU'),
('RB12_12335', 'Bob Savings', 'U2', 2000.00, 'EU'),
('RB12_12336', 'Bob Credit', 'U2', 2000.00, 'EU');

INSERT INTO card (card_number, card_type, account_id) VALUES
('4111111111111111', 'DEBIT', 'RB12_12324'),
('5555555555554444', 'DEBIT', 'RB12_12335'),
('5555533333334444', 'CREDIT', 'RB12_12336');

