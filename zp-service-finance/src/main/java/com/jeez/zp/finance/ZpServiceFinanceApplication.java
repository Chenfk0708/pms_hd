package com.jeez.zp.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.jeez.zp.finance.mapper")
public class ZpServiceFinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZpServiceFinanceApplication.class, args);
    }
}
