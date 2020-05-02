package com.xdclass.couponapp.service.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 **/
@Service
public class HelloService {
    @Scheduled(cron = "0/5 * * * * ?")
    public void hello() {
        System.out.println("enter hello job!");
    }
}
