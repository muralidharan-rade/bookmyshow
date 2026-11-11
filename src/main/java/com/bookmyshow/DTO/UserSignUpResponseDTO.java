package com.bookmyshow.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignUpResponseDTO {
    private Long userId;
    private String message;
    private ResponseStatus status;
}
