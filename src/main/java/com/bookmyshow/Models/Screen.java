package com.bookmyshow.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Screen extends BaseModel {

    private String name;

    @OneToMany(mappedBy = "screen") // Screen 1:M seats
    private List<Seat> seats;

    @OneToMany
    private List<Show> shows;

    @Enumerated(EnumType.STRING)
    @ElementCollection
    private List<Feature> features;

    @ManyToOne // Screen M:1 Theatre
    private Theatre theatre;
}

// Show - movieId, startTime, endTime, ScreenId, list<features>
// Show M-1 Screen
// City -> List of theatres -> List of screens -> List shows -> List of unique movies
// Show M - 1 Movie | ShowTable - id | showName | MovieId