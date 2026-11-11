package com.bookmyshow.Services;

import com.bookmyshow.Models.*;
import com.bookmyshow.Repositories.BookingRepository;
import com.bookmyshow.Repositories.ShowRepository;
import com.bookmyshow.Repositories.ShowSeatRepository;
import com.bookmyshow.Repositories.UserRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter

@Service
public class BookingService {

    private UserRepository userRepository;
    private BookingRepository bookingRepository;
    private ShowRepository showRepository;
    private ShowSeatRepository showSeatRepository;

    @Autowired
    public BookingService(UserRepository userRepository, BookingRepository bookingRepository, ShowRepository showRepository, ShowSeatRepository showSeatRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking bookMovie(Long userId, Long showId, List<Long> showSeatIds) {
        // TODO: Finish the actual booking creation logic
        // Fetch user from userId and validate
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user Id"));
        // Fetch show from showId and validate
        Show show = showRepository.findById(showId).orElseThrow(() -> new IllegalArgumentException("Invalid show Id"));

        // ----------- Transaction starts here -----------
        List<ShowSeat> savedShowSeats = reserveSeats(showSeatIds);
        // --------- Transaction ends here ---------------------
        // Calculate the amount using priceCalculator strategy
        Double amount = 0.0;

        // Create and return the booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setShowSeats(savedShowSeats);
        booking.setAmount(amount);
        booking.setMovie(show.getMovie());
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);
        return null;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    List<ShowSeat> reserveSeats(List<Long> showSeatIds) {
        // Fetch all the showSeats from ShowSeatIds and validate
        List<ShowSeat> showSeats = showSeatRepository.findAllById(showSeatIds);
        // Check availability of show seats
        // If unavailable - simply throw an exception
        // Unavailable - Booked or (Blocked and currTime-blockedAtTime < waitDuration)
        // If available - you have to update the status to Blocked and update blockedAt time to current time
        // if Status is Available then :update the status to Blocked and update blockedAt time to current time
        // if status is Blocked and currTime-blockedAtTime > waitDuration : update blockedAt time to current time
        for (ShowSeat showSeat : showSeats) {
            Boolean isAvailable = showSeat.getStatus().equals(ShowSeatStatus.AVAILABLE);
            Boolean isExpiredBlock = showSeat.getStatus().equals(ShowSeatStatus.BLOCKED)
                    && Duration.between(showSeat.getBlockedDate().toInstant(), new Date().toInstant()).toMinutes() > 10;

            if (!(isAvailable || isExpiredBlock)) {
                throw new IllegalArgumentException("Seats are either booked or blocked, please try again in sometime");
            }
        }
        List<ShowSeat> savedShowSeats = new ArrayList<>();
        for (ShowSeat showSeat : showSeats) {
            // Update the status and time
            showSeat.setStatus(ShowSeatStatus.BLOCKED);
            showSeat.setBlockedDate(new Date());
            savedShowSeats.add(showSeat);
        }
        return showSeatRepository.saveAll(savedShowSeats);
    }
}