# Руководство по установке и запуску

## Системные требования

- **Java**: JDK 17 или выше
- **Maven**: 3.6+ 
- **PostgreSQL**: 13 или выше
- **Память**: минимум 2 GB RAM
- **Диск**: минимум 500 MB свободного места

## Шаг 1: Установка зависимостей

### Установка Java

#### Windows:
1. Загрузите JDK 17 с сайта Oracle или Adoptium
2. Установите JDK, следуя инструкциям установщика
3. Добавьте `JAVA_HOME` в переменные среды
4. Проверьте установку: `java -version`

#### Linux:
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```

### Установка Maven

#### Windows:
1. Загрузите Maven с официального сайта
2. Распакуйте в папку (например, `C:\Program Files\Apache\Maven`)
3. Добавьте путь к Maven в переменную `PATH`
4. Проверьте: `mvn -version`

#### Linux:
```bash
sudo apt install maven
mvn -version
```

### Установка PostgreSQL

#### Windows:
1. Загрузите PostgreSQL Installer
2. Установите PostgreSQL, запомните пароль для пользователя `postgres`
3. Убедитесь, что PostgreSQL запущен

#### Linux:
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

## Шаг 2: Настройка базы данных

### Создание базы данных

Подключитесь к PostgreSQL:

```bash
# Windows
psql -U postgres

# Linux
sudo -u postgres psql
```

Выполните команды создания БД:

```sql
CREATE DATABASE inventory_db;
CREATE USER inventory_user WITH PASSWORD 'inventory_pass123';
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO inventory_user;
\q
```

### Настройка доступа (если необходимо)

Отредактируйте файл `pg_hba.conf` для разрешения локальных подключений:

```
# Найдите файл pg_hba.conf:
# Windows: C:\Program Files\PostgreSQL\[version]\data\pg_hba.conf
# Linux: /etc/postgresql/[version]/main/pg_hba.conf

# Добавьте или измените строку:
host    inventory_db    inventory_user    127.0.0.1/32    md5
```

Перезапустите PostgreSQL:

```bash
# Windows (в cmd от администратора)
net stop postgresql-x64-[version]
net start postgresql-x64-[version]

# Linux
sudo systemctl restart postgresql
```

## Шаг 3: Конфигурация приложения

### Настройка application.yml

Откройте файл `src/main/resources/application.yml` и настройте подключение к БД:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory_db
    username: inventory_user
    password: inventory_pass123
```

### Настройка почты (опционально)

Для отправки уведомлений настройте параметры SMTP:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

### Настройка JWT Secret

Для production измените JWT секрет:

```yaml
app:
  jwt:
    secret: your-very-long-and-secure-secret-key-here
```

## Шаг 4: Сборка проекта

Перейдите в корневую папку проекта и выполните:

```bash
mvn clean install
```

Это займет несколько минут при первом запуске, т.к. Maven загрузит все зависимости.

## Шаг 5: Запуск приложения

### Способ 1: Через Maven

```bash
mvn spring-boot:run
```

### Способ 2: Через JAR файл

```bash
java -jar target/inventory-system-1.0.0.jar
```

### Запуск в фоновом режиме (Linux)

```bash
nohup java -jar target/inventory-system-1.0.0.jar > app.log 2>&1 &
```

## Шаг 6: Проверка работы

### Проверка статуса приложения

Откройте браузер и перейдите по адресам:

- **Главная страница**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Health Check**: http://localhost:8080/api/management/health

### Первый вход

Используйте тестовые учетные данные:

| Роль | Логин | Пароль |
|------|-------|--------|
| Администратор | admin | admin123 |
| МОЛ | mol_user | mol123 |
| Инженер | engineer_user | engineer123 |
| Руководитель | manager_user | manager123 |

## Шаг 7: Инициализация данных

При первом запуске автоматически выполнятся миграции из папки `src/main/resources/db/migration/`:

1. `V1__initial_schema.sql` - создание таблиц
2. `V2__insert_initial_data.sql` - тестовые данные

## Решение проблем

### Ошибка подключения к БД

**Проблема**: `Connection refused` или `Authentication failed`

**Решение**:
1. Убедитесь, что PostgreSQL запущен
2. Проверьте логин/пароль в `application.yml`
3. Проверьте настройки `pg_hba.conf`
4. Убедитесь, что база данных создана

### Порт уже занят

**Проблема**: `Port 8080 is already in use`

**Решение**:
Измените порт в `application.yml`:

```yaml
server:
  port: 8081
```

### Ошибки при сборке Maven

**Проблема**: `Failed to execute goal`

**Решение**:
1. Очистите кэш Maven: `mvn clean`
2. Удалите папку `.m2/repository`
3. Проверьте наличие интернет-соединения
4. Попробуйте пересобрать: `mvn clean install -U`

### Hibernate ошибки

**Проблема**: `Table doesn't exist` или `Syntax error`

**Решение**:
1. Удалите все таблицы из БД
2. Перезапустите приложение для пересоздания схемы

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO inventory_user;
```

## Настройка для Production

### 1. Измените пароли

Замените все тестовые пароли в `V2__insert_initial_data.sql`

### 2. Настройте SSL

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
```

### 3. Настройте логирование

```yaml
logging:
  level:
    root: WARN
    com.systemtmc: INFO
  file:
    name: /var/log/inventory-system/app.log
```

### 4. Увеличьте производительность

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### 5. Настройте резервное копирование БД

Создайте скрипт автоматического бэкапа:

```bash
#!/bin/bash
pg_dump -U inventory_user inventory_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

## Мониторинг

### Проверка метрик

```bash
curl http://localhost:8080/api/management/metrics
```

### Prometheus

```bash
curl http://localhost:8080/api/management/prometheus
```

## Остановка приложения

### Graceful shutdown

```bash
# Найти PID процесса
ps aux | grep inventory-system

# Остановить приложение
kill -15 [PID]
```

### Через Ctrl+C

Если запущено через Maven или в терминале, нажмите `Ctrl+C`

## Дополнительная информация

- **Документация API**: http://localhost:8080/api/swagger-ui.html
- **Логи**: `logs/inventory-system.log`
- **Конфигурация**: `src/main/resources/application.yml`

## Поддержка

При возникновении проблем:
1. Проверьте логи приложения
2. Проверьте логи PostgreSQL
3. Обратитесь к README.md для дополнительной информации
