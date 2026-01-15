-- Create users table
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    dob DATE
);

-- Create accounts table
CREATE TABLE account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255),
    balance DECIMAL(19, 2),
    created_date DATE,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Create index on user_id for better query performance
CREATE INDEX idx_account_user_id ON account(user_id);
