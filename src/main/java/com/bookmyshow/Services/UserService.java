package com.bookmyshow.Services;

import com.bookmyshow.Models.User;
import com.bookmyshow.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User signup(String name, String email, String password) {
        // Fetch user by user email
        // Check if user already exists
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }
        // We will create a user
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(password));
        return userRepository.save(user);
    }
}
// Hash -> "skdbvsjadkbc" -> hashedOutput
// Hash -> "skdbvsjadkbc" -> hashedOutput(Same as above)

// Sign up -> Hash(userPassword) -> store it on DB as hashedUserPass
// log in -> user gives logInPassword -> Hash(logInPassword) == hashedUserPass

//$2a$10$N9qo8uLOickgx2ZMRZoMye.IjZAgcfl7p92ldGxad68LJZdL17lhWy
//    └──────── salt ─────────┘ └────────── hash ──────────┘

// Signup flow1 : "passpass" -> hashfunction(passpass, randomSalt1) -> randomSalt1.IjZAgcfl7p92_randomSalt1_ldGxad68LJZdL17lhWy
// Signup flow1 : "passpass" -> hashfunction(passpass, randomSalt2) -> .IjZAgcfl7p92ldGxad68LJZdL17lhWy

//Old login flow
// Login flow1 : "passpass" -> hashfunction(passpass) -> .IjZAgcfl7p92ldGxad68LJZdL17lhWy
// Login flow2 : "passpass" -> hashfunction(passpass) -> .IjZAgcfl7p92ldGxad68LJZdL17lhWy

// New login flow
//Login flow1: "passpass" -> fetch stored password from DB ->(randomSalt1.IjZAgcfl7p92_randomSalt1_ldGxad68LJZdL17lhWy)
// -> I read the randomsalt -> randomSalt1 -> hash("passpass12"(curr_pass), randomSalt1) ->randomSalt1.IjZAgcfl7p92_randomSalt1_ldGxad68LJZdL17lhWy


// hash(passpass_randomsalt) -> result1
// hash(passpass) -> result2

