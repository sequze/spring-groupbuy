-- Demo seed for manual UI testing.
-- Apply it to a database after the application schema has been created.
-- Password for every demo account is: password

insert into roles(name, created_at, updated_at)
values ('ROLE_USER', now(), now())
on conflict (name) do nothing;

insert into roles(name, created_at, updated_at)
values ('ROLE_ADMIN', now(), now())
on conflict (name) do nothing;

insert into users(email, password_hash, display_name, is_enabled, created_at, updated_at)
values
    ('admin@groupbuy.test', '$2a$10$GK6y.F3QKEj2izQb3QRskuGSFGRwMfAkeQmh.Rbp1AitLZ2QgC2Re', 'Системный администратор', true, now(), now()),
    ('anna@groupbuy.test', '$2a$10$GK6y.F3QKEj2izQb3QRskuGSFGRwMfAkeQmh.Rbp1AitLZ2QgC2Re', 'Анна Орлова', true, now(), now()),
    ('boris@groupbuy.test', '$2a$10$GK6y.F3QKEj2izQb3QRskuGSFGRwMfAkeQmh.Rbp1AitLZ2QgC2Re', 'Борис Ким', true, now(), now()),
    ('clara@groupbuy.test', '$2a$10$GK6y.F3QKEj2izQb3QRskuGSFGRwMfAkeQmh.Rbp1AitLZ2QgC2Re', 'Клара Смирнова', true, now(), now()),
    ('dmitry@groupbuy.test', '$2a$10$GK6y.F3QKEj2izQb3QRskuGSFGRwMfAkeQmh.Rbp1AitLZ2QgC2Re', 'Дмитрий Волков', true, now(), now()),
    ('disabled@groupbuy.test', '$2a$10$GK6y.F3QKEj2izQb3QRskuGSFGRwMfAkeQmh.Rbp1AitLZ2QgC2Re', 'Отключенный пользователь', false, now(), now())
on conflict (email) do nothing;

insert into user_roles(user_id, role_id)
select u.id, r.id
from users u
join roles r on r.name = 'ROLE_USER'
where u.email in (
    'admin@groupbuy.test',
    'anna@groupbuy.test',
    'boris@groupbuy.test',
    'clara@groupbuy.test',
    'dmitry@groupbuy.test',
    'disabled@groupbuy.test'
)
and not exists (
    select 1 from user_roles ur where ur.user_id = u.id and ur.role_id = r.id
);

insert into user_roles(user_id, role_id)
select u.id, r.id
from users u
join roles r on r.name = 'ROLE_ADMIN'
where u.email = 'admin@groupbuy.test'
and not exists (
    select 1 from user_roles ur where ur.user_id = u.id and ur.role_id = r.id
);

insert into workspaces(name, description, owner_id, join_token, is_active, created_at, updated_at)
select 'Офисная кухня', 'Закупки кофе, чая, снеков и расходников для общей кухни.', u.id, 'demo-office-kitchen', true, now(), now()
from users u
where u.email = 'anna@groupbuy.test'
and not exists (select 1 from workspaces w where w.join_token = 'demo-office-kitchen');

insert into workspaces(name, description, owner_id, join_token, is_active, created_at, updated_at)
select 'Поездка на конференцию', 'Совместный список техники, мерча и расходников для выездной команды.', u.id, 'demo-conference-trip', true, now(), now()
from users u
where u.email = 'boris@groupbuy.test'
and not exists (select 1 from workspaces w where w.join_token = 'demo-conference-trip');

insert into workspaces(name, description, owner_id, join_token, is_active, created_at, updated_at)
select 'Архивная закупка', 'Неактивное пространство для проверки запрета вступления по токену.', u.id, 'demo-archived-space', false, now(), now()
from users u
where u.email = 'anna@groupbuy.test'
and not exists (select 1 from workspaces w where w.join_token = 'demo-archived-space');

insert into workspace_members(workspace_id, user_id, role, joined_at, invited_by_id, created_at, updated_at)
select w.id, u.id, member.role, member.joined_at, invited_by.id, now(), now()
from (
    values
        ('demo-office-kitchen', 'anna@groupbuy.test', 'SPACE_ADMIN', now() - interval '30 days', null),
        ('demo-office-kitchen', 'boris@groupbuy.test', 'SPACE_ADMIN', now() - interval '24 days', 'anna@groupbuy.test'),
        ('demo-office-kitchen', 'clara@groupbuy.test', 'SPACE_MEMBER', now() - interval '18 days', 'anna@groupbuy.test'),
        ('demo-conference-trip', 'boris@groupbuy.test', 'SPACE_ADMIN', now() - interval '20 days', null),
        ('demo-conference-trip', 'clara@groupbuy.test', 'SPACE_MEMBER', now() - interval '15 days', 'boris@groupbuy.test'),
        ('demo-conference-trip', 'dmitry@groupbuy.test', 'SPACE_MEMBER', now() - interval '12 days', 'boris@groupbuy.test'),
        ('demo-archived-space', 'anna@groupbuy.test', 'SPACE_ADMIN', now() - interval '60 days', null)
) as member(join_token, email, role, joined_at, invited_by_email)
join workspaces w on w.join_token = member.join_token
join users u on u.email = member.email
left join users invited_by on invited_by.email = member.invited_by_email
where not exists (
    select 1 from workspace_members wm where wm.workspace_id = w.id and wm.user_id = u.id
);

insert into currency_rates(base_currency, currency_code, rate_from_base, fetched_at, created_at, updated_at)
values
    ('EUR', 'EUR', 1.0000000000, now(), now(), now()),
    ('EUR', 'RUB', 100.0000000000, now(), now(), now()),
    ('EUR', 'USD', 1.0800000000, now(), now(), now()),
    ('EUR', 'CNY', 7.8200000000, now(), now(), now()),
    ('EUR', 'TRY', 35.5000000000, now(), now(), now())
on conflict (base_currency, currency_code) do update
set rate_from_base = excluded.rate_from_base,
    fetched_at = excluded.fetched_at,
    updated_at = now();

insert into purchase_items(
    workspace_id, author_id, title, description, product_url, quantity, unit,
    price_amount, price_currency, status, rejection_reason, approved_at, approved_by_id,
    rejected_at, rejected_by_id, created_at, updated_at
)
select w.id, author.id, item.title, item.description, item.product_url, item.quantity, item.unit,
       item.price_amount, item.price_currency, item.status, item.rejection_reason,
       item.approved_at, approved_by.id, item.rejected_at, rejected_by.id, now(), now()
from (
    values
        ('demo-office-kitchen', 'anna@groupbuy.test', 'Кофе в зернах 1 кг', 'Средняя обжарка для офисной кофемашины.', 'https://example.com/coffee-beans', 6, 'пачка', 18.50, 'EUR', 'APPROVED', null, now() - interval '8 days', 'boris@groupbuy.test', null, null),
        ('demo-office-kitchen', 'clara@groupbuy.test', 'Овсяное молоко', 'Бариста-версия для кофе, коробка 12 штук.', 'https://example.com/oat-milk', 12, 'шт', 320.00, 'RUB', 'APPROVED', null, now() - interval '7 days', 'anna@groupbuy.test', null, null),
        ('demo-office-kitchen', 'boris@groupbuy.test', 'Бумажные стаканчики', 'Стаканчики 250 мл, нужны для гостевых встреч.', null, 500, 'шт', 42.00, 'USD', 'NEW', null, null, null, null, null),
        ('demo-office-kitchen', 'clara@groupbuy.test', 'Премиальный шоколад', 'Слишком дорогой вариант для регулярной закупки.', 'https://example.com/chocolate', 20, 'шт', 9.90, 'EUR', 'REJECTED', 'Выбрали более бюджетный аналог.', null, null, now() - interval '5 days', 'boris@groupbuy.test'),
        ('demo-conference-trip', 'boris@groupbuy.test', 'USB-C хабы', 'По одному хабу на каждого докладчика.', 'https://example.com/usb-c-hub', 4, 'шт', 29.99, 'USD', 'APPROVED', null, now() - interval '6 days', 'boris@groupbuy.test', null, null),
        ('demo-conference-trip', 'clara@groupbuy.test', 'Бейдж-холдеры', 'Прозрачные держатели для бейджей участников.', null, 80, 'шт', 4.50, 'CNY', 'APPROVED', null, now() - interval '4 days', 'boris@groupbuy.test', null, null),
        ('demo-conference-trip', 'dmitry@groupbuy.test', 'Пауэрбанки 20000 mAh', 'Запасные аккумуляторы для стенда.', 'https://example.com/powerbank', 3, 'шт', 39.00, 'EUR', 'NEW', null, null, null, null, null)
) as item(join_token, author_email, title, description, product_url, quantity, unit, price_amount, price_currency, status, rejection_reason, approved_at, approved_by_email, rejected_at, rejected_by_email)
join workspaces w on w.join_token = item.join_token
join users author on author.email = item.author_email
left join users approved_by on approved_by.email = item.approved_by_email
left join users rejected_by on rejected_by.email = item.rejected_by_email
where not exists (
    select 1 from purchase_items pi where pi.workspace_id = w.id and pi.title = item.title
);

insert into comments(purchase_item_id, author_id, content, is_edited, created_at, updated_at)
select pi.id, author.id, comment.content, comment.is_edited, now(), now()
from (
    values
        ('demo-office-kitchen', 'Кофе в зернах 1 кг', 'anna@groupbuy.test', 'Проверила цену, поставщик готов привезти завтра.', false),
        ('demo-office-kitchen', 'Кофе в зернах 1 кг', 'boris@groupbuy.test', 'Одобрил, берем в текущий заказ.', true),
        ('demo-office-kitchen', 'Бумажные стаканчики', 'clara@groupbuy.test', 'Можно заменить на многоразовые кружки для команды?', false),
        ('demo-office-kitchen', 'Премиальный шоколад', 'anna@groupbuy.test', 'Оставим как пример отклоненной позиции.', false),
        ('demo-conference-trip', 'USB-C хабы', 'dmitry@groupbuy.test', 'Нужны именно с HDMI, иначе часть ноутбуков не подключится.', false),
        ('demo-conference-trip', 'Бейдж-холдеры', 'clara@groupbuy.test', 'Количество с запасом на гостей.', false)
) as comment(join_token, item_title, author_email, content, is_edited)
join workspaces w on w.join_token = comment.join_token
join purchase_items pi on pi.workspace_id = w.id and pi.title = comment.item_title
join users author on author.email = comment.author_email
where not exists (
    select 1 from comments c
    where c.purchase_item_id = pi.id and c.author_id = author.id and c.content = comment.content
);

insert into orders(workspace_id, created_by_id, title, description, status, total_amount, base_currency, submitted_at, closed_at, created_at, updated_at)
select w.id, creator.id, order_seed.title, order_seed.description, order_seed.status,
       order_seed.total_amount, 'EUR', order_seed.submitted_at, order_seed.closed_at, now(), now()
from (
    values
        ('demo-office-kitchen', 'anna@groupbuy.test', 'Заказ кухни на май', 'Позиции, которые уже можно закупать.', 'DRAFT', 114.54, null, null),
        ('demo-conference-trip', 'boris@groupbuy.test', 'Заказ для конференции', 'Подтвержденный заказ перед оплатой.', 'SUBMITTED', 122.55, now() - interval '2 days', null),
        ('demo-conference-trip', 'boris@groupbuy.test', 'Закрытый заказ на бейджи', 'Исторический заказ для проверки статуса CLOSED.', 'CLOSED', 46.04, now() - interval '10 days', now() - interval '8 days')
) as order_seed(join_token, creator_email, title, description, status, total_amount, submitted_at, closed_at)
join workspaces w on w.join_token = order_seed.join_token
join users creator on creator.email = order_seed.creator_email
where not exists (
    select 1 from orders o where o.workspace_id = w.id and o.title = order_seed.title
);

insert into order_items(order_id, purchase_item_id, item_title_snapshot, quantity_snapshot, price_snapshot, currency_snapshot, created_at, updated_at)
select o.id, pi.id, oi.item_title_snapshot, oi.quantity_snapshot, oi.price_snapshot, 'EUR', now(), now()
from (
    values
        ('demo-office-kitchen', 'Заказ кухни на май', 'Кофе в зернах 1 кг', 'Кофе в зернах 1 кг', 6, 18.50),
        ('demo-office-kitchen', 'Заказ кухни на май', 'Овсяное молоко', 'Овсяное молоко', 12, 3.20),
        ('demo-conference-trip', 'Заказ для конференции', 'USB-C хабы', 'USB-C хабы', 4, 27.77),
        ('demo-conference-trip', 'Заказ для конференции', 'Бейдж-холдеры', 'Бейдж-холдеры', 80, 0.58),
        ('demo-conference-trip', 'Закрытый заказ на бейджи', 'Бейдж-холдеры', 'Бейдж-холдеры', 80, 0.58)
) as oi(join_token, order_title, item_title, item_title_snapshot, quantity_snapshot, price_snapshot)
join workspaces w on w.join_token = oi.join_token
join orders o on o.workspace_id = w.id and o.title = oi.order_title
join purchase_items pi on pi.workspace_id = w.id and pi.title = oi.item_title
where not exists (
    select 1 from order_items existing where existing.order_id = o.id and existing.purchase_item_id = pi.id
);
