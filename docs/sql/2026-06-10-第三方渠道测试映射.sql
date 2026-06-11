-- 本地 Apifox 测试映射：美团酒店 / 美团民宿拆分。
-- 依赖本地已有 camp_id=10001、poi_id=11001、room_category_id=2061750967433125889。

INSERT INTO channel_account (
    account_id,
    camp_id,
    channel_id,
    channel_name,
    account_name,
    out_account_id,
    status,
    authorized_at,
    config_json
) VALUES (
    25351,
    10001,
    31,
    '美团酒店测试',
    '美团酒店本地测试账号',
    'mt-hotel-account-25351',
    'authorized',
    NOW(),
    CAST('{"channelCode":"meituan_hotel","usage":"local-apifox-test"}' AS JSON)
) ON DUPLICATE KEY UPDATE
    camp_id = VALUES(camp_id),
    channel_id = VALUES(channel_id),
    channel_name = VALUES(channel_name),
    account_name = VALUES(account_name),
    out_account_id = VALUES(out_account_id),
    status = VALUES(status),
    config_json = VALUES(config_json);

INSERT INTO channel_account (
    account_id,
    camp_id,
    channel_id,
    channel_name,
    account_name,
    out_account_id,
    status,
    authorized_at,
    config_json
) VALUES (
    25352,
    10001,
    32,
    '美团民宿测试',
    '美团民宿本地测试账号',
    'mt-homestay-account-25352',
    'authorized',
    NOW(),
    CAST('{"channelCode":"meituan_homestay","usage":"local-apifox-test"}' AS JSON)
) ON DUPLICATE KEY UPDATE
    camp_id = VALUES(camp_id),
    channel_id = VALUES(channel_id),
    channel_name = VALUES(channel_name),
    account_name = VALUES(account_name),
    out_account_id = VALUES(out_account_id),
    status = VALUES(status),
    config_json = VALUES(config_json);

INSERT INTO channel_poi_rel (
    id,
    camp_id,
    account_id,
    poi_id,
    out_poi_id,
    sync_status
) VALUES (
    2535101,
    10001,
    25351,
    11001,
    'mt-poi-11001',
    'synced'
) ON DUPLICATE KEY UPDATE
    camp_id = VALUES(camp_id),
    poi_id = VALUES(poi_id),
    out_poi_id = VALUES(out_poi_id),
    sync_status = VALUES(sync_status);

INSERT INTO channel_poi_rel (
    id,
    camp_id,
    account_id,
    poi_id,
    out_poi_id,
    sync_status
) VALUES (
    2535201,
    10001,
    25352,
    11001,
    'mt-poi-11001',
    'synced'
) ON DUPLICATE KEY UPDATE
    camp_id = VALUES(camp_id),
    poi_id = VALUES(poi_id),
    out_poi_id = VALUES(out_poi_id),
    sync_status = VALUES(sync_status);

INSERT INTO channel_room_category_rel (
    id,
    camp_id,
    account_id,
    room_category_id,
    out_room_category_id,
    project_type,
    shelf_status,
    audit_status
) VALUES (
    2535102,
    10001,
    25351,
    2061750967433125889,
    'mt-room-2061750967433125889',
    'calendar_room',
    'on_shelf',
    'approved'
) ON DUPLICATE KEY UPDATE
    camp_id = VALUES(camp_id),
    room_category_id = VALUES(room_category_id),
    out_room_category_id = VALUES(out_room_category_id),
    project_type = VALUES(project_type),
    shelf_status = VALUES(shelf_status),
    audit_status = VALUES(audit_status);

INSERT INTO channel_room_category_rel (
    id,
    camp_id,
    account_id,
    room_category_id,
    out_room_category_id,
    project_type,
    shelf_status,
    audit_status
) VALUES (
    2535202,
    10001,
    25352,
    2061750967433125889,
    'mt-room-2061750967433125889',
    'calendar_room',
    'on_shelf',
    'approved'
) ON DUPLICATE KEY UPDATE
    camp_id = VALUES(camp_id),
    room_category_id = VALUES(room_category_id),
    out_room_category_id = VALUES(out_room_category_id),
    project_type = VALUES(project_type),
    shelf_status = VALUES(shelf_status),
    audit_status = VALUES(audit_status);
