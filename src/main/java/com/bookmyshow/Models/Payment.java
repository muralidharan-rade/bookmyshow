package com.bookmyshow.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Payment extends BaseModel {
    private String referenceNumber;
    private Double amount;
    @Enumerated(EnumType.STRING)
    private PaymentMode mode;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @ManyToOne
    private Booking booking;
}
