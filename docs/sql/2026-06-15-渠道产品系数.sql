CREATE TABLE IF NOT EXISTS channel_product_price_coefficient (
    id BIGINT NOT NULL PRIMARY KEY,
    camp_id BIGINT NOT NULL COMMENT '门店/营地ID',
    room_category_id BIGINT NOT NULL COMMENT '房型ID',
    channel_id BIGINT NOT NULL COMMENT '渠道ID',
    product_name VARCHAR(255) NOT NULL COMMENT '渠道售卖产品名称',
    operator_type VARCHAR(8) NOT NULL COMMENT '运算符: + - * /',
    coefficient_value DECIMAL(12, 4) NOT NULL COMMENT '系数值；+/- 按元，*// 按比例',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_channel_product_price_coefficient (
        camp_id,
        room_category_id,
        channel_id,
        product_name
    ),
    KEY idx_channel_product_price_coefficient_channel (
        camp_id,
        channel_id
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='渠道售卖产品价格系数';
