package com.zerobase.nsbackend;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableBatchProcessing
@EnableFeignClients
@EnableJpaAuditing
@SpringBootApplication
public class NsBackEndApplication {

  public static void main(String[] args) {
    SpringApplication.run(NsBackEndApplication.class, args);
  }

}
