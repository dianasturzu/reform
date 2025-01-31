-- 🔹 1 SEDINTA 1:1
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT '1 sedinta 1:1', 100.0, 1, 'ONE_ON_ONE', 'NONE', '2025-01-01', '2025-12-31'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '1 sedinta 1:1' AND availablefrom = '2025-01-01');

-- 🔹 1 SEDINTA 1:2 (JANUARY PRICING)
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT '1 sedinta 1:2', 95.0, 1, 'ONE_ON_TWO', 'NONE', '2025-01-01', '2025-01-31'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '1 sedinta 1:2' AND availablefrom = '2025-01-01');

-- 🔹 1 SEDINTA 1:2 (VALENTINE'S DAY PROMO - FEBRUARY)
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT '1 sedinta 1:2', 85.0, 1, 'ONE_ON_TWO', 'NONE', '2025-02-01', '2025-02-29'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '1 sedinta 1:2' AND availablefrom = '2025-02-01');

-- 🔹 ABONAMENT 10 SEDINTE 1:1 (STANDARD PRICE)
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT 'abonament 10 sedinte 1:1', 900.0, 10, 'ONE_ON_ONE', 'TWO_MONTHS', '2025-01-01', '2025-06-30'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'abonament 10 sedinte 1:1' AND availablefrom = '2025-01-01');

-- 🔹 ABONAMENT 10 SEDINTE 1:1 (DISCOUNTED SUMMER PRICE)
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT 'abonament 10 sedinte 1:1', 850.0, 10, 'ONE_ON_ONE', 'TWO_MONTHS', '2025-07-01', '2025-12-31'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'abonament 10 sedinte 1:1' AND availablefrom = '2025-07-01');

-- 🔹 ABONAMENT 10 SEDINTE 1:2 (STANDARD PRICE)
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT 'abonament 10 sedinte 1:2', 700.0, 10, 'ONE_ON_TWO', 'TWO_MONTHS', '2025-01-01', '2025-12-31'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'abonament 10 sedinte 1:2' AND availablefrom = '2025-01-01');

-- 🔹 ABONAMENT 5 SEDINTE 1:1 (STANDARD PRICE)
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT 'abonament 5 sedinte 1:1', 450.0, 5, 'ONE_ON_ONE', 'ONE_MONTH', '2025-01-01', '2025-12-31'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'abonament 5 sedinte 1:1' AND availablefrom = '2025-01-01');

-- 🔹 ABONAMENT 5 SEDINTE 1:2 (STANDARD PRICE)
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT 'abonament 5 sedinte 1:2', 375.0, 5, 'ONE_ON_TWO', 'ONE_MONTH', '2025-01-01', '2025-12-31'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'abonament 5 sedinte 1:2' AND availablefrom = '2025-01-01');

-- 🔹 SEDINTA BARTER (FIXED 0 RON)
INSERT INTO product (name, price, tokenqty, tokentype, tokenexpirepolicy, availablefrom, availableuntil)
SELECT 'sedinta barter', 0.0, 1, 'BARTER', 'NONE', '2025-01-01', '2025-12-31'
    WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'sedinta barter' AND availablefrom = '2025-01-01');
