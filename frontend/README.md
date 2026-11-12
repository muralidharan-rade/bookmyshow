# BMS Studio

The venue setup UI is a standalone static frontend in this directory; it is not served or bundled by the Spring backend.

1. Start the Spring Boot application and PostgreSQL database. The API is available at `http://localhost:8080/api`.
2. From this directory, run `python -m http.server 5173`.
3. Open `http://localhost:5173`. No UI sign-in is required.

The setup UI can create all persisted project entities: regions, theatres, screens, seat types, seats, movies, shows, show seat pricing, show seats, users, bookings, and payments. To create show seats, choose **Generate show seats** and select a show; the API creates available show seats for every seat on that show's screen and skips seats already created for the show. The API permits requests from local loopback frontend origins; override `app.frontend-origin` if the frontend is served from a different host.

The setup API is unauthenticated for local development. Do not expose it to an untrusted network.
