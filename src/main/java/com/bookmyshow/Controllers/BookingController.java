package com.bookmyshow.Controllers;

import com.bookmyshow.DTO.BookMovieRequestDTO;
import com.bookmyshow.DTO.BookMovieResponseDTO;
import com.bookmyshow.DTO.ResponseStatus;
import com.bookmyshow.Models.Booking;
import com.bookmyshow.Services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/booking")
public class BookingController {

    private BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/book")
    public BookMovieResponseDTO bookMovie(@RequestBody BookMovieRequestDTO requestDTO) {
        BookMovieResponseDTO responseDTO = new BookMovieResponseDTO();
        try {
            Booking booking = bookingService.bookMovie(
                    requestDTO.getUserId(),
                    requestDTO.getShowId(),
                    requestDTO.getShowSeatIds());
            responseDTO.setBookingId(booking.getId());
            responseDTO.setBookingStatus(booking.getStatus());
            responseDTO.setStatus(ResponseStatus.SUCCESS);
            responseDTO.setAmount(booking.getAmount());
            responseDTO.setResponseMessage("Booking Successful, please proceed to payment for confirming your booking");
        } catch (Exception e) {
            responseDTO.setStatus(ResponseStatus.FAILURE);
            responseDTO.setResponseMessage("Booking unsuccessful due to - " + e.getMessage());
        }
        return responseDTO;
    }
}

// bookingMovie -> Create a booking(UNPAID) --bookingId,amount--> User [Order detail Page] ----bookingId, amount----> Proceeds to paymentGateway ---Return a success/failure event(bookingId) back to bms server