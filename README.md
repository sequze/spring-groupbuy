Сервис совместных покупок groupbuy

Стек: Spring Boot, PostgreSQL, Spring Security

## Локальный запуск

Для локального `bootRun` приложение по умолчанию подключается к PostgreSQL на `localhost:5432`:

```bash
docker compose up postgres -d
./gradlew bootRun
```

## Docker Compose

Полный запуск приложения и PostgreSQL:

```bash
docker compose up --build
```