-- Вставка начальных данных

-- Создание подразделений
INSERT INTO departments (code, name, description, active, created_at, deleted) VALUES
('ROOT', 'Головной офис', 'Главное подразделение', true, CURRENT_TIMESTAMP, false),
('IT', 'ИТ отдел', 'Отдел информационных технологий', true, CURRENT_TIMESTAMP, false),
('SERVICE', 'Сервисный центр', 'Сервисный центр', true, CURRENT_TIMESTAMP, false),
('WAREHOUSE', 'Склад', 'Складское помещение', true, CURRENT_TIMESTAMP, false);

-- Создание категорий материалов
INSERT INTO material_categories (code, name, description, active, created_at, deleted) VALUES
('ELEC', 'Электроника', 'Электронные компоненты и устройства', true, CURRENT_TIMESTAMP, false),
('TOOLS', 'Инструменты', 'Ручные и электроинструменты', true, CURRENT_TIMESTAMP, false),
('SPARE', 'Запчасти', 'Запасные части и комплектующие', true, CURRENT_TIMESTAMP, false),
('CONSUMABLES', 'Расходники', 'Расходные материалы', true, CURRENT_TIMESTAMP, false),
('OFFICE', 'Офис', 'Офисные принадлежности', true, CURRENT_TIMESTAMP, false);

-- Создание пользователя администратора
-- Пароль: admin123 (зашифрован с помощью BCrypt)
-- ВАЖНО: Для генерации правильных хешей запустите класс PasswordGenerator или используйте онлайн генератор BCrypt
-- Временные хеши (замените на правильные после генерации):
INSERT INTO users (username, password, email, last_name, first_name, role, active, created_at, deleted) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@company.com', 'Администратор', 'Системный', 'ADMIN', true, CURRENT_TIMESTAMP, false);

-- Создание пользователя МОЛ
-- Пароль: mol123
INSERT INTO users (username, password, email, last_name, first_name, role, department_id, active, created_at, deleted) VALUES
('mol_user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'mol@company.com', 'Кладовщик', 'Иван', 'MOL', 4, true, CURRENT_TIMESTAMP, false);

-- Создание пользователя инженера
-- Пароль: engineer123
INSERT INTO users (username, password, email, last_name, first_name, role, department_id, active, created_at, deleted) VALUES
('engineer_user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'engineer@company.com', 'Петров', 'Петр', 'ENGINEER', 3, true, CURRENT_TIMESTAMP, false);

-- Создание пользователя руководителя
-- Пароль: manager123
INSERT INTO users (username, password, email, last_name, first_name, role, department_id, active, created_at, deleted) VALUES
('manager_user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'manager@company.com', 'Сидоров', 'Сергей', 'MANAGER', 3, true, CURRENT_TIMESTAMP, false);

-- Создание примеров материалов
-- category_id: 1=ELEC, 2=TOOLS, 3=SPARE, 4=CONSUMABLES, 5=OFFICE
INSERT INTO materials (article, name, description, category_id, unit_of_measure, current_quantity, min_quantity, critical_quantity, unit_price, active, created_at, deleted) VALUES
('RES-100K', 'Резистор 100кОм', 'Резистор 100кОм 0.25Вт', 1, 'шт', 500, 100, 50, 0.50, true, CURRENT_TIMESTAMP, false),
('CAP-10UF', 'Конденсатор 10мкФ', 'Электролитический конденсатор 10мкФ 25В', 1, 'шт', 300, 50, 25, 1.20, true, CURRENT_TIMESTAMP, false),
('LED-RED', 'Светодиод красный', 'Светодиод красный 5мм', 1, 'шт', 200, 100, 50, 0.80, true, CURRENT_TIMESTAMP, false),
('CABLE-USB', 'Кабель USB', 'Кабель USB Type-A - Type-C 1м', 1, 'шт', 50, 20, 10, 5.50, true, CURRENT_TIMESTAMP, false),
('SCREW-M3', 'Винт М3х10', 'Винт с крестовой головкой М3х10', 3, 'шт', 1000, 200, 100, 0.30, true, CURRENT_TIMESTAMP, false),
('SOLDER-WIRE', 'Припой ПОС-61', 'Припой ПОС-61 проволока 1мм 100г', 4, 'шт', 25, 10, 5, 8.00, true, CURRENT_TIMESTAMP, false),
('FLUX-PASTE', 'Флюс паста', 'Флюс паста 10мл', 4, 'шт', 15, 10, 5, 3.50, true, CURRENT_TIMESTAMP, false),
('PAPER-A4', 'Бумага А4', 'Бумага офисная А4 500 листов', 5, 'пачка', 30, 10, 5, 3.00, true, CURRENT_TIMESTAMP, false);

-- Создание примера заявки
INSERT INTO requests (request_number, status, requester_id, department_id, purpose, notes, priority, created_at, deleted) VALUES
('REQ-20260101-0001', 'ACCEPTED', 3, 3, 'Ремонт оборудования клиента №12345', 'Срочный ремонт', 1, CURRENT_TIMESTAMP, false);

-- Добавление позиций к заявке
INSERT INTO request_items (request_id, material_id, requested_quantity, notes, created_at, deleted) VALUES
(1, 1, 10, 'Для замены на плате', CURRENT_TIMESTAMP, false),
(1, 2, 5, 'Для замены на плате', CURRENT_TIMESTAMP, false),
(1, 6, 1, 'Для пайки компонентов', CURRENT_TIMESTAMP, false);

COMMENT ON TABLE departments IS 'Подразделения организации';
COMMENT ON TABLE users IS 'Пользователи системы';
COMMENT ON TABLE material_categories IS 'Категории товарно-материальных ценностей';
COMMENT ON TABLE materials IS 'Товарно-материальные ценности';
COMMENT ON TABLE requests IS 'Заявки на выдачу ТМЦ';
COMMENT ON TABLE request_items IS 'Позиции заявок';
COMMENT ON TABLE material_movements IS 'Движения ТМЦ (история операций)';
COMMENT ON TABLE audit_logs IS 'Журнал аудита действий пользователей';
