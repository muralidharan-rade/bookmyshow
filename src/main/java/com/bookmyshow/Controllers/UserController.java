package com.bookmyshow.Controllers;

import com.bookmyshow.DTO.ResponseStatus;
import com.bookmyshow.DTO.UserSignUpRequestDTO;
import com.bookmyshow.DTO.UserSignUpResponseDTO;
import com.bookmyshow.Models.User;
import com.bookmyshow.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    public UserSignUpResponseDTO signup(UserSignUpRequestDTO requestDTO) {
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
