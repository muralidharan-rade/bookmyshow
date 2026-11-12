package com.bookmyshow.Repositories;

import com.bookmyshow.Models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreen_Id(Long screenId);
}
