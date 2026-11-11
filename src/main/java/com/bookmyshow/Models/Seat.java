package com.bookmyshow.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CollectionId;

@Getter
@Setter
@Entity(name = "seat")
public class Seat extends BaseModel {

    private String number;

    // Seat M:1 SeatType
    @ManyToOne
    private SeatType seatType;

    @Column(name = "seat_row")
    private Integer row;

    @Column(name = "seat_column")
    private Integer column;

    @ManyToOne
    private Screen screen;
}
