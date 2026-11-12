package com.bookmyshow.Controllers;

import com.bookmyshow.DTO.ResponseStatus;
import com.bookmyshow.DTO.UserSignUpRequestDTO;
import com.bookmyshow.DTO.UserSignUpResponseDTO;
import com.bookmyshow.Models.User;
import com.bookmyshow.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public UserSignUpResponseDTO signup(@RequestBody UserSignUpRequestDTO requestDTO) {
        UserSignUpResponseDTO responseDTO = new UserSignUpResponseDTO();
        try {
            User user = userService.signup(requestDTO.getName(), requestDTO.getEmail(), requestDTO.getPassword());
            responseDTO.setUserId(user.getId());
            responseDTO.setMessage("User signup successful");
            responseDTO.setStatus(ResponseStatus.SUCCESS);
        } catch (Exception e) {
            responseDTO.setMessage("User signup failed");
            responseDTO.setStatus(ResponseStatus.FAILURE);
        }
        return responseDTO;
    }
}
