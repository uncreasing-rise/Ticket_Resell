package com.swd392.ticket_resell_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketResellBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketResellBeApplication.class, args);
    }

}
