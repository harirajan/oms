package com.hari.oms;

import org.springframework.boot.SpringApplication;

public class TestOmsApplication {

    public static void main(String[] args) {
        SpringApplication.from(OmsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
