-- Insert sample users using multi-insert
INSERT IGNORE INTO user (name, email, password, address, is_verified, registration_date, created_date, updated_date) VALUES
('admin', 'admin@gmail.com', '$2a$10$AL9BqJniJofdBOp379GR7.zmm6vTK7oHVEIpWzj7xHab0AVzlUkwG', 'Admin Address', TRUE, NOW(), NOW(), NOW()),
('Khun Nyan', 'khunnyan@gmail.com', '$2a$10$AL9BqJniJofdBOp379GR7.zmm6vTK7oHVEIpWzj7xHab0AVzlUkwG', 'Myanmar', FALSE, NOW(), NOW(), NOW());

-- Insert sample books using multi-insert
INSERT IGNORE INTO book (isbn, title, author, is_available, created_date, updated_date) VALUES
('978-0134685991', 'Effective Java', 'Joshua Bloch', TRUE, NOW(), NOW()),
('978-0596009205', 'Head First Design Patterns', 'Eric Freeman', TRUE, NOW(), NOW()),
('978-0321356680', 'Effective C++', 'Scott Meyers', TRUE, NOW(), NOW()),
('978-0132350884', 'Clean Code', 'Robert C. Martin', TRUE, NOW(), NOW()),
('978-0201633610', 'Design Patterns', 'Gang of Four', TRUE, NOW(), NOW()),
('978-0201633611', 'Programming for Kids', 'Ei Maung', TRUE, NOW(), NOW());

-- Verify the admin user was created
SELECT 'Admin user created successfully' as message, 
       name, email, is_verified 
FROM user 
WHERE email = 'admin@gmail.com';

-- Verify books were inserted
SELECT 'Books inserted successfully' as message, 
       COUNT(*) as book_count 
FROM book;