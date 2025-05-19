<h1>💳 Bank Card Management System</h1>

<p>RESTful-сервис для управления банковскими картами и пользователями, реализованный с помощью Spring Boot, с авторизацией через JWT и хранением данных в PostgreSQL.<p>



<h2>🚀 Технологии</h2>
<ul>
<li>Java 17+</li>
<li>Spring Boot, Spring Security + JWT, Spring Data JPA + Hibernate</li>
<li>PostgreSQL 14+</li>
<li>Liquibase</li>
<li>Swagger / OpenAPI</li>
<li>Docker + Docker Compose</li>
<li>JUnit</li>
<li>Mockito</li>
</ul>



<h2>⚙️ Как запустить</h2>
<h3>1. Клонируйте репозиторий</h3>

<p>git clone https://github.com/malishkreet/bankrest.git</p>
<p>cd bankres</p>


<h3>2. Собери проект (если нужно)</h3>

<p>mvn clean package</p>

<h3>Запусти с Docker</h3>
<p>docker-compose up --build</p>


<p>Приложение будет доступно по адресу:</p>
<p>http://localhost:8080/swagger-ui/index.html</p>

<h3>🗂️ Swagger: Авторизация</h3>
<p>Большинство эндпоинтов защищены JWT-токеном.</p>

<h3>Как получить токен:</h3>

<p>1. Выполни POST-запрос на /auth/login с телом:</p>
<p>
{
  "username": "your_username",
  "password": "your_password"
}
</p>
<p>2. Скопируй токен из ответа:</p>
<p>
{
  "token": "eyJhbGciOiJIUzI1NiIsInR..."
}
</p>

<p>3. Нажми Authorize в Swagger и введи:</p>
<p>твой_токен</p>

<p>По дефолту есть админ:</p>
<b>Логин:    admin</b>
<p></p>
<b>Логин:    admin123</b>

<h2>👤 Роли пользователей</h2>
<p>USER</p>
<p>	Просмотр и управление своими картами</p>

<p>ADMIN</p>
<p>Управление всеми пользователями и картами, удаление, блокировка</p>

<h2>💾 Данные БД (по умолчанию)</h2>
<p>PostgreSQL поднимается в Docker-контейнере:</p>

<p>Хост: localhost</p>

<p>Порт: 5432</p>

<p>БД: bankdb</p>

<p>Пользователь: postgres</p>

<p>Пароль: postgres</p>

<h2>📌 Примечания</h2>
<ul>
<li>Все миграции выполняются через Liquibase</li>
<li>JWT-секрет и другие параметры настраиваются в application.properties</li>
<li>Данные не сохраняются между рестартами, если не подключить внешнее хранилище</li>
</ul>
