ALTER TABLE inventory_items
    ADD COLUMN price DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN description TEXT,
    ADD COLUMN category VARCHAR(255);

UPDATE inventory_items 
    SET price = 99.99, description = 'Default product description', category = 'General' 
    WHERE product_name = 'DEFAULT_PRODUCT';
