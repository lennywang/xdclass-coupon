package com.xdclass.couponapp;

import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDubboConfig
@DubboComponentScan("com.xdclass.userapp.service.dubbo")
@SpringBootApplication
@EnableScheduling
public class CouponAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponAppApplication.class, args);
	}

}
