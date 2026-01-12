-- Add common fields
ALTER TABLE account
ADD COLUMN color VARCHAR(7) DEFAULT '#000000' COMMENT 'Hex color code',
ADD COLUMN account_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
ADD COLUMN initial_amount DECIMAL(19, 2) DEFAULT 0.00,
ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'LKR',
ADD COLUMN exclude_from_statistics BOOLEAN NOT NULL DEFAULT FALSE;

-- Add credit account fields
ALTER TABLE account
ADD COLUMN credit_card_limit DECIMAL(19, 2) NULL,
ADD COLUMN credit_due_day_of_month INT NULL,
ADD COLUMN credit_balance_type VARCHAR(20) NULL;

-- Add overdraft account fields
ALTER TABLE account
ADD COLUMN overdraft_limit DECIMAL(19, 2) NULL,
ADD COLUMN overdraft_due_day_of_month INT NULL,
ADD COLUMN overdraft_balance_type VARCHAR(20) NULL;

-- Add constraints
ALTER TABLE account
ADD CONSTRAINT chk_account_type
    CHECK (account_type IN ('GENERAL', 'CREDIT_ACCOUNT', 'OVERDRAFT_ACCOUNT')),
ADD CONSTRAINT chk_credit_balance_type
    CHECK (credit_balance_type IS NULL OR credit_balance_type IN ('CREDIT_BALANCE', 'AVAILABLE_CREDIT')),
ADD CONSTRAINT chk_overdraft_balance_type
    CHECK (overdraft_balance_type IS NULL OR overdraft_balance_type IN ('ACTUAL_BALANCE', 'AVAILABLE_BALANCE')),
ADD CONSTRAINT chk_credit_due_day
    CHECK (credit_due_day_of_month IS NULL OR (credit_due_day_of_month >= 1 AND credit_due_day_of_month <= 31)),
ADD CONSTRAINT chk_overdraft_due_day
    CHECK (overdraft_due_day_of_month IS NULL OR (overdraft_due_day_of_month >= 1 AND overdraft_due_day_of_month <= 31)),
ADD CONSTRAINT chk_credit_limit_positive
    CHECK (credit_card_limit IS NULL OR credit_card_limit >= 0),
ADD CONSTRAINT chk_overdraft_limit_positive
    CHECK (overdraft_limit IS NULL OR overdraft_limit >= 0);

-- Set initial_amount to current balance for existing accounts
UPDATE account SET initial_amount = balance WHERE initial_amount = 0.00;

-- Add indexes for filtering
CREATE INDEX idx_account_type ON account(account_type);
CREATE INDEX idx_exclude_from_statistics ON account(exclude_from_statistics);
