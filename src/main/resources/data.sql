DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM customers;

INSERT INTO customers (id, code, full_name, email, active) VALUES
    (1, 'CUST-001', 'Nguyen Van An', 'an.nguyen@example.com', true),
    (2, 'CUST-002', 'Tran Thi Binh', 'binh.tran@example.com', true),
    (3, 'CUST-003', 'Le Minh Chau', 'chau.le@example.com', false);

INSERT INTO orders (id, customer_id, created_by, created_at) VALUES
    (1001, 1, 'seed-script', '2026-06-24 08:30:00.000000'),
    (1002, 2, 'seed-script', '2026-06-24 09:15:00.000000');

INSERT INTO order_items (order_id, product_code, quantity) VALUES
    (1001, 'SKU-CHAIR-01', 2),
    (1001, 'SKU-DESK-01', 1),
    (1002, 'SKU-MONITOR-24', 2),
    (1002, 'SKU-KEYBOARD-01', 1);
