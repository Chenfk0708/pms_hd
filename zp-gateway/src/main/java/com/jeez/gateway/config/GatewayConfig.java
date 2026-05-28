package com.jeez.gateway.config;

import cn.dev33.satoken.dao.SaTokenDao;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

/**
 * 网关配置类
 *
 * @author Jeez
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
public class GatewayConfig {

    /**
     * 配置 Sa-Token 的 RedisDao
     */
    @Bean
    @ConditionalOnMissingBean
    public SaTokenDao saTokenDao(RedisTemplate<String, Object> redisTemplate) {
        return new SaTokenDaoImpl(redisTemplate);
    }

    /**
     * 配置 RedisTemplate Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 配置序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Sa-Token 自定义 RedisDao 实现类
     */
    public static class SaTokenDaoImpl implements SaTokenDao {

        /**
         * 键值不存在时的过期时间标识
         */
        private static final long NOT_VALUE_EXPIRE = -1;

        private final RedisTemplate<String, Object> redisTemplate;

        public SaTokenDaoImpl(RedisTemplate<String, Object> redisTemplate) {
            this.redisTemplate = redisTemplate;
            // RedisTemplate的序列化器已经在bean配置中设置好了
        }

        @Override
        public String get(String key) {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            // 处理类型转换：Integer、Long等数值类型转换为String
            if (value instanceof String) {
                return (String) value;
            } else {
                // 对于Integer、Long等其他类型，直接转为String
                return value.toString();
            }
        }

        @Override
        public void set(String key, String value, long timeout) {
            if (timeout <= 0) {
                redisTemplate.opsForValue().set(key, value);
            } else {
                // Sa-Token的timeout单位是秒，RedisTemplate需要显式指定时间单位
                redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            }
        }

        @Override
        public void update(String key, String value) {
            long expire = getTimeout(key);
            if (expire == NOT_VALUE_EXPIRE) {
                set(key, value, 0);
            } else {
                set(key, value, expire);
            }
        }

        @Override
        public void delete(String key) {
            redisTemplate.delete(key);
        }

        @Override
        public long getTimeout(String key) {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            // Redis返回值：-2表示key不存在，-1表示key永不过期
            if (expire == null || expire < 0) {
                return NOT_VALUE_EXPIRE;
            }
            return expire;
        }

        @Override
        public void updateTimeout(String key, long timeout) {
            if (timeout <= 0) {
                // Redis 不支持设置 null 超时时间，跳过
                return;
            } else {
                redisTemplate.expire(key, timeout, java.util.concurrent.TimeUnit.SECONDS);
            }
        }

        @Override
        public Object getObject(String key) {
            return redisTemplate.opsForValue().get(key);
        }

        @Override
        public void setObject(String key, Object object, long timeout) {
            if (timeout <= 0) {
                redisTemplate.opsForValue().set(key, object);
            } else {
                // Sa-Token的timeout单位是秒，RedisTemplate需要显式指定时间单位
                redisTemplate.opsForValue().set(key, object, timeout, TimeUnit.SECONDS);
            }
        }

        @Override
        public void updateObject(String key, Object object) {
            long expire = getTimeout(key);
            if (expire == NOT_VALUE_EXPIRE) {
                setObject(key, object, 0);
            } else {
                setObject(key, object, expire);
            }
        }

        @Override
        public void deleteObject(String key) {
            redisTemplate.delete(key);
        }

        @Override
        public long getObjectTimeout(String key) {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            // Redis返回值：-2表示key不存在，-1表示key永不过期
            if (expire == null || expire < 0) {
                return NOT_VALUE_EXPIRE;
            }
            return expire;
        }

        @Override
        public void updateObjectTimeout(String key, long timeout) {
            if (timeout <= 0) {
                // 移除过期时间，永久保存
                redisTemplate.persist(key);
            } else {
                redisTemplate.expire(key, timeout, java.util.concurrent.TimeUnit.SECONDS);
            }
        }

        @Override
        public java.util.List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
            // 简单实现：根据前缀搜索键
            java.util.Set<String> keys = redisTemplate.keys(prefix + "*" + keyword + "*");
            java.util.List<String> result = new java.util.ArrayList<>(keys);

            // 简单排序
            if (sortType) {
                result.sort(String::compareTo);
            } else {
                result.sort((a, b) -> b.compareTo(a));
            }

            // 分页处理
            int fromIndex = Math.min(start, result.size());
            int toIndex = Math.min(start + size, result.size());

            return result.subList(fromIndex, toIndex);
        }
    }
}
