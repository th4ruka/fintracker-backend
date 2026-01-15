-- V3: Create financial records system tables

-- Create category table
CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    category_type VARCHAR(20) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#000000',
    icon VARCHAR(50),
    created_date DATE NOT NULL,
    is_system_default BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_category_per_user (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create label table
CREATE TABLE label (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) NOT NULL DEFAULT '#000000',
    created_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_label_per_user (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create template table
CREATE TABLE template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(19, 2),
    record_type VARCHAR(20) NOT NULL,
    category_id BIGINT,
    account_id BIGINT,
    note VARCHAR(500),
    payment_type VARCHAR(20),
    payer VARCHAR(100),
    created_date DATE NOT NULL,
    to_account_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE SET NULL,
    FOREIGN KEY (to_account_id) REFERENCES account(id) ON DELETE SET NULL,
    UNIQUE KEY unique_template_per_user (user_id, name),
    CHECK (amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create template_label join table
CREATE TABLE template_label (
    template_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    PRIMARY KEY (template_id, label_id),
    FOREIGN KEY (template_id) REFERENCES template(id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES label(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create financial_record table
CREATE TABLE financial_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    record_type VARCHAR(20) NOT NULL,
    category_id BIGINT,
    note VARCHAR(1000),
    payer VARCHAR(100),
    payment_type VARCHAR(20),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'CLEARED',
    record_date DATE NOT NULL,
    record_time TIME,
    created_date DATETIME NOT NULL,
    updated_date DATETIME,
    to_account_id BIGINT,
    created_from_template BOOLEAN NOT NULL DEFAULT FALSE,
    template_id BIGINT,
    can_create_template BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL,
    FOREIGN KEY (to_account_id) REFERENCES account(id) ON DELETE SET NULL,
    CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create financial_record_label join table
CREATE TABLE financial_record_label (
    record_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    PRIMARY KEY (record_id, label_id),
    FOREIGN KEY (record_id) REFERENCES financial_record(id) ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES label(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for better query performance
CREATE INDEX idx_category_user ON category(user_id);
CREATE INDEX idx_category_type ON category(category_type);
CREATE INDEX idx_category_system_default ON category(is_system_default);

CREATE INDEX idx_label_user ON label(user_id);

CREATE INDEX idx_template_user ON template(user_id);
CREATE INDEX idx_template_record_type ON template(record_type);
CREATE INDEX idx_template_category ON template(category_id);
CREATE INDEX idx_template_account ON template(account_id);

CREATE INDEX idx_record_user ON financial_record(user_id);
CREATE INDEX idx_record_account ON financial_record(account_id);
CREATE INDEX idx_record_category ON financial_record(category_id);
CREATE INDEX idx_record_type ON financial_record(record_type);
CREATE INDEX idx_record_date ON financial_record(record_date);
CREATE INDEX idx_record_payment_status ON financial_record(payment_status);
CREATE INDEX idx_record_template ON financial_record(template_id);
CREATE INDEX idx_record_to_account ON financial_record(to_account_id);
CREATE INDEX idx_record_created_date ON financial_record(created_date);
