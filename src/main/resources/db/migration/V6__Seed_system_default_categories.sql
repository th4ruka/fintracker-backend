-- V6: Seed global system default categories
-- System defaults are shared by all users, so they have no owner: user_id
-- becomes nullable and seed rows use NULL. Repository queries already treat
-- them as global (is_system_default = TRUE OR user_id = :userId).
ALTER TABLE category MODIFY user_id BIGINT NULL;

INSERT INTO category (user_id, name, category_type, color, icon, created_date, is_system_default) VALUES
    (NULL, 'Groceries',     'EXPENSE', '#1a7f5a', '🛒', CURRENT_DATE, TRUE),
    (NULL, 'Transport',     'EXPENSE', '#d97706', '🚌', CURRENT_DATE, TRUE),
    (NULL, 'Dining',        'EXPENSE', '#b45309', '🍽️', CURRENT_DATE, TRUE),
    (NULL, 'Entertainment', 'EXPENSE', '#7c5ce0', '🎬', CURRENT_DATE, TRUE),
    (NULL, 'Health',        'EXPENSE', '#d6486a', '💊', CURRENT_DATE, TRUE),
    (NULL, 'Utilities',     'EXPENSE', '#3b5bad', '💡', CURRENT_DATE, TRUE),
    (NULL, 'Housing',       'EXPENSE', '#0369a1', '🏠', CURRENT_DATE, TRUE),
    (NULL, 'Shopping',      'EXPENSE', '#0d9488', '🛍️', CURRENT_DATE, TRUE),
    (NULL, 'Salary',        'INCOME',  '#15803d', '💼', CURRENT_DATE, TRUE),
    (NULL, 'Freelance',     'INCOME',  '#0d9488', '💻', CURRENT_DATE, TRUE),
    (NULL, 'Investments',   'INCOME',  '#1a7f5a', '📈', CURRENT_DATE, TRUE),
    (NULL, 'Other income',  'INCOME',  '#0369a1', '🪙', CURRENT_DATE, TRUE);
