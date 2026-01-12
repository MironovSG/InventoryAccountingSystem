# Руководство по развертыванию в Production

## Подготовка к развертыванию

### 1. Настройка окружения

Создайте отдельный профиль для production: `src/main/resources/application-prod.yml`

```yaml
spring:
  application:
    name: inventory-management-system-prod
  
  datasource:
    url: jdbc:postgresql://prod-db-server:5432/inventory_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

server:
  port: 8080
  servlet:
    context-path: /api
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE_PATH}
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000

logging:
  level:
    root: WARN
    com.systemtmc: INFO
  file:
    name: /var/log/inventory-system/app.log
    max-size: 50MB
    max-history: 60
```

### 2. Создание переменных окружения

Создайте файл `.env` (не добавляйте в git!):

```bash
DB_USERNAME=inventory_user
DB_PASSWORD=secure_password_here
MAIL_HOST=smtp.company.com
MAIL_PORT=587
MAIL_USERNAME=noreply@company.com
MAIL_PASSWORD=mail_password_here
JWT_SECRET=very_long_and_secure_secret_key_minimum_256_bits
SSL_KEYSTORE_PATH=/etc/ssl/inventory/keystore.p12
SSL_KEYSTORE_PASSWORD=keystore_password
```

### 3. Генерация SSL сертификата

```bash
# Создание самоподписанного сертификата для тестирования
keytool -genkeypair -alias inventory-system \
  -keyalg RSA -keysize 2048 -storetype PKCS12 \
  -keystore keystore.p12 -validity 3650 \
  -storepass your_password

# Для production используйте сертификат от CA (Let's Encrypt, DigiCert и т.д.)
```

## Развертывание на Linux сервере

### Вариант 1: Развертывание как Systemd Service

#### 1. Сборка приложения

```bash
mvn clean package -Pprod -DskipTests
```

#### 2. Копирование JAR на сервер

```bash
scp target/inventory-system-1.0.0.jar user@server:/opt/inventory-system/
scp application-prod.yml user@server:/opt/inventory-system/config/
```

#### 3. Создание пользователя для приложения

```bash
sudo useradd -r -s /bin/false inventory
sudo mkdir -p /opt/inventory-system
sudo mkdir -p /var/log/inventory-system
sudo chown -R inventory:inventory /opt/inventory-system
sudo chown -R inventory:inventory /var/log/inventory-system
```

#### 4. Создание systemd unit файла

Создайте `/etc/systemd/system/inventory-system.service`:

```ini
[Unit]
Description=Inventory Management System
After=syslog.target network.target postgresql.service

[Service]
User=inventory
Group=inventory
Type=simple
WorkingDirectory=/opt/inventory-system
ExecStart=/usr/bin/java \
  -Xms512m \
  -Xmx2g \
  -Dspring.profiles.active=prod \
  -Dspring.config.location=/opt/inventory-system/config/application-prod.yml \
  -jar /opt/inventory-system/inventory-system-1.0.0.jar

StandardOutput=journal
StandardError=journal
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

#### 5. Запуск сервиса

```bash
sudo systemctl daemon-reload
sudo systemctl enable inventory-system
sudo systemctl start inventory-system
sudo systemctl status inventory-system
```

#### 6. Просмотр логов

```bash
sudo journalctl -u inventory-system -f
```

### Вариант 2: Docker контейнер

#### 1. Создание Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/inventory-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

#### 2. Создание docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: inventory-db
    environment:
      POSTGRES_DB: inventory_db
      POSTGRES_USER: inventory_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - inventory-network
    restart: unless-stopped

  app:
    build: .
    container_name: inventory-app
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/inventory_db
      SPRING_DATASOURCE_USERNAME: inventory_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      APP_JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    networks:
      - inventory-network
    restart: unless-stopped
    volumes:
      - logs:/var/log/inventory-system

volumes:
  postgres-data:
  logs:

networks:
  inventory-network:
    driver: bridge
```

#### 3. Запуск Docker контейнеров

```bash
docker-compose up -d
docker-compose logs -f app
```

### Вариант 3: Kubernetes

#### 1. Создание ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: inventory-config
data:
  application-prod.yml: |
    # Ваш конфигурационный файл
```

#### 2. Создание Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: inventory-secrets
type: Opaque
data:
  db-password: <base64-encoded-password>
  jwt-secret: <base64-encoded-secret>
```

#### 3. Создание Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: inventory-system
spec:
  replicas: 3
  selector:
    matchLabels:
      app: inventory-system
  template:
    metadata:
      labels:
        app: inventory-system
    spec:
      containers:
      - name: inventory-app
        image: your-registry/inventory-system:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: inventory-secrets
              key: db-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: inventory-secrets
              key: jwt-secret
```

## Настройка Nginx как reverse proxy

Создайте `/etc/nginx/sites-available/inventory-system`:

```nginx
upstream inventory_backend {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name inventory.company.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name inventory.company.com;
    
    ssl_certificate /etc/ssl/certs/inventory.crt;
    ssl_certificate_key /etc/ssl/private/inventory.key;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    client_max_body_size 10M;
    
    location / {
        proxy_pass http://inventory_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    location /api {
        proxy_pass http://inventory_backend/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Активация конфигурации:

```bash
sudo ln -s /etc/nginx/sites-available/inventory-system /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## Мониторинг и логирование

### Настройка Prometheus

Добавьте в `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'inventory-system'
    metrics_path: '/api/management/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Настройка Grafana

1. Добавьте Prometheus как data source
2. Импортируйте дашборд для Spring Boot приложений (ID: 4701)
3. Настройте алерты для критических метрик

### Централизованное логирование (ELK Stack)

Настройка Logstash для сбора логов:

```ruby
input {
  file {
    path => "/var/log/inventory-system/app.log"
    start_position => "beginning"
    codec => multiline {
      pattern => "^%{TIMESTAMP_ISO8601}"
      negate => true
      what => "previous"
    }
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} - %{GREEDYDATA:log_message}" }
  }
  date {
    match => [ "timestamp", "yyyy-MM-dd HH:mm:ss" ]
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "inventory-system-%{+YYYY.MM.dd}"
  }
}
```

## Резервное копирование

### Автоматический бэкап PostgreSQL

Создайте скрипт `/opt/scripts/backup-inventory-db.sh`:

```bash
#!/bin/bash

BACKUP_DIR="/backup/inventory-db"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DB_NAME="inventory_db"
DB_USER="inventory_user"

mkdir -p $BACKUP_DIR

# Создание бэкапа
pg_dump -U $DB_USER -h localhost $DB_NAME | gzip > $BACKUP_DIR/backup_$TIMESTAMP.sql.gz

# Удаление бэкапов старше 30 дней
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete

# Загрузка в S3 (опционально)
# aws s3 cp $BACKUP_DIR/backup_$TIMESTAMP.sql.gz s3://your-bucket/backups/
```

Добавьте в crontab:

```bash
# Ежедневный бэкап в 2:00
0 2 * * * /opt/scripts/backup-inventory-db.sh
```

## Безопасность

### 1. Настройка файрвола (UFW)

```bash
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

### 2. Fail2Ban для защиты от брутфорса

Создайте `/etc/fail2ban/filter.d/inventory-system.conf`:

```ini
[Definition]
failregex = ^.*Authentication failed for user.*<HOST>.*$
ignoreregex =
```

### 3. Регулярные обновления

```bash
# Обновление системы
sudo apt update && sudo apt upgrade -y

# Обновление приложения
sudo systemctl stop inventory-system
sudo cp new-version.jar /opt/inventory-system/inventory-system-1.0.0.jar
sudo systemctl start inventory-system
```

## Проверка развертывания

### Health Check

```bash
curl https://inventory.company.com/api/management/health
```

### Smoke тесты

```bash
# Проверка входа
curl -X POST https://inventory.company.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Проверка API
curl https://inventory.company.com/api/materials/active \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Rollback процедура

При возникновении проблем:

```bash
# Остановка текущей версии
sudo systemctl stop inventory-system

# Восстановление предыдущей версии
sudo cp /opt/inventory-system/backup/inventory-system-previous.jar \
        /opt/inventory-system/inventory-system-1.0.0.jar

# Восстановление БД (если необходимо)
gunzip -c /backup/inventory-db/backup_TIMESTAMP.sql.gz | \
  psql -U inventory_user -h localhost inventory_db

# Запуск приложения
sudo systemctl start inventory-system
```

## Troubleshooting

### Проблема: Приложение не запускается

Проверьте:
1. Логи: `sudo journalctl -u inventory-system -n 100`
2. Доступность БД: `telnet db-server 5432`
3. Переменные окружения
4. Права доступа к файлам

### Проблема: Высокая нагрузка

1. Проверьте метрики: `/api/management/metrics`
2. Увеличьте memory settings: `-Xmx4g`
3. Оптимизируйте запросы к БД
4. Масштабируйте горизонтально (добавьте инстансы)

## Поддержка

Контакты технической поддержки:
- Email: support@company.com
- Телефон: +7 (XXX) XXX-XX-XX
- Wiki: https://wiki.company.com/inventory-system
