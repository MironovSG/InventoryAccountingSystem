-- Создание схемы базы данных для системы учета и выдачи ТМЦ

-- Таблица подразделений
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES departments(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_department_code ON departments(code);
CREATE INDEX idx_department_parent ON departments(parent_id);

-- Таблица пользователей
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    department_id BIGINT REFERENCES departments(id),
    phone VARCHAR(20),
    position VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_department ON users(department_id);
CREATE INDEX idx_user_role ON users(role);

-- Таблица категорий материалов
CREATE TABLE material_categories (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    parent_id BIGINT REFERENCES material_categories(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_category_code ON material_categories(code);
CREATE INDEX idx_category_parent ON material_categories(parent_id);

-- Таблица материалов
CREATE TABLE materials (
    id BIGSERIAL PRIMARY KEY,
    article VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(300) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL REFERENCES material_categories(id),
    unit_of_measure VARCHAR(20) NOT NULL,
    current_quantity DECIMAL(15,3) NOT NULL DEFAULT 0,
    min_quantity DECIMAL(15,3),
    max_quantity DECIMAL(15,3),
    critical_quantity DECIMAL(15,3),
    storage_location VARCHAR(200),
    storage_conditions TEXT,
    unit_price DECIMAL(15,2),
    manufacturer VARCHAR(200),
    supplier VARCHAR(200),
    barcode VARCHAR(100),
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_material_article ON materials(article);
CREATE INDEX idx_material_name ON materials(name);
CREATE INDEX idx_material_category ON materials(category_id);
CREATE INDEX idx_material_quantity ON materials(current_quantity);

-- Таблица заявок
CREATE TABLE requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    department_id BIGINT NOT NULL REFERENCES departments(id),
    purpose VARCHAR(500) NOT NULL,
    notes TEXT,
    approved_by_id BIGINT REFERENCES users(id),
    approved_at TIMESTAMP,
    issued_by_id BIGINT REFERENCES users(id),
    issued_at TIMESTAMP,
    rejection_reason TEXT,
    priority INTEGER DEFAULT 0,
    expected_date TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_request_number ON requests(request_number);
CREATE INDEX idx_request_status ON requests(status);
CREATE INDEX idx_request_requester ON requests(requester_id);
CREATE INDEX idx_request_department ON requests(department_id);
CREATE INDEX idx_request_created ON requests(created_at);

-- Таблица позиций заявок
CREATE TABLE request_items (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
    material_id BIGINT NOT NULL REFERENCES materials(id),
    requested_quantity DECIMAL(15,3) NOT NULL,
    approved_quantity DECIMAL(15,3),
    issued_quantity DECIMAL(15,3),
    notes TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_request_item_request ON request_items(request_id);
CREATE INDEX idx_request_item_material ON request_items(material_id);

-- Таблица движений материалов
CREATE TABLE material_movements (
    id BIGSERIAL PRIMARY KEY,
    material_id BIGINT NOT NULL REFERENCES materials(id),
    movement_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(15,3) NOT NULL,
    quantity_before DECIMAL(15,3),
    quantity_after DECIMAL(15,3),
    request_id BIGINT REFERENCES requests(id),
    user_id BIGINT REFERENCES users(id),
    department_id BIGINT REFERENCES departments(id),
    document_number VARCHAR(100),
    notes TEXT,
    unit_price DECIMAL(15,2),
    total_price DECIMAL(15,2),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_movement_material ON material_movements(material_id);
CREATE INDEX idx_movement_type ON material_movements(movement_type);
CREATE INDEX idx_movement_date ON material_movements(created_at);
CREATE INDEX idx_movement_request ON material_movements(request_id);
CREATE INDEX idx_movement_department ON material_movements(department_id);

-- Таблица журнала аудита
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    description TEXT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_date ON audit_logs(created_at);
