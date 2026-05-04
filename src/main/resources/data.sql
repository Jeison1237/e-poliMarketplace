-- Users (passwords are BCrypt encoded: 'password123')
INSERT INTO users (username, email, password, first_name, last_name, phone, address, role, active, created_at, updated_at)
VALUES
('admin', 'admin@e-poli.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyPGu6cAi', 'Admin', 'Sistema', '555-0001', 'Av. Principal 100', 'ADMIN', true, NOW(), NOW()),
('vendedor1', 'vendedor1@e-poli.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyPGu6cAi', 'Carlos', 'García', '555-0002', 'Calle Comercio 45', 'SELLER', true, NOW(), NOW()),
('vendedor2', 'vendedor2@e-poli.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyPGu6cAi', 'María', 'López', '555-0003', 'Boulevard Industria 78', 'SELLER', true, NOW(), NOW()),
('vendedor3', 'vendedor3@e-poli.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyPGu6cAi', 'Ana', 'Martínez', '555-0004', 'Paseo del Sol 22', 'SELLER', true, NOW(), NOW()),
('comprador1', 'comprador1@e-poli.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyPGu6cAi', 'Juan', 'Pérez', '555-0005', 'Calle Flores 33', 'BUYER', true, NOW(), NOW()),
('comprador2', 'comprador2@e-poli.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyPGu6cAi', 'Laura', 'Torres', '555-0006', 'Av. Libertad 56', 'BUYER', true, NOW(), NOW());

-- Sellers
INSERT INTO sellers (user_id, store_name, description, logo_url, rating, total_sales, verified, active, created_at)
VALUES
(2, 'TechStore Pro', 'La mejor tienda de tecnología y electrónica. Productos originales con garantía.', 'https://via.placeholder.com/100x100/4f46e5/ffffff?text=TP', 4.8, 350, true, true, NOW()),
(3, 'Moda & Style', 'Ropa y accesorios de moda para todas las ocasiones. Envío rápido garantizado.', 'https://via.placeholder.com/100x100/ec4899/ffffff?text=MS', 4.6, 280, true, true, NOW()),
(4, 'Casa y Hogar Express', 'Todo para tu hogar: muebles, decoración y artículos para el hogar.', 'https://via.placeholder.com/100x100/10b981/ffffff?text=CH', 4.5, 195, true, true, NOW());

-- Products - TechStore Pro (seller_id=1)
INSERT INTO products (name, description, price, image_url, category, stock, rating, review_count, active, featured, seller_id, created_at, updated_at)
VALUES
('Laptop Ultra Pro 15"', 'Potente laptop con procesador Intel Core i7, 16GB RAM, 512GB SSD. Perfecta para trabajo y gaming.', 1299.99, 'https://via.placeholder.com/400x300/4f46e5/ffffff?text=Laptop+Pro', 'Tecnología', 15, 4.8, 124, true, true, 1, NOW(), NOW()),
('Smartphone Galaxy X', 'Teléfono inteligente con pantalla AMOLED 6.7", cámara 108MP, batería 5000mAh.', 699.99, 'https://via.placeholder.com/400x300/6366f1/ffffff?text=Smartphone', 'Tecnología', 30, 4.7, 98, true, true, 1, NOW(), NOW()),
('Auriculares Inalámbricos Pro', 'Auriculares con cancelación activa de ruido, 40h batería, calidad de sonido premium.', 199.99, 'https://via.placeholder.com/400x300/8b5cf6/ffffff?text=Auriculares', 'Tecnología', 50, 4.9, 215, true, false, 1, NOW(), NOW()),
('Tablet Premium 10"', 'Tablet con pantalla 10 pulgadas, procesador octa-core, 256GB almacenamiento, ideal para trabajo y entretenimiento.', 449.99, 'https://via.placeholder.com/400x300/a78bfa/ffffff?text=Tablet', 'Tecnología', 20, 4.6, 67, true, false, 1, NOW(), NOW()),
('Smartwatch Fitness', 'Reloj inteligente con monitor cardíaco, GPS integrado, resistente al agua, compatible con iOS y Android.', 249.99, 'https://via.placeholder.com/400x300/7c3aed/ffffff?text=Smartwatch', 'Tecnología', 40, 4.5, 83, true, true, 1, NOW(), NOW());

-- Products - Moda & Style (seller_id=2)
INSERT INTO products (name, description, price, image_url, category, stock, rating, review_count, active, featured, seller_id, created_at, updated_at)
VALUES
('Vestido Elegante Primavera', 'Vestido floral de algodón suave, perfecto para ocasiones casuales y formales. Tallas S-XL.', 79.99, 'https://via.placeholder.com/400x300/ec4899/ffffff?text=Vestido', 'Moda', 25, 4.7, 156, true, true, 2, NOW(), NOW()),
('Jeans Premium Hombre', 'Jeans de corte slim con materiales de alta calidad. Cómodos y duraderos para el día a día.', 59.99, 'https://via.placeholder.com/400x300/db2777/ffffff?text=Jeans', 'Moda', 40, 4.5, 89, true, false, 2, NOW(), NOW()),
('Zapatos Deportivos Running', 'Zapatillas para correr con suela amortiguadora, transpirables y ligeras. Tallas 36-45.', 89.99, 'https://via.placeholder.com/400x300/f472b6/ffffff?text=Zapatos', 'Moda', 35, 4.8, 201, true, true, 2, NOW(), NOW()),
('Bolsa de Cuero Premium', 'Bolsa artesanal de cuero genuino, espaciosa y elegante. Disponible en varios colores.', 149.99, 'https://via.placeholder.com/400x300/be185d/ffffff?text=Bolsa', 'Moda', 15, 4.6, 72, true, false, 2, NOW(), NOW());

-- Products - Casa y Hogar Express (seller_id=3)
INSERT INTO products (name, description, price, image_url, category, stock, rating, review_count, active, featured, seller_id, created_at, updated_at)
VALUES
('Sofá Moderno 3 Plazas', 'Sofá de tela premium con diseño moderno, cómodo y duradero. Perfecto para sala de estar.', 599.99, 'https://via.placeholder.com/400x300/10b981/ffffff?text=Sofa', 'Hogar', 8, 4.7, 45, true, true, 3, NOW(), NOW()),
('Set Cocina Antiadherente', 'Juego completo de sartenes y ollas antiadherentes de alta resistencia, con mangos ergonómicos.', 129.99, 'https://via.placeholder.com/400x300/059669/ffffff?text=Cocina', 'Hogar', 20, 4.8, 118, true, false, 3, NOW(), NOW()),
('Lámpara LED Decorativa', 'Lámpara de diseño moderno con luz LED regulable, perfecta para dormitorio o sala.', 49.99, 'https://via.placeholder.com/400x300/34d399/ffffff?text=Lampara', 'Hogar', 45, 4.4, 67, true, false, 3, NOW(), NOW()),
('Escritorio Home Office', 'Escritorio funcional para trabajo en casa, con cajones y espacio para organizador de cables.', 299.99, 'https://via.placeholder.com/400x300/6ee7b7/ffffff?text=Escritorio', 'Hogar', 12, 4.6, 89, true, true, 3, NOW(), NOW());
