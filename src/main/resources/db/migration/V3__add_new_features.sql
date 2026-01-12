-- Добавление новых полей и таблиц для расширенного функционала

-- Добавление поля warehouseType в таблицу materials
ALTER TABLE materials ADD COLUMN IF NOT EXISTS warehouse_type VARCHAR(50);

-- Добавление поля workGroup в таблицу users
ALTER TABLE users ADD COLUMN IF NOT EXISTS work_group VARCHAR(20);

-- Создание таблицы уведомлений
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    message TEXT,
    type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notification_date ON notifications(created_at);

-- Обновление существующих материалов (установка склада по умолчанию)
UPDATE materials SET warehouse_type = 'CONSUMABLES' WHERE warehouse_type IS NULL;

-- Комментарии
COMMENT ON COLUMN materials.warehouse_type IS 'Тип склада: CONSUMABLES, SPARE_PARTS, COMPONENTS';
COMMENT ON COLUMN users.work_group IS 'Рабочая группа специалиста: АРМ, ГОТО, ГОКС';
COMMENT ON TABLE notifications IS 'Push-уведомления для пользователей';
