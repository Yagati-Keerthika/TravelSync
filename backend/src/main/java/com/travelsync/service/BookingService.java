package com.travelsync.service;

import com.travelsync.engine.RabinKarp;
import com.travelsync.model.Booking;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final List<Booking> bookings = new ArrayList<>();

    public BookingService() {
        // Initialize sample booking records for the application booking system
        bookings.add(new Booking("TS-8841-GOA", "Aarav Sharma", "Goa", LocalDate.now().plusDays(14), "CONFIRMED", 450.0));
        bookings.add(new Booking("TS-9124-PARIS", "Elena Rostova", "Paris", LocalDate.now().plusDays(30), "CONFIRMED", 1250.0));
        bookings.add(new Booking("TS-7320-MANALI", "Vikram Patel", "Manali", LocalDate.now().plusDays(7), "CONFIRMED", 320.0));
        bookings.add(new Booking("TS-4412-TOKYO", "Kenji Sato", "Tokyo", LocalDate.now().plusDays(45), "PENDING", 1680.0));
        bookings.add(new Booking("TS-6590-MUMBAI", "Priya Nair", "Mumbai", LocalDate.now().plusDays(3), "CONFIRMED", 290.0));
        bookings.add(new Booking("TS-3210-JAIPUR", "Rahul Verma", "Jaipur", LocalDate.now().plusDays(21), "CONFIRMED", 380.0));
    }

    public record BookingSearchResult(
            String queryCode,
            List<Booking> matchingBookings,
            String algorithm,
            long executionTimeNanos
    ) {}

    public BookingSearchResult searchByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return new BookingSearchResult(code, List.of(), "Rabin-Karp Rolling Hash", 0);
        }

        long start = System.nanoTime();
        String query = code.trim().toUpperCase();
        List<Booking> matched = new ArrayList<>();

        for (Booking b : bookings) {
            String target = b.getBookingCode().toUpperCase();
            // Use Rabin-Karp to find substring occurrence of query inside booking code
            List<Integer> occurrences = RabinKarp.search(target, query);
            if (!occurrences.isEmpty()) {
                matched.add(b);
            }
        }

        long duration = System.nanoTime() - start;
        return new BookingSearchResult(code, matched, "Rabin-Karp Rolling Hash", duration);
    }

    public List<Booking> getAllBookings() {
        return List.copyOf(bookings);
    }
}
