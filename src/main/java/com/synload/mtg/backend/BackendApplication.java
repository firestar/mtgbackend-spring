package com.synload.mtg.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.synload.mtg.backend.api.scryfall.ScryFallHandler;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
        ScryFallHandler handler = new ScryFallHandler();
        handler.getCardById("ae155ee2-008f-4dc6-82bf-476be7baa224");
    }
}
