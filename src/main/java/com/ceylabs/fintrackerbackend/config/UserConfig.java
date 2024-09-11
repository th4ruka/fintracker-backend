package com.ceylabs.fintrackerbackend.config;

import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Configuration
public class UserConfig {

    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository){
        return args ->{
            User u1 = new User((long) 1L,"tharuka", "tharuka@email.com", LocalDate.of(2008, Month.JANUARY,17));
            User u2 = new User("tehan", "tehan@email.com", LocalDate.of(1998, Month.JANUARY,17));
            userRepository.saveAll(List.of(u1, u2));
        };
    }
}
