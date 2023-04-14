# Roles
INSERT INTO roles (id,name) VALUES (1,'ROLE_CUSTOMER');
INSERT INTO roles (id,name) VALUES (2,'ROLE_ENTERPRISE');
INSERT INTO roles (id,name) VALUES (3,'ROLE_SUPPLIER');
INSERT INTO roles (id,name) VALUES (4,'ROLE_DISTRIBUTOR');
INSERT INTO roles (id,name) VALUES (5,'ROLE_ADMIN');

# Products
INSERT INTO products (id, article_no, name, price, external_number, weight) VALUES(1, 1001, 'Vordere linke Tür', 19.99, 'VL', 16.0);
INSERT INTO products (id, article_no, name, price, external_number, weight) VALUES(2, 1002, 'Vordere rechte Tür', 19.99, 'VR', 16.0);
INSERT INTO products (id, article_no, name, price, external_number, weight) VALUES(3, 1003, 'Hintere linke Tür', 19.99, 'HL', 16.0);
INSERT INTO products (id, article_no, name, price, external_number, weight) VALUES(4, 1004, 'Hintere rechte Tür', 19.99, 'HR', 16.0);

# Components - Products must exit before
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('Pfusch und weg', 'Blech außen', 'VL-BA', 1);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('gut und billig', 'Blech innen', 'VL-BI', 1);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('Röchling', 'Verkleidung', 'VL-VK', 1);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('Bose', 'Lautsprecher', 'VL-LS', 1);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('Pfusch und weg', 'Blech außen', 'VR-BA', 2);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('gut und billig', 'Blech innen', 'VR-BI', 2);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('Igus', 'Verkleidung', 'VR-VK', 2);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('Bang & Olufsen', 'Lautsprecher', 'VR-LS', 2);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('Pfusch und weg', 'Blech außen', 'HL-BA', 3);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('gut und billig', 'Blech innen', 'HL-BI', 3);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('ISE', 'Verkleidung', 'HL-VK', 3);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('JBL', 'Lautsprecher', 'HL-LS', 3);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('Pfusch und weg', 'Blech außen', 'HR-BA', 4);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('gut und billig', 'Blech innen', 'HR-BI', 4);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('ISE', 'Verkleidung', 'HR-VK', 4);
INSERT INTO components (manufacturer, name, article_no, product_id) VALUES('harmann kardon', 'Lautsprecher', 'HR-LS', 4);

# Demo users - password is role name
INSERT INTO users (user_type, id, email, name, password) VALUES('customer', 1, 'customer@customer.de', 'Curry Customer', '$2a$10$lIPd7e88K.afrmpy4bNzr.WgjOs4nUDplf23uOMvj8w4ZvdZrbFeO');
INSERT INTO users (user_type, id, email, name, password) VALUES('enterprise', 2, 'enterprise@enterprise.de', 'Ernie Enterprise', '$2a$10$EMG/sySD.ZFPuP0HNcvCWeaOtwQfYD4Igwy0AYjNLCuS8HG7g6T8i');
INSERT INTO users (user_type, id, email, name, password) VALUES('admin', 3, 'admin@admin.de', 'Anton Admin', '$2a$10$EF..2.kDKRfRomvHmsv2UOQDm.X5P43vdoi9YuydogeXS4PNILOsC');
INSERT INTO users (user_type, id, email, name, password) VALUES('supplier', 4, 'supplier@supplier.de', 'Samson Supplier', '$2a$10$AQZTMc92XvgIK0yT65rLgOWhWQu4ByBikdHetzHtHic60hzrjnL7G');
INSERT INTO users (user_type, id, email, name, password) VALUES('distributor', 5, 'distributor@distributor.de', 'Dieter Distributor', '$2a$10$QWpsgZfCS1QQLwaKaqR8gOVAEAPqKEnNdIRVNJRpX8FgRzdkmTaTG');

# user roles mapping
INSERT INTO users_roles (user_id, role_id) VALUES(1, 1);
INSERT INTO users_roles (user_id, role_id) VALUES(2, 2);
INSERT INTO users_roles (user_id, role_id) VALUES(3, 3);
INSERT INTO users_roles (user_id, role_id) VALUES(4, 4);
INSERT INTO users_roles (user_id, role_id) VALUES(5, 5);
