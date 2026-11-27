-- Repeatable seed for dev and test ONLY (location db/seed is not configured in prod).
-- Re-applied whenever this file changes, so every statement is idempotent.

INSERT INTO categories (id, name) VALUES
                                      (1, 'Home Appliances'),
                                      (2, 'Laptops')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO products (id, name, category_id, price, stock_quantity) VALUES
                                                                        (101, 'Samsung Air Conditioner',       1,  85000.00, 15),
                                                                        (102, 'LG Front Load Washing Machine', 1,  62000.00,  8),
                                                                        (103, 'Lenovo ThinkPad E14',           2, 115000.00,  5)
    ON CONFLICT (id) DO NOTHING;

-- Explicit ids do not advance identity sequences: move them past the highest id (next: 3 and 104).
SELECT setval(pg_get_serial_sequence('categories', 'id'), (SELECT max(id) FROM categories));
SELECT setval(pg_get_serial_sequence('products',   'id'), (SELECT max(id) FROM products));