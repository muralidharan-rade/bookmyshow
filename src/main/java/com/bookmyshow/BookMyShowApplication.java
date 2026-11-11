package com.bookmyshow;

import com.bookmyshow.Models.Region;
import com.bookmyshow.Models.Screen;
import com.bookmyshow.Models.Theatre;
import com.bookmyshow.Models.User;
import com.bookmyshow.Repositories.RegionRepository;
import com.bookmyshow.Repositories.ScreenRepository;
import com.bookmyshow.Repositories.TheatreRepository;
import com.bookmyshow.Repositories.UserRepository;
import com.bookmyshow.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@SpringBootApplication
public class BookMyShowApplication implements CommandLineRunner {

    @Autowired // Springboot will find an object from bean registry(singleton registry) and populate here
    RegionRepository regionRepository;
    @Autowired
    TheatreRepository theatreRepository;
    @Autowired
    ScreenRepository screenRepository;
    @Autowired
    UserService userService;
    @Autowired
    private UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(BookMyShowApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Region region = new Region();
        region.setName("Mumbai");
        regionRepository.save(region);

        Region region2 = new Region();
        region2.setName("Chennai");
        regionRepository.save(region2);

        System.out.println("Regions in DB: " + regionRepository.findAll().size());

        Theatre theatre = new Theatre();
        theatre.setName("Phoenix");
        theatre.setRegion(region);

        Theatre theatre2 = new Theatre();
        theatre2.setName("BKC_INOX");
        theatre2.setRegion(region);

        Theatre theatre3 = new Theatre();
        theatre3.setName("POWAI_CINEMAX");
        theatre3.setRegion(region);

        Theatre theatre4 = new Theatre();
        theatre4.setName("ANDHERI_PVR");
        theatre4.setRegion(region2);

        theatreRepository.save(theatre);
        theatreRepository.save(theatre2);
        theatreRepository.save(theatre3);
        theatreRepository.save(theatre4);


        Screen screen1A = new Screen();
        screen1A.setName("1A");
        screen1A.setTheatre(theatre);
        screenRepository.save(screen1A);
        Screen screen2A = new Screen();
        screen2A.setName("2A");
        screen2A.setTheatre(theatre);
        screenRepository.save(screen2A);
        Screen screen1B = new Screen();
        screen1B.setName("1B");
        screen1B.setTheatre(theatre);
        screenRepository.save(screen1B);
        Screen screen2B = new Screen();
        screen2B.setName("2B");
        screen2B.setTheatre(theatre);
        screenRepository.save(screen2B);


        List<Screen> screens = screenRepository.findAll();
        for (Screen screen : screens) {
            System.out.println(screen.getName());
        }

        Screen screen = screenRepository.findById(1L).orElse(null);
        System.out.println(screen.getName());

        Theatre searchTheatre = theatreRepository.findByName("Phoenix");
        System.out.println(searchTheatre.getName());
        //Screens -> Theatre -> Name = "Phoenix"

        List<Screen> screenList = screenRepository.findByTheatre_Name("Phoenix");
        for (Screen screen1 : screenList) {
            System.out.println(screen1.getName());
        }

        //Movies -> Screen > Theatre -> City -> Name is Mumbai

        User user1 = userService.signup("Akash", "akash1@gmail.com", "passpass");
        User user2 = userService.signup("Akash", "akash2@gmail.com", "passpass");
        User user3 = userService.signup("Sam", "sam@gmail.com", "passpass123");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.matches("passpass", "$2a$10$UdgI7d9nSqtPdFAz0spAeO/bMUPQe3ztaJvmGCX0dGyPS7HH4H8ku"));

    }
    // AND (Helps you to add filters of properties on the model), UNDERSCORE(_) - Helps you to add filters of nested properties
}

