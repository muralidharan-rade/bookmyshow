package com.bookmyshow.Repositories;

import com.bookmyshow.Models.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Long> {

    List<Screen> findByTheatre_Name(String phoenix);
    List<Screen> findByTheatre_Id(Long theatreId);
}
