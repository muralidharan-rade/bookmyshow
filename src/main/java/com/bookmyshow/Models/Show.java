package com.bookmyshow.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity(name="bms_show")
public class Show extends BaseModel {

    @ManyToOne // Show M:1 movie
    private Movie movie;

    private Date startTime;

    private Date endTime;

    @ManyToOne
    private Screen screen;

    @Enumerated(EnumType.STRING)
    @ElementCollection
    private List<Feature> features;
}
