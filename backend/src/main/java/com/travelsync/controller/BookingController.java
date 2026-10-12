package com.travelsync.controller;

import com.travelsync.dto.ApiResponse;
import com.travelsync.model.Booking;
import com.travelsync.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<BookingService.BookingSearchResult>> searchBookings(
            @RequestParam("code") String code) {
        BookingService.BookingSearchResult result = bookingService.searchByCode(code);

        String msg = result.matchingBookings().isEmpty()
                ? "No bookings found matching code: " + code
                : "Found " + result.matchingBookings().size() + " matching booking(s)";

        return ResponseEntity.ok(ApiResponse.ok(msg, result)
                .withMetadata("algorithm", result.algorithm())
                .withMetadata("executionNanos", result.executionTimeNanos())
                .withMetadata("complexity", "Average O(N + M), Worst O(N * M)"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.ok("Bookings retrieved", bookingService.getAllBookings()));
    }
}
