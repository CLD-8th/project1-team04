INSERT IGNORE INTO user (id) VALUES (1);
INSERT IGNORE INTO product (id, seller_id, title, content, category, image_path, price, status, created_at) VALUES (1, 1, 'Initial Product 1', 'Description for product 1', 'Electronics', '/images/1.png', 10000, 'SELLING', CURRENT_TIMESTAMP);
INSERT IGNORE INTO product (id, seller_id, title, content, category, image_path, price, status, created_at) VALUES (2, 1, 'Initial Product 2', 'Description for product 2', 'Books', '/images/2.png', 5000, 'SELLING', CURRENT_TIMESTAMP);
