package com.bookmyshow.Controllers;

import com.bookmyshow.Models.*;
import com.bookmyshow.Repositories.*;
import com.bookmyshow.Services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final RegionRepository regionRepository;
    private final TheatreRepository theatreRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final MovieRepository movieRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowSeatTypeRepository showSeatTypeRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    public CatalogController(
            RegionRepository regionRepository,
            TheatreRepository theatreRepository,
            ScreenRepository screenRepository,
            SeatRepository seatRepository,
            SeatTypeRepository seatTypeRepository,
            MovieRepository movieRepository,
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            ShowSeatTypeRepository showSeatTypeRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            UserService userService) {
        this.regionRepository = regionRepository;
        this.theatreRepository = theatreRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.seatTypeRepository = seatTypeRepository;
        this.movieRepository = movieRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.showSeatTypeRepository = showSeatTypeRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    @GetMapping("/regions")
    @Transactional(readOnly = true)
    public List<RegionView> getRegions() {
        return regionRepository.findAll().stream()
                .map(region -> new RegionView(region.getId(), region.getName()))
                .toList();
    }

    @PostMapping("/regions")
    @Transactional
    public RegionView createRegion(@RequestBody NameRequest request) {
        Region region = new Region();
        region.setName(requireName(request.name()));
        region = regionRepository.save(region);
        return new RegionView(region.getId(), region.getName());
    }

    @GetMapping("/theatres")
    @Transactional(readOnly = true)
    public List<TheatreView> getTheatres() {
        return theatreRepository.findAll().stream().map(this::toTheatreView).toList();
    }

    @PostMapping("/theatres")
    @Transactional
    public TheatreView createTheatre(@RequestBody TheatreRequest request) {
        Theatre theatre = new Theatre();
        theatre.setName(requireName(request.name()));
        theatre.setRegion(find(regionRepository, request.regionId(), "Region"));
        return toTheatreView(theatreRepository.save(theatre));
    }

    @GetMapping("/screens")
    @Transactional(readOnly = true)
    public List<ScreenView> getScreens() {
        return screenRepository.findAll().stream().map(this::toScreenView).toList();
    }

    @PostMapping("/screens")
    @Transactional
    public ScreenView createScreen(@RequestBody ScreenRequest request) {
        Screen screen = new Screen();
        screen.setName(requireName(request.name()));
        screen.setTheatre(find(theatreRepository, request.theatreId(), "Theatre"));
        screen.setFeatures(request.features() == null ? List.of() : request.features());
        return toScreenView(screenRepository.save(screen));
    }

    @GetMapping("/seat-types")
    @Transactional(readOnly = true)
    public List<SeatTypeView> getSeatTypes() {
        return seatTypeRepository.findAll().stream()
                .map(type -> new SeatTypeView(type.getId(), type.getType()))
                .toList();
    }

    @PostMapping("/seat-types")
    @Transactional
    public SeatTypeView createSeatType(@RequestBody NameRequest request) {
        SeatType seatType = new SeatType();
        seatType.setType(requireName(request.name()));
        seatType = seatTypeRepository.save(seatType);
        return new SeatTypeView(seatType.getId(), seatType.getType());
    }

    @GetMapping("/seats")
    @Transactional(readOnly = true)
    public List<SeatView> getSeats() {
        return seatRepository.findAll().stream().map(this::toSeatView).toList();
    }

    @PostMapping("/seats")
    @Transactional
    public SeatView createSeat(@RequestBody SeatRequest request) {
        Seat seat = new Seat();
        seat.setNumber(requireName(request.number()));
        seat.setRow(request.row());
        seat.setColumn(request.column());
        seat.setSeatType(find(seatTypeRepository, request.seatTypeId(), "Seat type"));
        seat.setScreen(find(screenRepository, request.screenId(), "Screen"));
        return toSeatView(seatRepository.save(seat));
    }

    @GetMapping("/movies")
    @Transactional(readOnly = true)
    public List<MovieView> getMovies() {
        return movieRepository.findAll().stream()
                .map(movie -> new MovieView(movie.getId(), movie.getName(), movie.getFeatures()))
                .toList();
    }

    @PostMapping("/movies")
    @Transactional
    public MovieView createMovie(@RequestBody MovieRequest request) {
        Movie movie = new Movie();
        movie.setName(requireName(request.name()));
        movie.setFeatures(request.features() == null ? List.of() : request.features());
        movie = movieRepository.save(movie);
        return new MovieView(movie.getId(), movie.getName(), movie.getFeatures());
    }

    @GetMapping("/shows")
    @Transactional(readOnly = true)
    public List<ShowView> getShows() {
        return showRepository.findAll().stream().map(this::toShowView).toList();
    }

    @PostMapping("/shows")
    @Transactional
    public ShowView createShow(@RequestBody ShowRequest request) {
        Show show = new Show();
        show.setMovie(find(movieRepository, request.movieId(), "Movie"));
        show.setScreen(find(screenRepository, request.screenId(), "Screen"));
        show.setStartTime(toDate(request.startTime(), "Start time"));
        show.setEndTime(toDate(request.endTime(), "End time"));
        if (!show.getEndTime().after(show.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        show.setFeatures(request.features() == null ? List.of() : request.features());
        return toShowView(showRepository.save(show));
    }

    @GetMapping("/show-seat-types")
    @Transactional(readOnly = true)
    public List<ShowSeatTypeView> getShowSeatTypes() {
        return showSeatTypeRepository.findAll().stream().map(this::toShowSeatTypeView).toList();
    }

    @PostMapping("/show-seat-types")
    @Transactional
    public ShowSeatTypeView createShowSeatType(@RequestBody ShowSeatTypeRequest request) {
        ShowSeatType showSeatType = new ShowSeatType();
        showSeatType.setShow(find(showRepository, request.showId(), "Show"));
        showSeatType.setSeatType(find(seatTypeRepository, request.seatTypeId(), "Seat type"));
        showSeatType.setPrice(requirePositive(request.price(), "Price"));
        return toShowSeatTypeView(showSeatTypeRepository.save(showSeatType));
    }

    @GetMapping("/show-seats")
    @Transactional(readOnly = true)
    public List<ShowSeatView> getShowSeats() {
        return showSeatRepository.findAll().stream().map(this::toShowSeatView).toList();
    }

    @PostMapping("/show-seats")
    @Transactional
    public ShowSeatView createShowSeat(@RequestBody ShowSeatRequest request) {
        ShowSeat showSeat = new ShowSeat();
        showSeat.setShow(find(showRepository, request.showId(), "Show"));
        showSeat.setSeat(find(seatRepository, request.seatId(), "Seat"));
        showSeat.setStatus(requireValue(request.status(), "Status"));
        showSeat.setBlockedDate(request.blockedDate() == null ? null : toDate(request.blockedDate(), "Blocked date"));
        return toShowSeatView(showSeatRepository.save(showSeat));
    }

    @PostMapping("/shows/{showId}/show-seats")
    @Transactional
    public GenerateShowSeatsResponse generateShowSeats(@PathVariable Long showId) {
        Show show = find(showRepository, showId, "Show");
        Screen screen = show.getScreen();
        if (screen == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Show does not have a screen");
        }

        List<Seat> seats = seatRepository.findByScreen_Id(screen.getId());
        if (seats.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected screen has no seats configured");
        }
        List<ShowSeat> existingShowSeats = showSeatRepository.findByShow_Id(show.getId());
        Set<Long> existingSeatIds = new HashSet<>();
        for (ShowSeat existing : existingShowSeats) {
            if (existing.getSeat() != null) {
                existingSeatIds.add(existing.getSeat().getId());
            }
        }

        List<ShowSeat> newShowSeats = seats.stream()
                .filter(seat -> !existingSeatIds.contains(seat.getId()))
                .map(seat -> {
                    ShowSeat showSeat = new ShowSeat();
                    showSeat.setShow(show);
                    showSeat.setSeat(seat);
                    showSeat.setStatus(ShowSeatStatus.AVAILABLE);
                    return showSeat;
                })
                .toList();
        showSeatRepository.saveAll(newShowSeats);
        return new GenerateShowSeatsResponse(show.getId(), seats.size(), newShowSeats.size());
    }

    @GetMapping("/users")
    @Transactional(readOnly = true)
    public List<UserView> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserView(user.getId(), user.getName(), user.getEmail()))
                .toList();
    }

    @PostMapping("/users")
    @Transactional
    public UserView createUser(@RequestBody UserRequest request) {
        String password = request.password();
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        String email = requireName(request.email());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A user with this email already exists");
        }
        User user = userService.signup(
                requireName(request.name()),
                email,
                password);
        return new UserView(user.getId(), user.getName(), user.getEmail());
    }

    @GetMapping("/bookings")
    @Transactional(readOnly = true)
    public List<BookingView> getBookings() {
        return bookingRepository.findAll().stream().map(this::toBookingView).toList();
    }

    @PostMapping("/bookings")
    @Transactional
    public BookingView createBooking(@RequestBody BookingRequest request) {
        Show show = find(showRepository, request.showId(), "Show");
        Booking booking = new Booking();
        booking.setUser(find(userRepository, request.userId(), "User"));
        booking.setShow(show);
        booking.setMovie(show.getMovie());
        booking.setStatus(requireValue(request.status(), "Status"));
        booking.setAmount(requirePositive(request.amount(), "Amount"));
        List<Long> seatIds = request.showSeatIds() == null ? List.of() : request.showSeatIds();
        booking.setShowSeats(showSeatRepository.findAllById(seatIds));
        if (booking.getShowSeats().size() != seatIds.size()
                || booking.getShowSeats().stream().anyMatch(showSeat -> !showSeat.getShow().getId().equals(show.getId()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more show seats were not found");
        }
        return toBookingView(bookingRepository.save(booking));
    }

    @GetMapping("/payments")
    @Transactional(readOnly = true)
    public List<PaymentView> getPayments() {
        return paymentRepository.findAll().stream().map(this::toPaymentView).toList();
    }

    @PostMapping("/payments")
    @Transactional
    public PaymentView createPayment(@RequestBody PaymentRequest request) {
        Payment payment = new Payment();
        payment.setReferenceNumber(requireName(request.referenceNumber()));
        payment.setAmount(requirePositive(request.amount(), "Amount"));
        payment.setMode(requireValue(request.mode(), "Mode"));
        payment.setStatus(requireValue(request.status(), "Status"));
        payment.setBooking(find(bookingRepository, request.bookingId(), "Booking"));
        return toPaymentView(paymentRepository.save(payment));
    }

    private TheatreView toTheatreView(Theatre theatre) {
        return new TheatreView(
                theatre.getId(), theatre.getName(),
                theatre.getRegion().getId(), theatre.getRegion().getName());
    }

    private ScreenView toScreenView(Screen screen) {
        return new ScreenView(
                screen.getId(), screen.getName(),
                screen.getTheatre().getId(), screen.getTheatre().getName(), screen.getFeatures());
    }

    private SeatView toSeatView(Seat seat) {
        return new SeatView(
                seat.getId(), seat.getNumber(), seat.getRow(), seat.getColumn(),
                seat.getSeatType().getId(), seat.getSeatType().getType(),
                seat.getScreen().getId(), seat.getScreen().getName());
    }

    private ShowView toShowView(Show show) {
        Movie movie = show.getMovie();
        Screen screen = show.getScreen();
        Theatre theatre = screen == null ? null : screen.getTheatre();
        Region region = theatre == null ? null : theatre.getRegion();
        return new ShowView(
                show.getId(), movie == null ? null : movie.getId(), movie == null ? null : movie.getName(),
                screen == null ? null : screen.getId(), screen == null ? null : screen.getName(),
                theatre == null ? null : theatre.getId(), theatre == null ? null : theatre.getName(),
                region == null ? null : region.getId(), region == null ? null : region.getName(),
                toLocalDateTime(show.getStartTime()), toLocalDateTime(show.getEndTime()), show.getFeatures());
    }

    private ShowSeatTypeView toShowSeatTypeView(ShowSeatType type) {
        return new ShowSeatTypeView(
                type.getId(), type.getShow().getId(), type.getSeatType().getId(),
                type.getSeatType().getType(), type.getPrice());
    }

    private ShowSeatView toShowSeatView(ShowSeat showSeat) {
        return new ShowSeatView(
                showSeat.getId(), showSeat.getShow().getId(), showSeat.getSeat().getId(),
                showSeat.getSeat().getNumber(), showSeat.getStatus(), toLocalDateTime(showSeat.getBlockedDate()));
    }

    private BookingView toBookingView(Booking booking) {
        return new BookingView(
                booking.getId(), booking.getUser().getId(), booking.getMovie().getId(),
                booking.getShow().getId(), booking.getStatus(), booking.getAmount(),
                booking.getShowSeats() == null ? List.of() : booking.getShowSeats().stream().map(ShowSeat::getId).toList());
    }

    private PaymentView toPaymentView(Payment payment) {
        return new PaymentView(
                payment.getId(), payment.getReferenceNumber(), payment.getAmount(),
                payment.getMode(), payment.getStatus(), payment.getBooking().getId());
    }

    private String requireName(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A non-empty value is required");
        }
        return value.trim();
    }

    private <T> T requireValue(T value, String field) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value;
    }

    private Double requirePositive(Double value, String field) {
        if (value == null || !Double.isFinite(value) || value <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must be greater than zero");
        }
        return value;
    }

    private Date toDate(LocalDateTime value, String field) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        return value == null ? null : value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private <T, ID> T find(org.springframework.data.jpa.repository.JpaRepository<T, ID> repository, ID id, String label) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + " is required");
        }
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, label + " not found"));
    }

    public record NameRequest(String name) {}
    public record TheatreRequest(String name, Long regionId) {}
    public record ScreenRequest(String name, Long theatreId, List<Feature> features) {}
    public record SeatRequest(String number, Integer row, Integer column, Long seatTypeId, Long screenId) {}
    public record MovieRequest(String name, List<Feature> features) {}
    public record ShowRequest(Long movieId, Long screenId, LocalDateTime startTime, LocalDateTime endTime, List<Feature> features) {}
    public record ShowSeatTypeRequest(Long showId, Long seatTypeId, Double price) {}
    public record ShowSeatRequest(Long showId, Long seatId, ShowSeatStatus status, LocalDateTime blockedDate) {}
    public record GenerateShowSeatsResponse(Long showId, int totalSeats, int createdSeats) {}
    public record UserRequest(String name, String email, String password) {}
    public record BookingRequest(Long userId, Long showId, List<Long> showSeatIds, BookingStatus status, Double amount) {}
    public record PaymentRequest(String referenceNumber, Double amount, PaymentMode mode, PaymentStatus status, Long bookingId) {}
    public record RegionView(Long id, String name) {}
    public record TheatreView(Long id, String name, Long regionId, String regionName) {}
    public record ScreenView(Long id, String name, Long theatreId, String theatreName, List<Feature> features) {}
    public record SeatTypeView(Long id, String type) {}
    public record SeatView(Long id, String number, Integer row, Integer column, Long seatTypeId, String seatType, Long screenId, String screen) {}
    public record MovieView(Long id, String name, List<Feature> features) {}
    public record ShowView(
            Long id,
            Long movieId,
            String movie,
            Long screenId,
            String screen,
            Long theatreId,
            String theatre,
            Long regionId,
            String region,
            LocalDateTime startTime,
            LocalDateTime endTime,
            List<Feature> features) {}
    public record ShowSeatTypeView(Long id, Long showId, Long seatTypeId, String seatType, Double price) {}
    public record ShowSeatView(Long id, Long showId, Long seatId, String seat, ShowSeatStatus status, LocalDateTime blockedDate) {}
    public record UserView(Long id, String name, String email) {}
    public record BookingView(Long id, Long userId, Long movieId, Long showId, BookingStatus status, Double amount, List<Long> showSeatIds) {}
    public record PaymentView(Long id, String referenceNumber, Double amount, PaymentMode mode, PaymentStatus status, Long bookingId) {}
}
