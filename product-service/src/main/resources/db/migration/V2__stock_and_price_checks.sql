-- V2: database-level guards for the domain invariants (L25 context map).
ALTER TABLE products ADD CONSTRAINT ck_products_stock_non_negative CHECK (stock_quantity >= 0);
ALTER TABLE products ADD CONSTRAINT ck_products_price_positive     CHECK (price > 0);