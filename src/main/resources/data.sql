-- Скрипт для автоматической загрузки данных при запуске приложения
-- (альтернатива Flyway migration, если не используется Flyway)

-- ВНИМАНИЕ: Этот файл выполняется только если в application.yml установлено:
-- spring.jpa.hibernate.ddl-auto=create или create-drop
-- И spring.sql.init.mode=always

-- Если используете Flyway, этот файл можно удалить
