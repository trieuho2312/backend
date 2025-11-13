-- USERS
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER','SHOP_OWNER','ADMIN')),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ADMINISTRATIVE DIVISIONS
CREATE TABLE IF NOT EXISTS province (
    province_id SERIAL PRIMARY KEY,
    province_name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS district (
    district_id SERIAL PRIMARY KEY,
    district_name VARCHAR(100) NOT NULL,
    province_id INT NOT NULL REFERENCES province(province_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS ward (
    ward_id SERIAL PRIMARY KEY,
    ward_name VARCHAR(100) NOT NULL,
    district_id INT NOT NULL REFERENCES district(district_id) ON DELETE CASCADE
);

-- ADDRESS BOOK
CREATE TABLE IF NOT EXISTS address_book (
    address_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    address_detail TEXT NOT NULL,
    phone_no VARCHAR(15),
    ward_id INT REFERENCES ward(ward_id)
);

-- SHOP
CREATE TABLE IF NOT EXISTS shop (
    shop_id SERIAL PRIMARY KEY,
    owner_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    average_rating DECIMAL(2,1) DEFAULT 0 CHECK (average_rating >= 0 AND average_rating <= 5),
    address_id INT REFERENCES address_book(address_id)
);

-- CATEGORIES
CREATE TABLE IF NOT EXISTS categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) UNIQUE NOT NULL
);

-- PRODUCTS
CREATE TABLE IF NOT EXISTS products (
    product_id SERIAL PRIMARY KEY,
    shop_id INT NOT NULL REFERENCES shop(shop_id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    description TEXT,
    stock_quantity INT DEFAULT 0 CHECK (stock_quantity >= 0),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    category_id INT REFERENCES categories(category_id)
);

-- PRODUCT IMAGES
CREATE TABLE IF NOT EXISTS product_images (
    image_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    image_type VARCHAR(50),
    description TEXT
);

-- REVIEWS
CREATE TABLE IF NOT EXISTS product_review (
    product_review_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    content TEXT,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, user_id)
);

-- CART
CREATE TABLE IF NOT EXISTS cart (
    cart_id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL REFERENCES users(user_id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS cart_items (
    cart_id INT NOT NULL REFERENCES cart(cart_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    PRIMARY KEY (cart_id, product_id)
);

-- ORDERS
CREATE TABLE IF NOT EXISTS orders (
    order_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    order_cost DECIMAL(12,2) NOT NULL CHECK (order_cost >= 0)
);
CREATE TABLE IF NOT EXISTS order_items (
    order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id),
    quantity INT NOT NULL CHECK (quantity > 0),
    price_snapshot DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (order_id, product_id)
);

-- SHIPPING PROVIDER
CREATE TABLE IF NOT EXISTS shipping_provider (
    provider_id SERIAL PRIMARY KEY,
    provider_name VARCHAR(100) UNIQUE NOT NULL,
    contact_number VARCHAR(20)
);

-- SHIPMENTS
CREATE TABLE IF NOT EXISTS shipments (
    shipment_id SERIAL PRIMARY KEY,
    order_id INT UNIQUE NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    provider_id INT NOT NULL REFERENCES shipping_provider(provider_id),
    deliver_date DATE,
    shipping_cost DECIMAL(12,2) CHECK (shipping_cost >= 0),
    receiver_address TEXT,
    pick_up_address TEXT
);

-- TRANSACTIONS
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id SERIAL PRIMARY KEY,
    order_id INT UNIQUE NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    transaction_method VARCHAR(50) NOT NULL,
    transaction_status VARCHAR(30) NOT NULL DEFAULT 'pending',
    total_cost DECIMAL(12,2) NOT NULL CHECK (total_cost >= 0),
    tax_amount DECIMAL(12,2) DEFAULT 0 CHECK (tax_amount >= 0),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CONVERSATIONS & MESSAGES
CREATE TABLE IF NOT EXISTS conversation (
    conversation_id SERIAL PRIMARY KEY,
    user1_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    user2_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_not_self_pair CHECK (user1_id <> user2_id),
    CONSTRAINT uq_pair UNIQUE (user1_id, user2_id)
);
CREATE TABLE IF NOT EXISTS message (
    message_id SERIAL PRIMARY KEY,
    conversation_id INT NOT NULL REFERENCES conversation(conversation_id) ON DELETE CASCADE,
    sender_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for search
CREATE INDEX IF NOT EXISTS idx_products_name ON products (name);
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category_id);
