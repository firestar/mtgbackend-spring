package com.synload.mtg.scryfall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableScheduling
public class ScryfallApplication {

  public static void main(String[] args) {
    SpringApplication.run(ScryfallApplication.class, args);
  }
}
