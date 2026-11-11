package com.bookmyshow.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Region extends BaseModel {

    private String name;

    @OneToMany(mappedBy = "region")
    private List<Theatre> theatres;
}
