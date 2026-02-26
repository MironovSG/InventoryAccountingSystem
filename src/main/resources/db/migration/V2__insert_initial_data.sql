-- Вставка начальных данных

-- Создание подразделений
INSERT INTO departments (code, name, description, active) VALUES
('ROOT', 'Головной офис', 'Главное подразделение', true),
('IT', 'ИТ отдел', 'Отдел информационных технологий', true),
('SERVICE', 'Сервисный центр', 'Сервисный центр', true),
('WAREHOUSE', 'Склад', 'Складское помещение', true);

-- Создание категорий материалов
INSERT INTO material_categories (code, name, description, active) VALUES
('ELEC', 'Электроника', 'Электронные компоненты и устройства', true),
('TOOLS', 'Инструменты', 'Ручные и электроинструменты', true),
('SPARE', 'Запчасти', 'Запасные части и комплектующие', true),
('CONSUMABLES', 'Расходники', 'Расходные материалы', true),
('OFFICE', 'Офис', 'Офисные принадлежности', true);

-- Создание пользователя администратора
-- Пароль: admin123 (зашифрован с помощью BCrypt)
INSERT INTO users (username, password, email, last_name, first_name, role, active) VALUES
('admin', '$2a$10$8.H7Gk9L0OzV3d8Ew8GUCe5vB7YFz8YE9.nNL2L1tJvdC8PzZvGRG', 'admin@company.com', 'Администратор', 'Системный', 'ADMIN', true);

-- Создание пользователя МОЛ
-- Пароль: mol123
INSERT INTO users (username, password, email, last_name, first_name, role, department_id, active) VALUES
('mol_user', '$2a$10$8.H7Gk9L0OzV3d8Ew8GUCe5vB7YFz8YE9.nNL2L1tJvdC8PzZvGRG', 'mol@company.com', 'Кладовщик', 'Иван', 'MOL', 4, true);

-- Создание пользователя инженера
-- Пароль: engineer123
INSERT INTO users (username, password, email, last_name, first_name, role, department_id, active) VALUES
('engineer_user', '$2a$10$8.H7Gk9L0OzV3d8Ew8GUCe5vB7YFz8YE9.nNL2L1tJvdC8PzZvGRG', 'engineer@company.com', 'Петров', 'Петр', 'ENGINEER', 3, true);

-- Создание пользователя руководителя
-- Пароль: manager123
INSERT INTO users (username, password, email, last_name, first_name, role, department_id, active) VALUES
('manager_user', '$2a$10$8.H7Gk9L0OzV3d8Ew8GUCe5vB7YFz8YE9.nNL2L1tJvdC8PzZvGRG', 'manager@company.com', 'Сидоров', 'Сергей', 'MANAGER', 3, true);

-- Создание примеров материалов
INSERT INTO materials (article, name, description, category_id, unit_of_measure, current_quantity, min_quantity, critical_quantity, unit_price, active) VALUES
('RES-100K', 'Резистор 100кОм', 'Резистор 100кОм 0.25Вт', 1, 'шт', 500, 100, 50, 0.50, true),
('CAP-10UF', 'Конденсатор 10мкФ', 'Электролитический конденсатор 10мкФ 25В', 1, 'шт', 300, 50, 25, 1.20, true),
('LED-RED', 'Светодиод красный', 'Светодиод красный 5мм', 1, 'шт', 200, 100, 50, 0.80, true),
('CABLE-USB', 'Кабель USB', 'Кабель USB Type-A - Type-C 1м', 1, 'шт', 50, 20, 10, 5.50, true),
('SCREW-M3', 'Винт М3х10', 'Винт с крестовой головкой М3х10', 3, 'шт', 1000, 200, 100, 0.30, true),
('SOLDER-WIRE', 'Припой ПОС-61', 'Припой ПОС-61 проволока 1мм 100г', 4, 'шт', 25, 10, 5, 8.00, true),
('FLUX-PASTE', 'Флюс паста', 'Флюс паста 10мл', 4, 'шт', 15, 10, 5, 3.50, true),
('PAPER-A4', 'Бумага А4', 'Бумага офисная А4 500 листов', 5, 'пачка', 30, 10, 5, 3.00, true);

-- Создание примера заявки
INSERT INTO requests (request_number, status, requester_id, department_id, purpose, notes, priority, created_at) VALUES
('REQ-20260101-0001', 'ACCEPTED', 3, 3, 'Ремонт оборудования клиента №12345', 'Срочный ремонт', 1, CURRENT_TIMESTAMP);

-- Добавление позиций к заявке
INSERT INTO request_items (request_id, material_id, requested_quantity, notes) VALUES
(1, 1, 10, 'Для замены на плате'),
(1, 2, 5, 'Для замены на плате'),
(1, 6, 1, 'Для пайки компонентов');

COMMENT ON TABLE departments IS 'Подразделения организации';
COMMENT ON TABLE users IS 'Пользователи системы';
COMMENT ON TABLE material_categories IS 'Категории товарно-материальных ценностей';
COMMENT ON TABLE materials IS 'Товарно-материальные ценности';
COMMENT ON TABLE requests IS 'Заявки на выдачу ТМЦ';
COMMENT ON TABLE request_items IS 'Позиции заявок';
COMMENT ON TABLE material_movements IS 'Движения ТМЦ (история операций)';
COMMENT ON TABLE audit_logs IS 'Журнал аудита действий пользователей';
