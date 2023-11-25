package com.cashai.cashaibackend;

import com.cashai.cashaibackend.user.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
public class CashaiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashaiBackendApplication.class, args);
	}

}
