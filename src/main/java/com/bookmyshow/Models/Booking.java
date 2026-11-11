package com.bookmyshow.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Booking extends BaseModel {

    @ManyToOne
    private User user;
    //Booking M:1 Movie
    @ManyToOne
    private Movie movie;
    // Booking M:1 SHow
    @ManyToOne
    private Show show;
    // Booking 1:M ShowSeat
    @OneToMany
    private List<ShowSeat> showSeats;

    @OneToMany(mappedBy = "booking")
    private List<Payment> payments;

    private BookingStatus status;

    private Double amount;
}
// Booking Table - List<Payment> - [p1, p2, p3]
// booking_payments