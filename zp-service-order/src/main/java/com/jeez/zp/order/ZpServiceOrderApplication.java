package com.jeez.zp.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
        basePackages = {"com.jeez.zp.order", "com.jeez.common"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.jeez\\.common\\.mq\\..*"
        )
)
@MapperScan("com.jeez.zp.order.mapper")
public class ZpServiceOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZpServiceOrderApplication.class, args);
    }
}
