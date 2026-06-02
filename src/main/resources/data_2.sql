DELETE
FROM product_variant;

DELETE
FROM laptops;

DELETE
FROM products;

DELETE
FROM colors;

INSERT INTO colors (id, hex_code, name)
VALUES (1, '#000000', 'Black'),
       (2, '#FFFFFF', 'White'),
       (3, '#C0C0C0', 'Silver'),
       (4, '#1E90FF', 'Blue'),
       (5, '#FF0000', 'Red');

INSERT INTO products (id, product_type, brand, created, description, name, thumbnail)
VALUES (1, 'laptop', 'Dell', '2026-01-01 10:00:00', 'Laptop Dell for office and study', 'Dell Inspiron 15',
        'dell_inspiron_15.jpg'),
       (2, 'laptop', 'Dell', '2026-01-02 10:00:00', 'High performance Dell laptop', 'Dell XPS 13', 'dell_xps_13.jpg'),
       (3, 'laptop', 'HP', '2026-01-03 10:00:00', 'HP laptop for students', 'HP Pavilion 14', 'hp_pavilion_14.jpg'),
       (4, 'laptop', 'HP', '2026-01-04 10:00:00', 'Gaming HP laptop', 'HP Omen 16', 'hp_omen_16.jpg'),
       (5, 'laptop', 'Lenovo', '2026-01-05 10:00:00', 'Lenovo ThinkPad business laptop', 'ThinkPad X1 Carbon',
        'thinkpad_x1.jpg'),
       (6, 'laptop', 'Lenovo', '2026-01-06 10:00:00', 'Affordable Lenovo laptop', 'Lenovo IdeaPad 3', 'ideapad_3.jpg'),
       (7, 'laptop', 'Asus', '2026-01-07 10:00:00', 'Gaming laptop Asus', 'Asus ROG Strix', 'rog_strix.jpg'),
       (8, 'laptop', 'Asus', '2026-01-08 10:00:00', 'Thin and light Asus laptop', 'Asus ZenBook 14', 'zenbook_14.jpg'),
       (9, 'laptop', 'Acer', '2026-01-09 10:00:00', 'Acer Aspire series', 'Acer Aspire 5', 'aspire_5.jpg'),
       (10, 'laptop', 'Acer', '2026-01-10 10:00:00', 'Gaming Acer Nitro', 'Acer Nitro 5', 'nitro_5.jpg'),
       (11, 'laptop', 'Apple', '2026-01-11 10:00:00', 'MacBook Air M2', 'MacBook Air 13 M2', 'macbook_air_m2.jpg'),
       (12, 'laptop', 'Apple', '2026-01-12 10:00:00', 'MacBook Pro performance', 'MacBook Pro 14 M3',
        'macbook_pro_14.jpg'),
       (13, 'laptop', 'MSI', '2026-01-13 10:00:00', 'MSI gaming laptop', 'MSI GF63 Thin', 'msi_gf63.jpg'),
       (14, 'laptop', 'MSI', '2026-01-14 10:00:00', 'High-end MSI laptop', 'MSI Stealth 15', 'msi_stealth_15.jpg'),
       (15, 'laptop', 'Gigabyte', '2026-01-15 10:00:00', 'Gaming laptop Gigabyte', 'Gigabyte G5', 'gigabyte_g5.jpg');

INSERT INTO laptops (id, cpu, dimension, gpu, ram, resolution, screen_size, storage)
VALUES (1, 'Intel i5-1235U', '358 x 235 x 19 mm', 'Intel Iris Xe', '8GB', '1920x1080', 15.6, '512GB SSD'),
       (2, 'Intel i7-1360P', '295 x 199 x 14 mm', 'Intel Iris Xe', '16GB', '1920x1200', 13.4, '1TB SSD'),
       (3, 'Intel i5-1240P', '324 x 225 x 18 mm', 'Intel Iris Xe', '8GB', '1920x1080', 14.0, '512GB SSD'),
       (4, 'Intel i7-12700H', '369 x 248 x 23 mm', 'RTX 3060', '16GB', '2560x1440', 16.1, '1TB SSD'),
       (5, 'Intel i7-1355U', '315 x 222 x 15 mm', 'Intel Iris Xe', '16GB', '2240x1400', 14.0, '512GB SSD'),
       (6, 'AMD Ryzen 5 5500U', '362 x 251 x 20 mm', 'Radeon Graphics', '8GB', '1920x1080', 15.6, '512GB SSD'),
       (7, 'Intel i9-13900H', '354 x 264 x 22 mm', 'RTX 4060', '16GB', '2560x1440', 15.6, '1TB SSD'),
       (8, 'Intel i7-1260P', '313 x 220 x 16 mm', 'Intel Iris Xe', '16GB', '2880x1800', 14.0, '512GB SSD'),
       (9, 'AMD Ryzen 5 5625U', '363 x 238 x 19 mm', 'Radeon Graphics', '8GB', '1920x1080', 15.6, '512GB SSD'),
       (10, 'Intel i5-12500H', '360 x 270 x 24 mm', 'RTX 3050', '16GB', '1920x1080', 15.6, '512GB SSD'),
       (11, 'Apple M2', '304 x 215 x 11 mm', 'Apple GPU', '8GB', '2560x1664', 13.6, '256GB SSD'),
       (12, 'Apple M3 Pro', '312 x 221 x 16 mm', 'Apple GPU 18-core', '18GB', '3024x1964', 14.2, '1TB SSD'),
       (13, 'Intel i5-12450H', '359 x 254 x 21 mm', 'RTX 2050', '8GB', '1920x1080', 15.6, '512GB SSD'),
       (14, 'Intel i7-13700H', '358 x 248 x 20 mm', 'RTX 4070', '32GB', '2560x1600', 15.6, '1TB SSD'),
       (15, 'Intel i5-12500H', '360 x 255 x 23 mm', 'RTX 4050', '16GB', '1920x1080', 15.6, '512GB SSD');

INSERT INTO product_variant (id, active, image, is_default, price, sku, stock_quantity, version, color_id, product_id)
VALUES (1, 1, 'dell_inspiron_black.jpg', 1, 15000000, 'DELL-INSP-15-BLK', 10, 1, 1, 1),
       (2, 1, 'dell_inspiron_silver.jpg', 0, 15200000, 'DELL-INSP-15-SLV', 8, 1, 3, 1),

       (3, 1, 'dell_xps_black.jpg', 1, 28000000, 'DELL-XPS-13-BLK', 5, 1, 1, 2),
       (4, 1, 'dell_xps_white.jpg', 0, 28500000, 'DELL-XPS-13-WHT', 4, 1, 2, 2),

       (5, 1, 'hp_pavilion_silver.jpg', 1, 14000000, 'HP-PAV-14-SLV', 12, 1, 3, 3),
       (6, 1, 'hp_pavilion_blue.jpg', 0, 14200000, 'HP-PAV-14-BLU', 9, 1, 4, 3),

       (7, 1, 'hp_omen_black.jpg', 1, 32000000, 'HP-OMEN-16-BLK', 6, 1, 1, 4),
       (8, 1, 'hp_omen_red.jpg', 0, 32500000, 'HP-OMEN-16-RED', 4, 1, 5, 4),

       (9, 1, 'thinkpad_black.jpg', 1, 26000000, 'LEN-TP-X1-BLK', 7, 1, 1, 5),
       (10, 1, 'thinkpad_silver.jpg', 0, 26200000, 'LEN-TP-X1-SLV', 6, 1, 3, 5),

       (11, 1, 'ideapad_black.jpg', 1, 12000000, 'LEN-IDEA-3-BLK', 15, 1, 1, 6),
       (12, 1, 'ideapad_silver.jpg', 0, 12200000, 'LEN-IDEA-3-SLV', 14, 1, 3, 6),

       (13, 1, 'rog_black.jpg', 1, 45000000, 'ASUS-ROG-BLK', 3, 1, 1, 7),
       (14, 1, 'rog_red.jpg', 0, 45500000, 'ASUS-ROG-RED', 2, 1, 5, 7),

       (15, 1, 'zenbook_silver.jpg', 1, 22000000, 'ASUS-ZEN-SLV', 8, 1, 3, 8),
       (16, 1, 'zenbook_blue.jpg', 0, 22500000, 'ASUS-ZEN-BLU', 6, 1, 4, 8),

       (17, 1, 'aspire_black.jpg', 1, 11000000, 'ACER-ASP-5-BLK', 20, 1, 1, 9),
       (18, 1, 'aspire_silver.jpg', 0, 11200000, 'ACER-ASP-5-SLV', 18, 1, 3, 9),

       (19, 1, 'nitro_black.jpg', 1, 18000000, 'ACER-NITRO-5-BLK', 10, 1, 1, 10),
       (20, 1, 'nitro_red.jpg', 0, 18500000, 'ACER-NITRO-5-RED', 8, 1, 5, 10),

       (21, 1, 'macbook_air_silver.jpg', 1, 27000000, 'APPLE-MBA-M2-SLV', 6, 1, 3, 11),
       (22, 1, 'macbook_air_white.jpg', 0, 27200000, 'APPLE-MBA-M2-WHT', 5, 1, 2, 11),

       (23, 1, 'macbook_pro_silver.jpg', 1, 42000000, 'APPLE-MBP-14-SLV', 4, 1, 3, 12),
       (24, 1, 'macbook_pro_black.jpg', 0, 42500000, 'APPLE-MBP-14-BLK', 3, 1, 1, 12),

       (25, 1, 'msi_gf_black.jpg', 1, 16000000, 'MSI-GF63-BLK', 12, 1, 1, 13),
       (26, 1, 'msi_gf_red.jpg', 0, 16500000, 'MSI-GF63-RED', 10, 1, 5, 13),

       (27, 1, 'msi_stealth_black.jpg', 1, 38000000, 'MSI-STEALTH-BLK', 5, 1, 1, 14),
       (28, 1, 'msi_stealth_silver.jpg', 0, 38500000, 'MSI-STEALTH-SLV', 4, 1, 3, 14),

       (29, 1, 'gigabyte_black.jpg', 1, 19000000, 'GIGA-G5-BLK', 9, 1, 1, 15),
       (30, 1, 'gigabyte_blue.jpg', 0, 19200000, 'GIGA-G5-BLU', 7, 1, 4, 15);


INSERT INTO products (id, product_type, brand, thumbnail, name, description, created)
VALUES (16, 'MOBILE', 'Apple', 'iphone14.jpg', 'iPhone 14', 'Previous generation Apple phone', NOW()),
       (17, 'MOBILE', 'Samsung', 's23.jpg', 'Galaxy S23', 'Compact flagship Samsung', NOW()),
       (18, 'MOBILE', 'Samsung', 'a55.jpg', 'Galaxy A55', 'Mid-range Samsung phone', NOW()),
       (19, 'MOBILE', 'Xiaomi', 'redmi13.jpg', 'Redmi Note 13', 'Budget Xiaomi smartphone', NOW()),
       (20, 'MOBILE', 'Xiaomi', 'pocoF6.jpg', 'Poco F6', 'Gaming budget phone', NOW()),
       (21, 'MOBILE', 'Oppo', 'reno10.jpg', 'Oppo Reno 10', 'Portrait camera phone', NOW()),
       (22, 'MOBILE', 'Vivo', 'v27.jpg', 'Vivo V27', 'Elegant design smartphone', NOW()),
       (23, 'MOBILE', 'OnePlus', 'op10t.jpg', 'OnePlus 10T', 'Fast charging flagship', NOW()),
       (24, 'MOBILE', 'Google', 'pixel7.jpg', 'Pixel 7', 'Clean Android experience', NOW()),
       (25, 'MOBILE', 'Realme', 'realme11.jpg', 'Realme 11 Pro', 'Stylish mid-range phone', NOW()),
       (26, 'MOBILE', 'Apple', 'iphone13.jpg', 'iPhone 13', 'Stable Apple performance', NOW()),
       (27, 'MOBILE', 'Samsung', 'zflip5.jpg', 'Galaxy Z Flip 5', 'Foldable compact phone', NOW()),
       (28, 'MOBILE', 'Samsung', 'zfold5.jpg', 'Galaxy Z Fold 5', 'Premium foldable device', NOW()),
       (29, 'MOBILE', 'Xiaomi', 'mi13.jpg', 'Xiaomi 13', 'Flagship Xiaomi model', NOW()),
       (30, 'MOBILE', 'Sony', 'xperia1v.jpg', 'Sony Xperia 1 V', 'Cinema-grade display phone', NOW());


INSERT INTO mobiles (id, model, screen_size, resolution, camera, battery, dimension)
VALUES (16, 'A15 Bionic', 6.1, '1170x2532', 'Dual 12MP', '3279mAh', '146.7 x 71.5 x 7.8 mm'),
       (17, 'Snapdragon 8 Gen 2', 6.1, '1080x2340', '50MP + 12MP', '3900mAh', '146.3 x 70.9 x 7.6 mm'),
       (18, 'Exynos 1480', 6.6, '1080x2340', '50MP + 12MP + 5MP', '5000mAh', '161.1 x 77.4 x 8.2 mm'),
       (19, 'Dimensity 6080', 6.67, '1080x2400', '108MP + 2MP', '5000mAh', '161.1 x 74.1 x 7.6 mm'),
       (20, 'Snapdragon 8s Gen 3', 6.67, '1220x2712', '64MP + 8MP', '5000mAh', '160.5 x 74.4 x 7.8 mm'),
       (21, 'Snapdragon 778G', 6.7, '1080x2412', '64MP + 32MP', '5000mAh', '162.3 x 74.2 x 7.9 mm'),
       (22, 'Dimensity 7200', 6.78, '1080x2400', '50MP + 2MP', '4600mAh', '164.1 x 74.8 x 7.4 mm'),
       (23, 'Snapdragon 8+ Gen 1', 6.7, '1080x2412', '50MP + 8MP', '4800mAh', '163 x 75.4 x 8.8 mm'),
       (24, 'Google Tensor G2', 6.3, '1080x2400', '50MP + 12MP', '4355mAh', '155.6 x 73.2 x 8.7 mm'),
       (25, 'Dimensity 7050', 6.7, '1080x2412', '100MP + 2MP', '5000mAh', '161.6 x 73.9 x 8.2 mm'),
       (26, 'A15 Bionic', 6.1, '1170x2532', 'Dual 12MP', '3240mAh', '146.7 x 71.5 x 7.7 mm'),
       (27, 'Snapdragon 8 Gen 2', 6.7, '2640x1080', '12MP + 12MP', '3700mAh', '165.1 x 71.9 x 6.9 mm'),
       (28, 'Snapdragon 8 Gen 2', 7.6, '2176x1812', '50MP + 12MP + 10MP', '4400mAh', '154.9 x 129.9 x 6.1 mm'),
       (29, 'Snapdragon 8 Gen 2', 6.36, '1080x2400', '50MP + 10MP + 12MP', '4500mAh', '152.8 x 71.5 x 8.0 mm'),
       (30, 'Snapdragon 8 Gen 2', 6.5, '1644x3840', '48MP + 12MP + 12MP', '5000mAh', '165 x 71 x 8.3 mm');

INSERT INTO product_variant (id, active, image, is_default, price, sku, stock_quantity, version, color_id, product_id)
VALUES

-- 16 iPhone 14
(60, 1, 'iphone14_black.jpg', 1, 18000000, 'IP14-BLK', 10, 1, 1, 16),
(31, 1, 'iphone14_blue.jpg', 0, 18200000, 'IP14-BLU', 8, 1, 4, 16),

-- 17 Galaxy S23
(32, 1, 's23_black.jpg', 1, 20000000, 'S23-BLK', 12, 1, 1, 17),
(33, 1, 's23_white.jpg', 0, 20200000, 'S23-WHT', 9, 1, 2, 17),

-- 18 Galaxy A55
(34, 1, 'a55_black.jpg', 1, 9000000, 'A55-BLK', 20, 1, 1, 18),
(35, 1, 'a55_blue.jpg', 0, 9200000, 'A55-BLU', 18, 1, 4, 18),

-- 19 Redmi Note 13
(36, 1, 'rn13_black.jpg', 1, 6500000, 'RN13-BLK', 25, 1, 1, 19),
(37, 1, 'rn13_blue.jpg', 0, 6700000, 'RN13-BLU', 20, 1, 4, 19),

-- 20 Poco F6
(38, 1, 'pocof6_black.jpg', 1, 11000000, 'POCOF6-BLK', 15, 1, 1, 20),
(39, 1, 'pocof6_red.jpg', 0, 11200000, 'POCOF6-RED', 12, 1, 5, 20),

-- 21 Oppo Reno 10
(40, 1, 'reno10_black.jpg', 1, 10000000, 'R10-BLK', 14, 1, 1, 21),
(41, 1, 'reno10_gold.jpg', 0, 10200000, 'R10-GLD', 10, 1, 2, 21),

-- 22 Vivo V27
(42, 1, 'v27_black.jpg', 1, 9500000, 'V27-BLK', 16, 1, 1, 22),
(43, 1, 'v27_blue.jpg', 0, 9700000, 'V27-BLU', 13, 1, 4, 22),

-- 23 OnePlus 10T
(44, 1, 'op10t_black.jpg', 1, 13000000, 'OP10T-BLK', 11, 1, 1, 23),
(45, 1, 'op10t_green.jpg', 0, 13200000, 'OP10T-GRN', 9, 1, 3, 23),

-- 24 Pixel 7
(46, 1, 'pixel7_black.jpg', 1, 12000000, 'PIX7-BLK', 10, 1, 1, 24),
(47, 1, 'pixel7_white.jpg', 0, 12200000, 'PIX7-WHT', 8, 1, 2, 24),

-- 25 Realme 11 Pro
(48, 1, 'r11pro_black.jpg', 1, 8000000, 'R11P-BLK', 18, 1, 1, 25),
(49, 1, 'r11pro_gold.jpg', 0, 8200000, 'R11P-GLD', 15, 1, 2, 25),

-- 26 iPhone 13
(50, 1, 'iphone13_black.jpg', 1, 15000000, 'IP13-BLK', 12, 1, 1, 26),
(51, 1, 'iphone13_red.jpg', 0, 15200000, 'IP13-RED', 10, 1, 5, 26),

-- 27 Z Flip 5
(52, 1, 'zflip5_black.jpg', 1, 25000000, 'ZFLIP5-BLK', 8, 1, 1, 27),
(53, 1, 'zflip5_purple.jpg', 0, 25200000, 'ZFLIP5-PUR', 6, 1, 4, 27),

-- 28 Z Fold 5
(54, 1, 'zfold5_black.jpg', 1, 35000000, 'ZFOLD5-BLK', 5, 1, 1, 28),
(55, 1, 'zfold5_silver.jpg', 0, 35200000, 'ZFOLD5-SLV', 4, 1, 3, 28),

-- 29 Xiaomi 13
(56, 1, 'mi13_black.jpg', 1, 17000000, 'MI13-BLK', 9, 1, 1, 29),
(57, 1, 'mi13_white.jpg', 0, 17200000, 'MI13-WHT', 7, 1, 2, 29),

-- 30 Sony Xperia 1 V
(58, 1, 'xperia1v_black.jpg', 1, 26000000, 'XPERIA1V-BLK', 6, 1, 1, 30),
(59, 1, 'xperia1v_purple.jpg', 0, 26200000, 'XPERIA1V-PUR', 5, 1, 4, 30);

INSERT INTO products (id, product_type, brand, thumbnail, name, description, created)
VALUES (31, 'TELEVISION', 'Samsung', 'samsung_qled_55.jpg', 'Samsung QLED 55"', 'Premium QLED TV', NOW()),
       (32, 'TELEVISION', 'Samsung', 'samsung_qled_65.jpg', 'Samsung QLED 65"', 'High-end QLED experience', NOW()),
       (33, 'TELEVISION', 'LG', 'lg_oled_55.jpg', 'LG OLED 55"', 'Perfect black OLED display', NOW()),
       (34, 'TELEVISION', 'LG', 'lg_oled_65.jpg', 'LG OLED 65"', 'Cinema-grade OLED TV', NOW()),
       (35, 'TELEVISION', 'Sony', 'sony_bravia_55.jpg', 'Sony Bravia 55"', 'Bravia 4K HDR TV', NOW()),
       (36, 'TELEVISION', 'Sony', 'sony_bravia_65.jpg', 'Sony Bravia 65"', 'Premium Bravia series', NOW()),
       (37, 'TELEVISION', 'TCL', 'tcl_4k_50.jpg', 'TCL 50" 4K', 'Affordable 4K smart TV', NOW()),
       (38, 'TELEVISION', 'TCL', 'tcl_4k_55.jpg', 'TCL 55" 4K', 'Budget 4K entertainment', NOW()),
       (39, 'TELEVISION', 'Xiaomi', 'mi_tv_55.jpg', 'Xiaomi TV 55"', 'Smart TV Android system', NOW()),
       (40, 'TELEVISION', 'Xiaomi', 'mi_tv_65.jpg', 'Xiaomi TV 65"', 'Large screen smart TV', NOW()),
       (41, 'TELEVISION', 'Panasonic', 'panasonic_50.jpg', 'Panasonic 50"', 'Reliable 4K TV', NOW()),
       (42, 'TELEVISION', 'Panasonic', 'panasonic_55.jpg', 'Panasonic 55"', 'HDR smart TV', NOW()),
       (43, 'TELEVISION', 'Hisense', 'hisense_50.jpg', 'Hisense 50"', 'Budget smart TV', NOW()),
       (44, 'TELEVISION', 'Hisense', 'hisense_55.jpg', 'Hisense 55"', 'Good value 4K TV', NOW()),
       (45, 'TELEVISION', 'Sony', 'sony_oled_65.jpg', 'Sony OLED 65"', 'High-end OLED flagship TV', NOW());

INSERT INTO televisions (id, resolution, refresh_rate, screen_size, weight, warranty_months)
VALUES (31, '4K', 120, 55.0, 16.5, 24),
       (32, '4K', 120, 65.0, 20.0, 24),
       (33, '4K', 120, 55.0, 15.8, 24),
       (34, '4K', 120, 65.0, 19.5, 24),
       (35, '4K', 60, 55.0, 14.2, 24),
       (36, '4K', 60, 65.0, 18.0, 24),
       (37, '4K', 60, 50.0, 12.0, 12),
       (38, '4K', 60, 55.0, 13.5, 12),
       (39, '4K', 60, 55.0, 11.8, 24),
       (40, '4K', 60, 65.0, 15.0, 24),
       (41, '4K', 60, 50.0, 13.0, 24),
       (42, '4K', 60, 55.0, 14.0, 24),
       (43, 'Full HD', 60, 50.0, 10.5, 12),
       (44, '4K', 60, 55.0, 12.5, 12),
       (45, '4K', 120, 65.0, 21.0, 36);

INSERT INTO product_variant (id, active, image, is_default, price, sku, stock_quantity, version, color_id, product_id)
VALUES

-- 31 Samsung QLED 55
(90, 1, 'samsung_qled55_black.jpg', 1, 18000000, 'SAMS-Q55-BLK', 10, 1, 1, 31),
(61, 1, 'samsung_qled55_silver.jpg', 0, 18200000, 'SAMS-Q55-SLV', 8, 1, 3, 31),

-- 32 Samsung QLED 65
(62, 1, 'samsung_qled65_black.jpg', 1, 24000000, 'SAMS-Q65-BLK', 8, 1, 1, 32),
(63, 1, 'samsung_qled65_silver.jpg', 0, 24200000, 'SAMS-Q65-SLV', 6, 1, 3, 32),

-- 33 LG OLED 55
(64, 1, 'lg_oled55_black.jpg', 1, 22000000, 'LG-O55-BLK', 9, 1, 1, 33),
(65, 1, 'lg_oled55_gray.jpg', 0, 22200000, 'LG-O55-GRY', 7, 1, 3, 33),

-- 34 LG OLED 65
(66, 1, 'lg_oled65_black.jpg', 1, 30000000, 'LG-O65-BLK', 6, 1, 1, 34),
(67, 1, 'lg_oled65_gray.jpg', 0, 30200000, 'LG-O65-GRY', 5, 1, 3, 34),

-- 35 Sony Bravia 55
(68, 1, 'sony55_black.jpg', 1, 19000000, 'SONY-B55-BLK', 10, 1, 1, 35),
(69, 1, 'sony55_silver.jpg', 0, 19200000, 'SONY-B55-SLV', 8, 1, 3, 35),

-- 36 Sony Bravia 65
(70, 1, 'sony65_black.jpg', 1, 26000000, 'SONY-B65-BLK', 7, 1, 1, 36),
(71, 1, 'sony65_silver.jpg', 0, 26200000, 'SONY-B65-SLV', 5, 1, 3, 36),

-- 37 TCL 50 4K
(72, 1, 'tcl50_black.jpg', 1, 9000000, 'TCL50-BLK', 15, 1, 1, 37),
(73, 1, 'tcl50_gray.jpg', 0, 9200000, 'TCL50-GRY', 12, 1, 3, 37),

-- 38 TCL 55 4K
(74, 1, 'tcl55_black.jpg', 1, 10000000, 'TCL55-BLK', 14, 1, 1, 38),
(75, 1, 'tcl55_gray.jpg', 0, 10200000, 'TCL55-GRY', 10, 1, 3, 38),

-- 39 Xiaomi TV 55
(76, 1, 'mi55_black.jpg', 1, 11000000, 'MI55-BLK', 13, 1, 1, 39),
(77, 1, 'mi55_silver.jpg', 0, 11200000, 'MI55-SLV', 11, 1, 3, 39),

-- 40 Xiaomi TV 65
(78, 1, 'mi65_black.jpg', 1, 15000000, 'MI65-BLK', 9, 1, 1, 40),
(79, 1, 'mi65_silver.jpg', 0, 15200000, 'MI65-SLV', 7, 1, 3, 40),

-- 41 Panasonic 50
(80, 1, 'pan50_black.jpg', 1, 9500000, 'PAN50-BLK', 10, 1, 1, 41),
(81, 1, 'pan50_gray.jpg', 0, 9700000, 'PAN50-GRY', 8, 1, 3, 41),

-- 42 Panasonic 55
(82, 1, 'pan55_black.jpg', 1, 10500000, 'PAN55-BLK', 9, 1, 1, 42),
(83, 1, 'pan55_gray.jpg', 0, 10700000, 'PAN55-GRY', 7, 1, 3, 42),

-- 43 Hisense 50
(84, 1, 'his50_black.jpg', 1, 8000000, 'HIS50-BLK', 18, 1, 1, 43),
(85, 1, 'his50_gray.jpg', 0, 8200000, 'HIS50-GRY', 15, 1, 3, 43),

-- 44 Hisense 55
(86, 1, 'his55_black.jpg', 1, 9000000, 'HIS55-BLK', 16, 1, 1, 44),
(87, 1, 'his55_gray.jpg', 0, 9200000, 'HIS55-GRY', 13, 1, 3, 44),

-- 45 Sony OLED 65
(88, 1, 'sony_oled65_black.jpg', 1, 40000000, 'SONYOLED65-BLK', 5, 1, 1, 45),
(89, 1, 'sony_oled65_silver.jpg', 0, 40500000, 'SONYOLED65-SLV', 4, 1, 3, 45);



INSERT INTO products (id, product_type, brand, thumbnail, name, description, created)
VALUES (46, 'WATCHES', 'Apple', 'apple_watch_s9.jpg', 'Apple Watch Series 9', 'Premium smartwatch from Apple', NOW()),
       (47, 'WATCHES', 'Apple', 'apple_watch_ultra2.jpg', 'Apple Watch Ultra 2', 'Rugged outdoor smartwatch', NOW()),
       (48, 'WATCHES', 'Samsung', 'galaxy_watch6.jpg', 'Galaxy Watch 6', 'Health tracking smartwatch', NOW()),
       (49, 'WATCHES', 'Samsung', 'galaxy_watch6_classic.jpg', 'Galaxy Watch 6 Classic', 'Classic premium design watch',
        NOW()),
       (50, 'WATCHES', 'Garmin', 'garmin_fenix7.jpg', 'Garmin Fenix 7', 'Outdoor GPS sports watch', NOW()),
       (51, 'WATCHES', 'Garmin', 'garmin_venu2.jpg', 'Garmin Venu 2', 'Fitness smartwatch', NOW()),
       (52, 'WATCHES', 'Huawei', 'huawei_gt4.jpg', 'Huawei Watch GT 4', 'Long battery life watch', NOW()),
       (53, 'WATCHES', 'Huawei', 'huawei_4pro.jpg', 'Huawei Watch 4 Pro', 'Premium health smartwatch', NOW()),
       (54, 'WATCHES', 'Xiaomi', 'mi_watch_s3.jpg', 'Xiaomi Watch S3', 'Affordable smartwatch', NOW()),
       (55, 'WATCHES', 'Amazfit', 'amazfit_gtr4.jpg', 'Amazfit GTR 4', 'Fitness focused watch', NOW()),
       (56, 'WATCHES', 'Amazfit', 'amazfit_trex2.jpg', 'Amazfit T-Rex 2', 'Rugged outdoor watch', NOW()),
       (57, 'WATCHES', 'Fitbit', 'fitbit_sense2.jpg', 'Fitbit Sense 2', 'Health tracking smartwatch', NOW()),
       (58, 'WATCHES', 'Fitbit', 'fitbit_versa4.jpg', 'Fitbit Versa 4', 'Fitness smartwatch', NOW()),
       (59, 'WATCHES', 'Fossil', 'fossil_gen6.jpg', 'Fossil Gen 6', 'Stylish smartwatch', NOW()),
       (60, 'WATCHES', 'Casio', 'casio_smart.jpg', 'Casio Smart Watch', 'Hybrid smart watch', NOW());

INSERT INTO watches (id, model, gender, screen_size, gps, battery_life, weight, material)
VALUES (46, 'S9 Chip', 'Unisex', 1.9, TRUE, 18.0, '38g', 'Aluminum'),
       (47, 'Ultra 2', 'Unisex', 2.0, TRUE, 36.0, '61g', 'Titanium'),
       (48, 'Exynos W930', 'Unisex', 1.5, TRUE, 40.0, '33g', 'Stainless Steel'),
       (49, 'Exynos W930 Pro', 'Unisex', 1.5, TRUE, 40.0, '59g', 'Stainless Steel'),
       (50, 'Garmin Fenix', 'Male', 1.3, TRUE, 144.0, '79g', 'Fiber Polymer'),
       (51, 'Garmin Venu', 'Unisex', 1.2, TRUE, 120.0, '49g', 'Polymer'),
       (52, 'HarmonyOS GT4', 'Unisex', 1.43, TRUE, 336.0, '35g', 'Steel'),
       (53, 'HarmonyOS Pro', 'Unisex', 1.5, TRUE, 480.0, '65g', 'Titanium'),
       (54, 'Xiaomi OS', 'Unisex', 1.43, TRUE, 336.0, '36g', 'Aluminum'),
       (55, 'Amazfit GTR', 'Unisex', 1.45, TRUE, 504.0, '34g', 'Aluminum'),
       (56, 'Amazfit T-Rex', 'Male', 1.39, TRUE, 1000.0, '67g', 'Polymer'),
       (57, 'Fitbit Sense', 'Female', 1.58, TRUE, 144.0, '30g', 'Aluminum'),
       (58, 'Fitbit Versa', 'Unisex', 1.58, TRUE, 144.0, '40g', 'Aluminum'),
       (59, 'Fossil Gen 6', 'Unisex', 1.28, TRUE, 24.0, '48g', 'Steel'),
       (60, 'Casio Hybrid', 'Male', 1.32, TRUE, 720.0, '55g', 'Resin');


INSERT INTO product_variant (id, active, image, is_default, price, sku, stock_quantity, version, color_id, product_id)
VALUES

-- 46 Apple Watch S9
(91, 1, 's9_black.jpg', 1, 9000000, 'AW-S9-BLK', 15, 1, 1, 46),
(92, 1, 's9_silver.jpg', 0, 9200000, 'AW-S9-SLV', 12, 1, 3, 46),

-- 47 Ultra 2
(93, 1, 'ultra2_black.jpg', 1, 20000000, 'AW-U2-BLK', 8, 1, 1, 47),
(94, 1, 'ultra2_orange.jpg', 0, 20500000, 'AW-U2-ORG', 6, 1, 5, 47),

-- 48 Galaxy Watch 6
(95, 1, 'gw6_black.jpg', 1, 7000000, 'GW6-BLK', 20, 1, 1, 48),
(96, 1, 'gw6_silver.jpg', 0, 7200000, 'GW6-SLV', 18, 1, 3, 48),

-- 49 Watch 6 Classic
(97, 1, 'gw6c_black.jpg', 1, 8500000, 'GW6C-BLK', 14, 1, 1, 49),
(98, 1, 'gw6c_silver.jpg', 0, 8700000, 'GW6C-SLV', 12, 1, 3, 49),

-- 50 Garmin Fenix
(99, 1, 'fenix_black.jpg', 1, 15000000, 'FENIX-BLK', 10, 1, 1, 50),
(100, 1, 'fenix_gray.jpg', 0, 15200000, 'FENIX-GRY', 8, 1, 3, 50),

-- 51 Garmin Venu
(101, 1, 'venu_black.jpg', 1, 9000000, 'VENU-BLK', 16, 1, 1, 51),
(102, 1, 'venu_white.jpg', 0, 9200000, 'VENU-WHT', 14, 1, 2, 51),

-- 52 Huawei GT4
(103, 1, 'gt4_black.jpg', 1, 6000000, 'GT4-BLK', 25, 1, 1, 52),
(104, 1, 'gt4_brown.jpg', 0, 6200000, 'GT4-BRN', 20, 1, 3, 52),

-- 53 Huawei 4 Pro
(105, 1, 'h4pro_black.jpg', 1, 12000000, 'H4P-BLK', 12, 1, 1, 53),
(106, 1, 'h4pro_titanium.jpg', 0, 12500000, 'H4P-TIT', 10, 1, 3, 53),

-- 54 Xiaomi Watch S3
(107, 1, 's3_black.jpg', 1, 4000000, 'S3-BLK', 30, 1, 1, 54),
(108, 1, 's3_blue.jpg', 0, 4200000, 'S3-BLU', 25, 1, 4, 54),

-- 55 Amazfit GTR 4
(109, 1, 'gtr4_black.jpg', 1, 5000000, 'GTR4-BLK', 18, 1, 1, 55),
(110, 1, 'gtr4_gray.jpg', 0, 5200000, 'GTR4-GRY', 15, 1, 3, 55),

-- 56 T-Rex 2
(111, 1, 'trex_black.jpg', 1, 7000000, 'TREX2-BLK', 12, 1, 1, 56),
(112, 1, 'trex_green.jpg', 0, 7200000, 'TREX2-GRN', 10, 1, 3, 56),

-- 57 Fitbit Sense
(113, 1, 'sense_black.jpg', 1, 6500000, 'SENSE-BLK', 20, 1, 1, 57),
(114, 1, 'sense_white.jpg', 0, 6700000, 'SENSE-WHT', 18, 1, 2, 57),

-- 58 Versa 4
(115, 1, 'versa_black.jpg', 1, 5500000, 'VERSA4-BLK', 22, 1, 1, 58),
(116, 1, 'versa_pink.jpg', 0, 5700000, 'VERSA4-PNK', 20, 1, 5, 58),

-- 59 Fossil Gen 6
(117, 1, 'gen6_black.jpg', 1, 8000000, 'GEN6-BLK', 14, 1, 1, 59),
(118, 1, 'gen6_silver.jpg', 0, 8200000, 'GEN6-SLV', 12, 1, 3, 59),

-- 60 Casio Hybrid
(119, 1, 'casio_black.jpg', 1, 3000000, 'CASIO-BLK', 30, 1, 1, 60),
(120, 1, 'casio_silver.jpg', 0, 3200000, 'CASIO-SLV', 25, 1, 3, 60);


INSERT INTO users (id, email, image_url, password, status, username)
VALUES (2, 'ttran@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'TTran'),
       (3, 'lpham@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'LPham'),
       (4, 'nnguyen@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'BLOCKED',
        'NNguyen'),
       (5, 'htran@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'HTran'),

       (6, 'kvo@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE', 'KVo'),
       (7, 'bnguyen@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'INACTIVE',
        'BNguyen'),
       (8, 'ctran@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'CTran'),
       (9, 'dle@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE', 'DLe'),
       (10, 'hho@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'BLOCKED', 'HHo'),

       (11, 'tvo@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE', 'TVo'),
       (12, 'kpham@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'KPham'),
       (13, 'ltran@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'INACTIVE',
        'LTran'),
       (14, 'mnguyen@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'MNguyen'),
       (15, 'nvo@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE', 'NVo'),

       (16, 'ptran@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'PTran'),
       (17, 'qle@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'BLOCKED', 'QLe'),
       (18, 'rpham@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'RPham'),
       (19, 'snguyen@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'ACTIVE',
        'SNguyen'),
       (20, 'tngo@example.com', NULL, '$2a$10$2UVf/Gb4PEEeblzfePGrK.kSS3WLyFea1mHM/eFX1Gv9HgFzQcSvm', 'INACTIVE',
        'TNgo');


INSERT INTO addresses (id, address, created_at, is_default, phone, recipient_name, user_id)
VALUES
-- USER 1
(1, '12 Nguyen Trai, District 1, HCM', NOW(), 1, '0901000001', 'DNguyen', 1),
(2, '45 Le Loi, District 3, HCM', NOW(), 0, '0901000002', 'DNguyen Home', 1),

-- USER 2
(3, '88 Tran Hung Dao, District 5, HCM', NOW(), 1, '0901000003', 'TTran', 2),
(4, '12 Vo Van Tan, District 3, HCM', NOW(), 0, '0901000004', 'TTran Office', 2),

-- USER 3
(5, '101 Nguyen Hue, District 1, HCM', NOW(), 1, '0901000005', 'LPham', 3),
(6, '55 Cach Mang Thang 8, District 10', NOW(), 0, '0901000006', 'LPham Mom', 3),

-- USER 4
(7, '23 Le Van Sy, Phu Nhuan', NOW(), 1, '0901000007', 'NNguyen', 4),
(8, '90 Hoang Van Thu, Tan Binh', NOW(), 0, '0901000008', 'NNguyen Work', 4),

-- USER 5
(9, '11 Dien Bien Phu, Binh Thanh', NOW(), 1, '0901000009', 'HTran', 5),
(10, '77 Nguyen Thi Minh Khai', NOW(), 0, '0901000010', 'HTran Office', 5),

-- USER 6
(11, '33 Pham Van Dong, Thu Duc', NOW(), 1, '0901000011', 'KVo', 6),
(12, '12 Vo Chi Cong, Thu Duc', NOW(), 0, '0901000012', 'KVo Home', 6),

-- USER 7
(13, '88 Nguyen Oanh, Go Vap', NOW(), 1, '0901000013', 'BNguyen', 7),
(14, '44 Quang Trung, Go Vap', NOW(), 0, '0901000014', 'BNguyen Office', 7),

-- USER 8
(15, '99 Le Duc Tho, Go Vap', NOW(), 1, '0901000015', 'CTran', 8),
(16, '21 Phan Van Tri, Go Vap', NOW(), 0, '0901000016', 'CTran House', 8),

-- USER 9
(17, '10 Pasteur, District 1', NOW(), 1, '0901000017', 'DLe', 9),
(18, '99 Nguyen Van Cu, District 5', NOW(), 0, '0901000018', 'DLe Work', 9),

-- USER 10
(19, '33 Ly Thuong Kiet, District 10', NOW(), 1, '0901000019', 'HHo', 10),
(20, '88 Ba Thang Hai, District 10', NOW(), 0, '0901000020', 'HHo Home', 10),

-- USER 11
(21, '12 Nguyen Van Linh, District 7', NOW(), 1, '0901000021', 'TVo', 11),
(22, '45 Huynh Tan Phat, District 7', NOW(), 0, '0901000022', 'TVo Office', 11),

-- USER 12
(23, '66 Nguyen Dinh Chieu, District 3', NOW(), 1, '0901000023', 'KPham', 12),
(24, '88 Pasteur, District 3', NOW(), 0, '0901000024', 'KPham House', 12),

-- USER 13
(25, '11 Le Van Sy, Phu Nhuan', NOW(), 1, '0901000025', 'LTran', 13),
(26, '55 Nguyen Trong Tuyen', NOW(), 0, '0901000026', 'LTran Work', 13),

-- USER 14
(27, '22 Cong Hoa, Tan Binh', NOW(), 1, '0901000027', 'MNguyen', 14),
(28, '88 Truong Chinh, Tan Binh', NOW(), 0, '0901000028', 'MNguyen Office', 14),

-- USER 15
(29, '10 Phan Xich Long, Phu Nhuan', NOW(), 1, '0901000029', 'NVo', 15),
(30, '99 Hoang Hoa Tham, Phu Nhuan', NOW(), 0, '0901000030', 'NVo House', 15),

-- USER 16
(31, '45 Vo Van Tan, District 3', NOW(), 1, '0901000031', 'PTran', 16),
(32, '12 Dien Bien Phu, District 3', NOW(), 0, '0901000032', 'PTran Work', 16),

-- USER 17
(33, '77 Nguyen Hue, District 1', NOW(), 1, '0901000033', 'QLe', 17),
(34, '88 Le Loi, District 1', NOW(), 0, '0901000034', 'QLe Office', 17),

-- USER 18
(35, '12 Cach Mang Thang 8', NOW(), 1, '0901000035', 'RPham', 18),
(36, '99 Nguyen Thi Minh Khai', NOW(), 0, '0901000036', 'RPham Home', 18),

-- USER 19
(37, '88 Tran Phu, District 5', NOW(), 1, '0901000037', 'SNguyen', 19),
(38, '21 An Duong Vuong', NOW(), 0, '0901000038', 'SNguyen Work', 19),

-- USER 20
(39, '55 Le Hong Phong, District 10', NOW(), 1, '0901000039', 'TNgo', 20),
(40, '88 Su Van Hanh, District 10', NOW(), 0, '0901000040', 'TNgo Office', 20),

-- EXTRA (to reach 45 records)
(41, '12 Hai Ba Trung, District 1', NOW(), 1, '0901000041', 'Extra User 1', 1),
(42, '34 Nguyen Trai, District 5', NOW(), 0, '0901000042', 'Extra User 2', 2),
(43, '56 Le Lai, District 1', NOW(), 1, '0901000043', 'Extra User 3', 3),
(44, '78 Dien Bien Phu, Binh Thanh', NOW(), 0, '0901000044', 'Extra User 4', 4),
(45, '99 Vo Thi Sau, District 3', NOW(), 1, '0901000045', 'Extra User 5', 5);


INSERT INTO carts (id, cart_key, created_at, status, updated_at, user_id)
VALUES (1, 'CART-001', NOW(), 'ACTIVE', NOW(), 1),
       (2, 'CART-002', NOW(), 'ORDERED', NOW(), 2),
       (3, 'CART-003', NOW(), 'ABANDONED', NOW(), 3),
       (4, 'CART-004', NOW(), 'ACTIVE', NOW(), 4),
       (5, 'CART-005', NOW(), 'ACTIVE', NOW(), 5),
       (6, 'CART-006', NOW(), 'ABANDONED', NOW(), 6),
       (7, 'CART-007', NOW(), 'ACTIVE', NOW(), 7),
       (8, 'CART-008', NOW(), 'ORDERED', NOW(), 8),
       (9, 'CART-009', NOW(), 'ACTIVE', NOW(), 9),
       (10, 'CART-010', NOW(), 'ACTIVE', NOW(), 10),
       (11, 'CART-011', NOW(), 'ABANDONED', NOW(), 11),
       (12, 'CART-012', NOW(), 'ACTIVE', NOW(), 12),
       (13, 'CART-013', NOW(), 'ACTIVE', NOW(), 13),
       (14, 'CART-014', NOW(), 'ORDERED', NOW(), 14),
       (15, 'CART-015', NOW(), 'ACTIVE', NOW(), 15),
       (16, 'CART-016', NOW(), 'ACTIVE', NOW(), 16),
       (17, 'CART-017', NOW(), 'ABANDONED', NOW(), 17),
       (18, 'CART-018', NOW(), 'ACTIVE', NOW(), 18),
       (19, 'CART-019', NOW(), 'ORDERED', NOW(), 19),
       (20, 'CART-020', NOW(), 'ACTIVE', NOW(), 20);

INSERT INTO cart_items (id, created_at, price, quantity, cart_id, product_variant_id)
VALUES
    (1, '2026-05-01 10:15:00', 18000000/25000, 1, 1, 30),
    (2, '2026-05-02 11:20:00', 9200000/25000, 2, 1, 91),

    (3, '2026-05-03 09:05:00', 20000000/25000, 1, 2, 32),
    (4, '2026-05-03 09:10:00', 7000000/25000, 1, 2, 95),

    (5, '2026-05-04 14:30:00', 11000000/25000, 1, 3, 38),
    (6, '2026-05-04 14:45:00', 6500000/25000, 2, 3, 113),

    (7, '2026-05-05 08:00:00', 15000000/25000, 1, 4, 50),
    (8, '2026-05-05 08:20:00', 22000000/25000, 1, 4, 64),

    (9, '2026-05-06 12:10:00', 9000000/25000, 2, 5, 72),
    (10, '2026-05-06 12:40:00', 12000000/25000, 1, 5, 46),

    (11, '2026-05-07 15:00:00', 18000000/25000, 1, 6, 74),
    (12, '2026-05-07 15:25:00', 9500000/25000, 1, 6, 42),

    (13, '2026-05-08 10:00:00', 26000000/25000, 1, 7, 70),
    (14, '2026-05-08 10:30:00', 4000000/25000, 3, 7, 107),

    (15, '2026-05-09 09:15:00', 24000000/25000, 1, 8, 62),
    (16, '2026-05-09 09:45:00', 8000000/25000, 1, 8, 99),

    (17, '2026-05-10 13:20:00', 30000000/25000, 1, 9, 67),
    (18, '2026-05-10 13:50:00', 11000000/25000, 2, 9, 76),

    (19, '2026-05-11 16:10:00', 15000000/25000, 1, 10, 78),
    (20, '2026-05-11 16:40:00', 9000000/25000, 1, 10, 102),

    (21, '2026-05-12 11:00:00', 12000000/25000, 2, 11, 46),
    (22, '2026-05-12 11:30:00', 7000000/25000, 1, 11, 48),

    (23, '2026-05-13 14:15:00', 20000000/25000, 1, 12, 93),
    (24, '2026-05-13 14:45:00', 8500000/25000, 1, 12, 97),

    (25, '2026-05-14 09:10:00', 18000000/25000, 1, 13, 60),
    (26, '2026-05-14 09:40:00', 9000000/25000, 2, 13, 113),

    (27, '2026-05-15 18:00:00', 26000000/25000, 1, 14, 70),
    (28, '2026-05-15 18:25:00', 15000000/25000, 1, 14, 50),

    (29, '2026-05-16 12:30:00', 40000000/25000, 1, 15, 88),
    (30, '2026-05-16 12:55:00', 22000000/25000, 1, 15, 64),

    (31, '2026-05-17 08:10:00', 18000000/25000, 1, 16, 30),
    (32, '2026-05-17 08:40:00', 11000000/25000, 1, 16, 36),

    (33, '2026-05-18 10:20:00', 20000000/25000, 1, 17, 32),
    (34, '2026-05-18 10:50:00', 7000000/25000, 2, 17, 95),

    (35, '2026-05-19 15:00:00', 9000000/25000, 1, 18, 34),
    (36, '2026-05-19 15:30:00', 6500000/25000, 2, 18, 113),

    (37, '2026-05-20 09:45:00', 15000000/25000, 1, 19, 26),
    (38, '2026-05-20 10:10:00', 11000000/25000, 1, 19, 20),

    (39, '2026-05-21 13:15:00', 9000000/25000, 1, 20, 58),
    (40, '2026-05-21 13:40:00', 5500000/25000, 2, 20, 115),

    (41, '2026-05-22 11:00:00', 18000000/25000, 1, 1, 33),
    (42, '2026-05-22 11:30:00', 12000000/25000, 1, 2, 47),

    (43, '2026-05-23 14:00:00', 22000000/25000, 1, 3, 65),
    (44, '2026-05-23 14:30:00', 9000000/25000, 2, 4, 72),

    (45, '2026-05-24 09:00:00', 26000000/25000, 1, 5, 70),
    (46, '2026-05-24 09:30:00', 15000000/25000, 1, 6, 50),

    (47, '2026-05-25 10:15:00', 7000000/25000, 1, 7, 95),
    (48, '2026-05-25 10:45:00', 11000000/25000, 1, 8, 76),

    (49, '2026-05-26 08:30:00', 30000000/25000, 1, 9, 66),
    (50, '2026-05-26 09:00:00', 9000000/25000, 1, 10, 101);



INSERT INTO orders (id, created_at, discount_amount, note, order_code, payment_method, payment_transaction_id, address,
                    phone, recipient_name, shipping_fee, status, total_price, version, user_id)
VALUES (1, '2026-05-01 10:00:00', 5.00, NULL, 'ORD-0001', 'COD', NULL, '12 Nguyen Trai HCM', '0901000001', 'DNguyen',
        2.00, 'COMPLETED', 520.00, 1, 1),
       (2, '2026-05-02 11:10:00', 0.00, NULL, 'ORD-0002', 'CARD', 'TXN-1002', '45 Le Loi HCM', '0901000002', 'TTran',
        3.00, 'SHIPPED', 680.00, 1, 2),
       (3, '2026-05-03 09:20:00', 10.00, NULL, 'ORD-0003', 'COD', NULL, '88 Tran Hung Dao', '0901000003', 'LPham', 2.50,
        'PROCESSING', 740.00, 1, 3),
       (4, '2026-05-04 14:00:00', 0.00, NULL, 'ORD-0004', 'CARD', 'TXN-1004', '23 Le Van Sy', '0901000007', 'NNguyen',
        3.00, 'COMPLETED', 880.00, 1, 4),
       (5, '2026-05-05 08:15:00', 15.00, NULL, 'ORD-0005', 'COD', NULL, '11 Dien Bien Phu', '0901000009', 'HTran', 2.00,
        'PENDING', 420.00, 1, 5),

       (6, '2026-05-06 12:30:00', 0.00, NULL, 'ORD-0006', 'CARD', 'TXN-1006', '33 Pham Van Dong', '0901000011', 'KVo',
        3.00, 'SHIPPED', 990.00, 1, 6),
       (7, '2026-05-07 15:45:00', 8.00, NULL, 'ORD-0007', 'COD', NULL, '88 Nguyen Oanh', '0901000013', 'BNguyen', 2.50,
        'PROCESSING', 610.00, 1, 7),
       (8, '2026-05-08 10:10:00', 0.00, NULL, 'ORD-0008', 'CARD', 'TXN-1008', '99 Le Duc Tho', '0901000015', 'CTran',
        3.00, 'COMPLETED', 720.00, 1, 8),
       (9, '2026-05-09 09:00:00', 20.00, NULL, 'ORD-0009', 'COD', NULL, '10 Pasteur', '0901000017', 'DLe', 2.00,
        'CANCELLED', 300.00, 1, 9),
       (10, '2026-05-10 13:40:00', 0.00, NULL, 'ORD-0010', 'CARD', 'TXN-1010', '33 Ly Thuong Kiet', '0901000019', 'HHo',
        3.00, 'COMPLETED', 860.00, 1, 10),

       (11, '2026-05-11 16:20:00', 5.00, NULL, 'ORD-0011', 'COD', NULL, '12 Nguyen Van Linh', '0901000021', 'TVo', 2.00,
        'PROCESSING', 540.00, 1, 11),
       (12, '2026-05-12 11:30:00', 0.00, NULL, 'ORD-0012', 'CARD', 'TXN-1012', '66 Nguyen Dinh Chieu', '0901000023',
        'KPham', 3.00, 'SHIPPED', 920.00, 1, 12),
       (13, '2026-05-13 14:10:00', 12.00, NULL, 'ORD-0013', 'COD', NULL, '11 Le Van Sy', '0901000025', 'LTran', 2.50,
        'COMPLETED', 780.00, 1, 13),
       (14, '2026-05-14 09:50:00', 0.00, NULL, 'ORD-0014', 'CARD', 'TXN-1014', '22 Cong Hoa', '0901000027', 'MNguyen',
        3.00, 'SHIPPED', 1100.00, 1, 14),
       (15, '2026-05-15 18:10:00', 25.00, NULL, 'ORD-0015', 'COD', NULL, '10 Phan Xich Long', '0901000029', 'NVo', 2.00,
        'PENDING', 650.00, 1, 15),

       (16, '2026-05-16 12:45:00', 0.00, NULL, 'ORD-0016', 'CARD', 'TXN-1016', '45 Vo Van Tan', '0901000031', 'PTran',
        3.00, 'PROCESSING', 990.00, 1, 16),
       (17, '2026-05-17 08:20:00', 0.00, NULL, 'ORD-0017', 'COD', NULL, '77 Nguyen Hue', '0901000033', 'QLe', 2.50,
        'COMPLETED', 870.00, 1, 17),
       (18, '2026-05-18 10:35:00', 10.00, NULL, 'ORD-0018', 'CARD', 'TXN-1018', '12 Cach Mang Thang 8', '0901000035',
        'RPham', 3.00, 'SHIPPED', 940.00, 1, 18),
       (19, '2026-05-19 15:25:00', 0.00, NULL, 'ORD-0019', 'COD', NULL, '88 Tran Phu', '0901000037', 'SNguyen', 2.00,
        'PROCESSING', 760.00, 1, 19),
       (20, '2026-05-20 09:10:00', 18.00, NULL, 'ORD-0020', 'CARD', 'TXN-1020', '55 Le Hong Phong', '0901000039',
        'TNgo', 3.00, 'COMPLETED', 820.00, 1, 20);

INSERT INTO order_item (id, original_price, price, product_discount_amount, product_discount_type, quantity, order_id,
                        product_variant_id)
VALUES

-- ORDER 1
(1, 500.00, 450.00, 50.00, 'FIXED_AMOUNT', 1, 1, 30),
(2, 200.00, 180.00, 10.00, 'PERCENTAGE', 2, 1, 91),
(3, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 1, 95),

-- ORDER 2
(4, 600.00, 550.00, 50.00, 'FIXED_AMOUNT', 1, 2, 32),
(5, 400.00, 360.00, 10.00, 'PERCENTAGE', 1, 2, 64),
(6, 250.00, 220.00, 30.00, 'FIXED_AMOUNT', 2, 2, 113),

-- ORDER 3
(7, 700.00, 650.00, 50.00, 'FIXED_AMOUNT', 1, 3, 38),
(8, 300.00, 270.00, 10.00, 'PERCENTAGE', 1, 3, 42),
(9, 200.00, 190.00, 10.00, 'FIXED_AMOUNT', 1, 3, 50),

-- ORDER 4
(10, 800.00, 750.00, 50.00, 'FIXED_AMOUNT', 1, 4, 70),
(11, 500.00, 450.00, 10.00, 'PERCENTAGE', 2, 4, 107),
(12, 600.00, 580.00, 20.00, 'FIXED_AMOUNT', 1, 4, 99),

-- ORDER 5
(13, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 5, 72),
(14, 250.00, 230.00, 10.00, 'PERCENTAGE', 1, 5, 46),
(15, 200.00, 180.00, 20.00, 'FIXED_AMOUNT', 1, 5, 91),

-- ORDER 6
(16, 900.00, 850.00, 50.00, 'FIXED_AMOUNT', 1, 6, 74),
(17, 400.00, 370.00, 10.00, 'PERCENTAGE', 1, 6, 42),
(18, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 6, 93),

-- ORDER 7
(19, 600.00, 550.00, 50.00, 'FIXED_AMOUNT', 1, 7, 70),
(20, 300.00, 270.00, 10.00, 'PERCENTAGE', 2, 7, 107),
(21, 200.00, 180.00, 20.00, 'FIXED_AMOUNT', 1, 7, 115),

-- ORDER 8
(22, 700.00, 650.00, 50.00, 'FIXED_AMOUNT', 1, 8, 62),
(23, 400.00, 360.00, 10.00, 'PERCENTAGE', 1, 8, 76),
(24, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 8, 101),

-- ORDER 9
(25, 500.00, 450.00, 50.00, 'FIXED_AMOUNT', 1, 9, 66),
(26, 300.00, 270.00, 10.00, 'PERCENTAGE', 1, 9, 34),
(27, 200.00, 180.00, 20.00, 'FIXED_AMOUNT', 1, 9, 95),

-- ORDER 10
(28, 800.00, 750.00, 50.00, 'FIXED_AMOUNT', 1, 10, 78),
(29, 400.00, 360.00, 10.00, 'PERCENTAGE', 2, 10, 99),
(30, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 10, 113),

-- ORDER 11 → 20 (rút gọn pattern tương tự)
(31, 500.00, 460.00, 40.00, 'FIXED_AMOUNT', 1, 11, 46),
(32, 300.00, 270.00, 10.00, 'PERCENTAGE', 1, 11, 48),
(33, 200.00, 180.00, 20.00, 'FIXED_AMOUNT', 1, 11, 30),

(34, 600.00, 550.00, 50.00, 'FIXED_AMOUNT', 1, 12, 93),
(35, 400.00, 360.00, 10.00, 'PERCENTAGE', 1, 12, 97),
(36, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 12, 64),

(37, 700.00, 650.00, 50.00, 'FIXED_AMOUNT', 1, 13, 60),
(38, 300.00, 270.00, 10.00, 'PERCENTAGE', 2, 13, 113),
(39, 200.00, 180.00, 20.00, 'FIXED_AMOUNT', 1, 13, 91),

(40, 800.00, 750.00, 50.00, 'FIXED_AMOUNT', 1, 14, 70),
(41, 500.00, 450.00, 10.00, 'PERCENTAGE', 1, 14, 50),
(42, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 14, 107),

(43, 900.00, 850.00, 50.00, 'FIXED_AMOUNT', 1, 15, 88),
(44, 400.00, 370.00, 10.00, 'PERCENTAGE', 1, 15, 64),
(45, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 15, 42),

(46, 500.00, 460.00, 40.00, 'FIXED_AMOUNT', 1, 16, 30),
(47, 300.00, 270.00, 10.00, 'PERCENTAGE', 1, 16, 36),
(48, 200.00, 180.00, 20.00, 'FIXED_AMOUNT', 1, 16, 99),

(49, 600.00, 550.00, 50.00, 'FIXED_AMOUNT', 1, 17, 32),
(50, 400.00, 360.00, 10.00, 'PERCENTAGE', 1, 17, 95),
(51, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 17, 113),

(52, 700.00, 650.00, 50.00, 'FIXED_AMOUNT', 1, 18, 34),
(53, 300.00, 270.00, 10.00, 'PERCENTAGE', 2, 18, 76),
(54, 200.00, 180.00, 20.00, 'FIXED_AMOUNT', 1, 18, 101),

(55, 800.00, 750.00, 50.00, 'FIXED_AMOUNT', 1, 19, 26),
(56, 400.00, 360.00, 10.00, 'PERCENTAGE', 1, 19, 20),
(57, 300.00, 280.00, 20.00, 'FIXED_AMOUNT', 1, 19, 58),

(58, 500.00, 460.00, 40.00, 'FIXED_AMOUNT', 1, 20, 115),
(59, 300.00, 270.00, 10.00, 'PERCENTAGE', 1, 20, 101),
(60, 200.00, 180.00, 20.00, 'FIXED_AMOUNT', 1, 20, 113);

INSERT INTO discounts (id, active, end_date, name, start_date, type, value)
VALUES
    (1, b'1', '2026-06-30 23:59:59.000000', 'Summer Sale 10%', '2026-05-01 00:00:00.000000', 'PERCENTAGE', 10),

    (2, b'1', '2026-06-15 23:59:59.000000', 'Flash Discount 5$', '2026-05-10 00:00:00.000000', 'FIXED_AMOUNT', 5),

    (3, b'1', '2026-07-01 23:59:59.000000', 'Mega Sale 20%', '2026-05-20 00:00:00.000000', 'PERCENTAGE', 20),

    (4, b'0', '2026-05-30 23:59:59.000000', 'Expired Promo', '2026-04-01 00:00:00.000000', 'FIXED_AMOUNT', 3),

    (5, b'1', '2026-08-31 23:59:59.000000', 'VIP Customer 15%', '2026-05-01 00:00:00.000000', 'PERCENTAGE', 15);

INSERT INTO discount_products (discount_id, product_variant_id)
VALUES
    (1, 30),
    (1, 32),
    (1, 50),

    (2, 91),
    (2, 95),
    (2, 113),

    (3, 70),
    (3, 64),
    (3, 66),

    (4, 36),
    (4, 42),

    (5, 88),
    (5, 76),
    (5, 101);



INSERT INTO reviews (
    id, avatar, comment, created_at, helpful_count,
    rating, updated_at, username, order_id,
    product_variant_id, user_id
)
VALUES
    (1,  'images/user/user-01.png', 'Sản phẩm rất tốt', '2026-05-01 10:00:00', 5, 5, '2026-05-01 10:05:00', 'user1', 1, 30, 1),
    (2,  'images/user/user-02.png', 'Giao hàng nhanh', '2026-05-01 11:00:00', 3, 4, '2026-05-01 11:10:00', 'user2', 2, 91, 2),
    (3,  'images/user/user-03.png', 'Chất lượng ổn', '2026-05-02 09:15:00', 2, 4, '2026-05-02 09:20:00', 'user3', 3, 32, 3),
    (4,  'images/user/user-04.png', 'Không như mong đợi', '2026-05-02 10:30:00', 1, 2, '2026-05-02 10:35:00', 'user4', 4, 95, 4),
    (5,  'images/user/user-05.png', 'Rất đáng tiền', '2026-05-03 08:00:00', 6, 5, '2026-05-03 08:05:00', 'user5', 5, 38, 5),
    (6,  'images/user/user-06.png', 'OK trong tầm giá', '2026-05-03 09:20:00', 2, 4, '2026-05-03 09:25:00', 'user6', 6, 113, 6),
    (7,  'images/user/user-07.png', 'Rất hài lòng', '2026-05-04 12:00:00', 4, 5, '2026-05-04 12:10:00', 'user7', 7, 50, 7),
    (8,  'images/user/user-08.png', 'Tạm ổn', '2026-05-04 13:00:00', 1, 3, '2026-05-04 13:05:00', 'user8', 8, 64, 8),
    (9,  'images/user/user-09.png', 'Đẹp, chất lượng tốt', '2026-05-05 08:30:00', 5, 5, '2026-05-05 08:35:00', 'user9', 9, 72, 9),
    (10, 'images/user/user-10.png', 'Hơi chậm giao hàng', '2026-05-05 09:10:00', 2, 3, '2026-05-05 09:15:00', 'user10', 10, 46, 10),

    (11, 'images/user/user-11.png', 'Ổn áp', '2026-05-06 10:00:00', 3, 4, '2026-05-06 10:05:00', 'user11', 11, 74, 11),
    (12, 'images/user/user-12.png', 'Không tốt lắm', '2026-05-06 11:00:00', 1, 2, '2026-05-06 11:05:00', 'user12', 12, 42, 12),
    (13, 'images/user/user-13.png', 'Rất đẹp', '2026-05-07 09:00:00', 6, 5, '2026-05-07 09:10:00', 'user13', 13, 70, 13),
    (14, 'images/user/user-14.png', 'Bình thường', '2026-05-07 10:00:00', 2, 3, '2026-05-07 10:05:00', 'user14', 14, 107, 14),
    (15, 'images/user/user-15.png', 'Quá tốt', '2026-05-08 08:00:00', 7, 5, '2026-05-08 08:10:00', 'user15', 15, 62, 15),
    (16, 'images/user/user-16.png', 'Ổn', '2026-05-08 09:00:00', 2, 4, '2026-05-08 09:05:00', 'user16', 16, 99, 16),
    (17, 'images/user/user-17.png', 'Hài lòng', '2026-05-09 10:00:00', 5, 5, '2026-05-09 10:10:00', 'user17', 17, 67, 17),
    (18, 'images/user/user-18.png', 'Không tệ', '2026-05-09 11:00:00', 2, 3, '2026-05-09 11:05:00', 'user18', 18, 76, 18),
    (19, 'images/user/user-19.png', 'Tốt', '2026-05-10 08:00:00', 3, 4, '2026-05-10 08:05:00', 'user19', 19, 78, 19),
    (20, 'images/user/user-20.png', 'Rất tốt', '2026-05-10 09:00:00', 4, 5, '2026-05-10 09:05:00', 'user1', 20, 102, 1),

    (21, 'images/user/user-21.png', 'OK', '2026-05-11 10:00:00', 2, 4, '2026-05-11 10:05:00', 'user2', 1, 46, 2),
    (22, 'images/user/user-22.png', 'Không thích lắm', '2026-05-11 11:00:00', 1, 2, '2026-05-11 11:05:00', 'user3', 2, 48, 3),
    (23, 'images/user/user-23.png', 'Rất đẹp', '2026-05-12 09:00:00', 6, 5, '2026-05-12 09:10:00', 'user4', 3, 93, 4),
    (24, 'images/user/user-24.png', 'Ổn', '2026-05-12 10:00:00', 2, 4, '2026-05-12 10:05:00', 'user5', 4, 97, 5),
    (25, 'images/user/user-25.png', 'Chất lượng tốt', '2026-05-13 08:00:00', 5, 5, '2026-05-13 08:10:00', 'user6', 5, 60, 6),
    (26, 'images/user/user-26.png', 'Tạm ổn', '2026-05-13 09:00:00', 2, 3, '2026-05-13 09:05:00', 'user7', 6, 113, 7),
    (27, 'images/user/user-27.png', 'Rất thích', '2026-05-14 10:00:00', 7, 5, '2026-05-14 10:10:00', 'user8', 7, 70, 8),
    (28, 'images/user/user-28.png', 'OK', '2026-05-14 11:00:00', 3, 4, '2026-05-14 11:05:00', 'user9', 8, 50, 9),
    (29, 'images/user/user-29.png', 'Quá đẹp', '2026-05-15 08:00:00', 8, 5, '2026-05-15 08:10:00', 'user10', 9, 88, 10),
    (30, 'images/user/user-30.png', 'Hơi tệ', '2026-05-15 09:00:00', 1, 2, '2026-05-15 09:05:00', 'user11', 10, 64, 11),

    (31, 'images/user/user-01.png', 'Ổn', '2026-05-16 10:00:00', 3, 4, '2026-05-16 10:05:00', 'user12', 11, 30, 12),
    (32, 'images/user/user-02.png', 'Tốt', '2026-05-16 11:00:00', 4, 5, '2026-05-16 11:05:00', 'user13', 12, 36, 13),
    (33, 'images/user/user-03.png', 'Không ổn', '2026-05-17 08:00:00', 1, 2, '2026-05-17 08:05:00', 'user14', 13, 32, 14),
    (34, 'images/user/user-04.png', 'Khá tốt', '2026-05-17 09:00:00', 2, 4, '2026-05-17 09:05:00', 'user15', 14, 95, 15),
    (35, 'images/user/user-05.png', 'Rất hài lòng', '2026-05-18 10:00:00', 6, 5, '2026-05-18 10:10:00', 'user16', 15, 34, 16),
    (36, 'images/user/user-06.png', 'Tạm được', '2026-05-18 11:00:00', 2, 3, '2026-05-18 11:05:00', 'user17', 16, 113, 17),
    (37, 'images/user/user-07.png', 'Đẹp', '2026-05-19 08:00:00', 5, 5, '2026-05-19 08:10:00', 'user18', 17, 26, 18),
    (38, 'images/user/user-08.png', 'Ổn', '2026-05-19 09:00:00', 3, 4, '2026-05-19 09:05:00', 'user19', 18, 20, 19),
    (39, 'images/user/user-09.png', 'Không tệ', '2026-05-20 10:00:00', 2, 3, '2026-05-20 10:05:00', 'user1', 19, 58, 1),
    (40, 'images/user/user-10.png', 'Rất tốt', '2026-05-20 11:00:00', 5, 5, '2026-05-20 11:05:00', 'user2', 20, 115, 2);


INSERT INTO review_helpful (user_id, review_id)
VALUES
    (1, 1), (2, 1), (3, 1), (4, 1), (5, 1),
    (6, 2), (7, 2), (8, 2), (9, 2), (10, 2),

    (11, 3), (12, 3), (13, 3), (14, 3), (15, 3),
    (16, 4), (17, 4), (18, 4), (19, 4), (1, 4),

    (2, 5), (3, 5), (4, 5), (5, 5), (6, 5),
    (7, 6), (8, 6), (9, 6), (10, 6), (11, 6),

    (12, 7), (13, 7), (14, 7), (15, 7), (16, 7),
    (17, 8), (18, 8), (19, 8), (1, 8), (2, 8),

    (3, 9), (4, 9), (5, 9), (6, 9), (7, 9),
    (8, 10), (9, 10), (10, 10), (11, 10), (12, 10),

    (13, 11), (14, 11), (15, 11), (16, 11), (17, 11),
    (18, 12), (19, 12), (1, 12), (2, 12), (3, 12),

    (4, 13), (5, 13), (6, 13), (7, 13), (8, 13),
    (9, 14), (10, 14), (11, 14), (12, 14), (13, 14),

    (14, 15), (15, 15), (16, 15), (17, 15), (18, 15),
    (19, 16), (1, 16), (2, 16), (3, 16), (4, 16),

    (5, 17), (6, 17), (7, 17), (8, 17), (9, 17),
    (10, 18), (11, 18), (12, 18), (13, 18), (14, 18),

    (15, 19), (16, 19), (17, 19), (18, 19), (19, 19),
    (1, 20), (2, 20), (3, 20), (4, 20), (5, 20);

INSERT INTO vouchers (
    id, active, code, discount_type, end_date,
    max_discount, min_order_value, quantity,
    start_date, type, used_count, value
)
VALUES
    (1,  b'1', 'WELCOME10', 'PERCENTAGE', '2026-06-30 23:59:59.000000',
     50.00, 0.00, 1000, '2026-05-01 00:00:00.000000',
     'ORDER_DISCOUNT', 120, 10.00),

    (2,  b'1', 'FREESHIP50K', 'FIXED_AMOUNT', '2026-07-15 23:59:59.000000',
     50.00, 200.00, 500, '2026-05-01 00:00:00.000000',
     'FREE_SHIP', 80, 50000.00),

    (3,  b'1', 'SUMMER15', 'PERCENTAGE', '2026-06-20 23:59:59.000000',
     100.00, 300.00, 800, '2026-05-10 00:00:00.000000',
     'ORDER_DISCOUNT', 200, 15.00),

    (4,  b'1', 'FLASH20K', 'FIXED_AMOUNT', '2026-06-10 23:59:59.000000',
     20.00, 100.00, 300, '2026-05-05 00:00:00.000000',
     'ORDER_DISCOUNT', 150, 20000.00),

    (5,  b'1', 'VIP20', 'PERCENTAGE', '2026-08-31 23:59:59.000000',
     200.00, 500.00, 2000, '2026-05-01 00:00:00.000000',
     'ORDER_DISCOUNT', 450, 20.00),

    (6,  b'0', 'EXPIRED10', 'PERCENTAGE', '2026-05-10 23:59:59.000000',
     30.00, 0.00, 100, '2026-04-01 00:00:00.000000',
     'ORDER_DISCOUNT', 100, 10.00),

    (7,  b'1', 'PRODUCT30', 'PERCENTAGE', '2026-07-01 23:59:59.000000',
     150.00, 0.00, 600, '2026-05-12 00:00:00.000000',
     'PRODUCT_DISCOUNT', 90, 30.00),

    (8,  b'1', 'FREESHIP10K', 'FIXED_AMOUNT', '2026-06-25 23:59:59.000000',
     10.00, 150.00, 1000, '2026-05-03 00:00:00.000000',
     'FREE_SHIP', 300, 10000.00),

    (9,  b'1', 'MEGA25', 'PERCENTAGE', '2026-07-30 23:59:59.000000',
     250.00, 1000.00, 1500, '2026-05-15 00:00:00.000000',
     'ORDER_DISCOUNT', 500, 25.00),

    (10, b'1', 'NEWUSER5K', 'FIXED_AMOUNT', '2026-06-01 23:59:59.000000',
     5.00, 0.00, 5000, '2026-05-01 00:00:00.000000',
     'ORDER_DISCOUNT', 1200, 5000.00),

    (11, b'1', 'TECH10', 'PERCENTAGE', '2026-08-01 23:59:59.000000',
     100.00, 500.00, 700, '2026-05-20 00:00:00.000000',
     'PRODUCT_DISCOUNT', 60, 10.00),

    (12, b'1', 'BIGSALE50', 'PERCENTAGE', '2026-09-30 23:59:59.000000',
     500.00, 2000.00, 1000, '2026-05-01 00:00:00.000000',
     'ORDER_DISCOUNT', 220, 50.00);


INSERT INTO user_vouchers (
    id, used, used_at, user_id, voucher_id
)
VALUES
    (1,  b'1', '2026-05-02 10:00:00.000000', 1, 1),
    (2,  b'0', NULL, 2, 2),
    (3,  b'1', '2026-05-03 11:00:00.000000', 3, 3),
    (4,  b'0', NULL, 4, 4),
    (5,  b'1', '2026-05-04 09:30:00.000000', 5, 5),

    (6,  b'0', NULL, 6, 6),
    (7,  b'1', '2026-05-05 14:10:00.000000', 7, 7),
    (8,  b'0', NULL, 8, 8),
    (9,  b'1', '2026-05-06 16:20:00.000000', 9, 9),
    (10, b'0', NULL, 10, 10),

    (11, b'1', '2026-05-07 10:15:00.000000', 11, 11),
    (12, b'0', NULL, 12, 12),
    (13, b'1', '2026-05-08 12:40:00.000000', 13, 1),
    (14, b'0', NULL, 14, 2),
    (15, b'1', '2026-05-09 09:00:00.000000', 15, 3),

    (16, b'0', NULL, 16, 4),
    (17, b'1', '2026-05-10 11:25:00.000000', 17, 5),
    (18, b'0', NULL, 18, 6),
    (19, b'1', '2026-05-11 08:45:00.000000', 19, 7),
    (20, b'0', NULL, 1, 8),

    (21, b'1', '2026-05-12 13:10:00.000000', 2, 9),
    (22, b'0', NULL, 3, 10),
    (23, b'1', '2026-05-13 15:30:00.000000', 4, 11),
    (24, b'0', NULL, 5, 12),
    (25, b'1', '2026-05-14 09:20:00.000000', 6, 1),

    (26, b'0', NULL, 7, 2),
    (27, b'1', '2026-05-15 10:10:00.000000', 8, 3),
    (28, b'0', NULL, 9, 4),
    (29, b'1', '2026-05-16 12:00:00.000000', 10, 5),
    (30, b'0', NULL, 11, 6);