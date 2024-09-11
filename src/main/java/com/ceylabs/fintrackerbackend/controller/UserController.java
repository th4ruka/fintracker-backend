<<<<<<<< HEAD:src/main/java/com/cashai/cashaibackend/controller/UserController.java
package com.cashai.cashaibackend.controller;

import com.cashai.cashaibackend.model.User;
import com.cashai.cashaibackend.service.UserService;
========
package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.service.UserService;
>>>>>>>> 46a1152 (Refactor User components):src/main/java/com/ceylabs/fintrackerbackend/controller/UserController.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
//        this.userService = new UserService(); //no need to do this if @Autowired is used
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers(){
        return userService.getUsers();
    }

    @PostMapping
    public void registerNewUser(@RequestBody User user){
        userService.addNewUser(user);
    }

    @DeleteMapping(path="{userId}")
    public void deleteUser(@PathVariable("userId") Long userId){
        userService.deleteUser(userId);
    }

    @PutMapping(path="{userId}")
    public void updateUser(
            @PathVariable("userId") Long userId,
            @PathVariable(required = false)String name,
            @PathVariable(required = false)String email){
        System.out.println("inside controller");
        userService.updateUser(userId, name, email);
    }

}
