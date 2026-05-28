package com.jeez.zp.room;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(
        basePackages = {"com.jeez.zp.room", "com.jeez.common"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.jeez\\.common\\.mq\\..*"
        )
)
@MapperScan("com.jeez.zp.room.mapper")
public class ZpServiceRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZpServiceRoomApplication.class, args);
    }
}
