package com.bookmyshow.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity(name="bms_user")
public class User extends BaseModel {
    private String email;
    private String password;
    private String name;
    @OneToMany(mappedBy="user")
    private List<Booking> bookings;
}
