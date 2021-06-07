package com.example.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserRepository repository;
    @Autowired
    private Environment env;

    @PostMapping
    public void add(@RequestBody User user) {
        repository.save(user);
    }

    @GetMapping("/{id}")
    public User get(@PathVariable("id") int id) {
        return repository.findById(id).get();
    }

    @GetMapping
    public List<User> list() {
        return repository.findAll();
    }

    @GetMapping("/welcome")
    public String welcome() {
        return env.getProperty("app.user.welcome") + ". huangjinxxx";
    }

}
