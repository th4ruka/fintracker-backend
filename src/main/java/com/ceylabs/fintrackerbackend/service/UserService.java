
package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service Layer
 */
//@Component //exactly same as service
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public void addNewUser(User user) {
        Optional<User> userByEmail = userRepository.findUserByEmail(user.getEmail());
        if (userByEmail.isPresent()){
            throw new IllegalStateException("Email exists");
        }
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if(!exists){
            throw new IllegalStateException("User ID: "+ userId + " does not exist.");
        }
        userRepository.deleteById(userId);
    }

    @Transactional //no need to implement jpa query
    public void updateUser(Long userId, String name, String email) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(()->new IllegalStateException(
//                        "User with id: "+userId+" doesn't exist."
//                ));

//        if(name != null && name.length() > 0 &&
//        !Objects.equals(user.getName(), name)){
//            user.setName(name);
//            userRepository.save(user);
//            System.out.println("Modified name");
//        }
//
//        if(email != null && email.length() > 0 &&
//                !Objects.equals(user.getEmail(), email)){
//            Optional<User> userOptional = userRepository.findUserByEmail(email);
//            if(userOptional.isPresent()){
//                throw new IllegalStateException("Email already exists.");
//            }
//            user.setEmail(email);
//            userRepository.save(user);
//            System.out.println("Modified email");
//
//        }

        Optional<User> optinalEntity = userRepository.findById(userId);

        if(optinalEntity.isPresent()){
            User existingUser = optinalEntity.get();
            if(name != null) existingUser.setName(name);
            if(email != null) existingUser.setEmail(email);
            User savedUser = userRepository.save(existingUser);
        }else{
            throw new IllegalStateException("User with id: "+userId+" doesn't exist.");
        }
    }
}
