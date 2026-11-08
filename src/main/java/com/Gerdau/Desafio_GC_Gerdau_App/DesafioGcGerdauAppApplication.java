package com.Gerdau.Desafio_GC_Gerdau_App;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class DesafioGcGerdauAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesafioGcGerdauAppApplication.class, args);
	}

}
