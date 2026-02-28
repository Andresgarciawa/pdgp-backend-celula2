-- Crear base de datos
CREATE DATABASE authdb;
\c authdb;

-- Tabla de usuarios
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER')),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para búsqueda rápida por email
CREATE INDEX idx_users_email ON users(email);

---------------------------------------------------------------------------------------------
CREATE DATABASE catalogdb;
\c catalogdb;

-- Tabla de productos
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(150) NOT NULL,
                          description TEXT,
                          price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
                          stock INTEGER NOT NULL CHECK (stock >= 0),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla para idempotencia de eventos
CREATE TABLE processed_events (
                                  id BIGSERIAL PRIMARY KEY,
                                  event_id VARCHAR(100) NOT NULL UNIQUE,
                                  processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice opcional para búsquedas
CREATE INDEX idx_products_name ON products(name);

--------------------------------------------------------------------------------------------
CREATE DATABASE orderdb;
\c orderdb;

-- Tabla de pedidos
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        status VARCHAR(50) NOT NULL CHECK (status IN ('CREATED', 'CONFIRMED', 'CANCELLED')),
                        total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla detalle de pedido (muchos productos por pedido)
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Tabla para idempotencia en consumo de eventos
CREATE TABLE processed_events (
                                  id BIGSERIAL PRIMARY KEY,
                                  event_id VARCHAR(100) NOT NULL UNIQUE,
                                  processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);