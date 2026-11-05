package com.mss301.watch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsWatchDe190685Application {

	public static void main(String[] args) {
		SpringApplication.run(MsWatchDe190685Application.class, args);
	}

}
