package com.bookmyshow.Repositories;

import com.bookmyshow.Models.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre,Long> {
    Theatre findByName(String phoenix);
    List<Theatre> findByRegion_Id(Long regionId);
}
