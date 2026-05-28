package com.jeez.zp.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(
        basePackages = {"com.jeez.zp.platform", "com.jeez.common"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.jeez\\.common\\.mq\\..*"
        )
)
@MapperScan("com.jeez.zp.platform.mapper")
@EnableTransactionManagement
public class ZpServicePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZpServicePlatformApplication.class, args);
    }
}
