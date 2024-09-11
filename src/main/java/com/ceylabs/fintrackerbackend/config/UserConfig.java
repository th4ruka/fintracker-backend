package com.ceylabs.fintrackerbackend.config;

import com.ceylabs.fintrackerbackend.model.Account;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.AccountRepository;
import com.ceylabs.fintrackerbackend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Configuration
public class UserConfig {

    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository, AccountRepository accountRepository){
        return args ->{
            User u1 = new User((long) 1L,"tharuka", "tharuka@email.com", LocalDate.of(2008, Month.JANUARY,17));
            User u2 = new User("tehan", "tehan@email.com", LocalDate.of(1998, Month.JANUARY,17));
            userRepository.saveAll(List.of(u1, u2));

            List<User> users = userRepository.findAll();
            User user1 = users.get(0);
            User user2 = users.get(1);

            Account acc1 = new Account("Savings Account", new BigDecimal("5000.00"), user1);
            Account acc2 = new Account("Checking Account", new BigDecimal("1200.00"), user1);
            Account acc3 = new Account("Business Account", new BigDecimal("3000.00"), user2);


            accountRepository.saveAll(List.of(acc1, acc2, acc3));
        };
    }
}
