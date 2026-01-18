-- Add authentication fields to user table
ALTER TABLE user
    ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN roles VARCHAR(255) NOT NULL DEFAULT 'USER';

-- Create index on email for faster authentication lookups
CREATE INDEX idx_user_email ON user(email);

-- Note: Default empty password will need to be updated for existing users
-- New users created through /api/auth/register will have proper bcrypt passwords
