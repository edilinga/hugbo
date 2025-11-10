package com.team.gym.controller;

import com.team.gym.dto.BookingResponse;
import com.team.gym.service.BookingService;
import com.team.gym.errors.Unauthorized;
import com.team.gym.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping
public class BookingController {
    private final BookingRepository bookings;

    private final BookingService bookingService;

    /**
     * Smíðar nýjan {@code BookingController} með skilgreint repo og service.
     *
     * @param bookings hlekkur {@link BookingRepository} notaður til að fá aðang að og vinna með gögn sem varða bókanir
     * @param bookingService hlekkur {@link BookingService} notaður til að vinna business lógík sem varðar bókanir
     */
    public BookingController(BookingRepository bookings, BookingService bookingService) {
        this.bookings = bookings;
        this.bookingService = bookingService;
    }

    /**
     * UC1 - bóka tíma
     *
     * @param classId auðkenni tímans sem á að bóka
     * @param session hlekkur {@link HttpSession} sem táknar núverandi session
     * @return hlekkur {@link BookingResponse} sem inniheldur upplýsingar um stöðu bókunar
     */
    @PostMapping("/bookings/{classId}")
    public BookingResponse book(@PathVariable Long classId, HttpSession session) {
        return bookingService.book(classId, session);
    }

    /**
     *  UC2 — afbóka tíma
     *
     * @param classId auðkenni tímans sem á að bóka
     * @param session hlekkur {@link HttpSession} sem táknar núverandi session
     * @return hlekkur {@link ResponseEntity} með HTTP status 204 (No Content) ef afbókun tókst
     * @throws Unauthorized ef notandi er ekki innskráður eða session inniheldur ekki gilt auðkenni notanda
     */
    @DeleteMapping("/bookings/{classId}")
    public ResponseEntity<Void> cancel(@PathVariable Long classId, HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized();
        bookingService.cancel(uid, classId);
        return ResponseEntity.noContent().build();
    }

    /**
     * //UC5: mínar bókanir
     *
     * @param session hlekkur {@link HttpSession} sem táknar núverandi session
     * @return listi af {@link BookingResponse} hlutum sem tákna bókanir notanda,
     *         raðað eftir upphafstíma bókaðs tíma í vaxandi röð
     * @throws Unauthorized ef notandi er ekki innskráður eða session inniheldur ekki gilt auðkenni notanda
     */
    @GetMapping("/minar-bokanir")
    public List<BookingResponse> myBookings(HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) throw new Unauthorized();

        return bookings.findByUserIdOrderByClassSessionStartAtAsc(uid).stream()
        .map(b -> new BookingResponse(
            b.getId(),
            b.getClassSession().getId(),
            b.getClassSession().getType(),
            b.getClassSession().getStartAt(),
            b.getClassSession().getEndAt()
        )).toList();
    }

    /**
     * UC3 - Skrá á biðlista
     *
     * @param classId auðkenni tímans sem notandi vill skrá sig á biðlista fyrir
     * @param session hlekkur {@link HttpSession} sem táknar núverandi session
     * @return hlekkur {@link BookingResponse} sem inniheldur upplýsingar um inngöngu á biðlista
     * @throws Unauthorized ef notandi er ekki innskráður eða session inniheldur ekki gilt auðkenni notanda
     */
    @PostMapping("/bookings/{classId}/waitlist")
    public BookingResponse joinWaitlist(@PathVariable Long classId, HttpSession session) {
        return bookingService.joinWaitlist(classId, session);
    }


}
