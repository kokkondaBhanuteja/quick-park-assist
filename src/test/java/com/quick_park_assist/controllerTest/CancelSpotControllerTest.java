package com.quick_park_assist.controllerTest;

import com.quick_park_assist.controller.CancelSpotController;
import com.quick_park_assist.entity.BookingSpot;
import com.quick_park_assist.service.ICancelSpotService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CancelSpotControllerTest {

    @InjectMocks
    private CancelSpotController cancelSpotController;

    @Mock
    private ICancelSpotService cancelSpotService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testShowCancelForm_UserNotLoggedIn() {
        when(session.getAttribute("userId")).thenReturn(null);

        String viewName = cancelSpotController.showCancelForm(session, model);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    public void testShowCancelForm_NoConfirmedBookings() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(cancelSpotService.getConfirmedBookingsByUserID(1L)).thenReturn(new ArrayList<>());

        String viewName = cancelSpotController.showCancelForm(session, model);

        verify(model).addAttribute(eq("bookings"), anyList());
        verify(model).addAttribute(eq("cancelSpot"), any(BookingSpot.class));
        assertEquals("CancelBooking", viewName);
    }

    @Test
    public void testShowCancelForm_WithConfirmedBookings() {
        when(session.getAttribute("userId")).thenReturn(1L);
        List<BookingSpot> confirmedBookings = new ArrayList<>();
        BookingSpot booking = new BookingSpot();
        booking.setBookingId(1L);
        confirmedBookings.add(booking);
        when(cancelSpotService.getConfirmedBookingsByUserID(1L)).thenReturn(confirmedBookings);

        String viewName = cancelSpotController.showCancelForm(session, model);

        verify(model).addAttribute(eq("bookings"), eq(confirmedBookings));
        verify(model).addAttribute(eq("cancelSpot"), any(BookingSpot.class));
        assertEquals("CancelBooking", viewName);
    }

    @Test
    public void testShowCancelForm_ConfirmedBookingsNull() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(cancelSpotService.getConfirmedBookingsByUserID(1L)).thenReturn(null);

        String viewName = cancelSpotController.showCancelForm(session, model);

        verify(model).addAttribute(eq("bookings"), isNull());
        verify(model).addAttribute(eq("cancelSpot"), any(BookingSpot.class));
        assertEquals("CancelBooking", viewName);
    }

    @Test
    public void testCancelSelectedBooking_ValidBookingId() {
        Long bookingId = 1L;
        when(redirectAttributes.addFlashAttribute(anyString(), anyString())).thenReturn(redirectAttributes);
        when(cancelSpotService.cancelBooking(bookingId)).thenReturn(true);

        String viewName = cancelSpotController.cancelSelectedBooking(bookingId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("successMessage", "Booking successfully Cancelled!");
        assertEquals("redirect:/cancelSpot/", viewName);
    }

    @Test
    public void testCancelSelectedBooking_InvalidBookingId() {
        Long bookingId = null;

        String viewName = cancelSpotController.cancelSelectedBooking(bookingId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("errorMessage", "Invalid Booking ID.");
        assertEquals("redirect:/cancelSpot/", viewName);
    }

    @Test
    public void testCancelSelectedBooking_CancellationFailed() {
        Long bookingId = 1L;
        when(cancelSpotService.cancelBooking(bookingId)).thenReturn(false);

        String viewName = cancelSpotController.cancelSelectedBooking(bookingId, redirectAttributes);

        verify(redirectAttributes).addFlashAttribute("errorMessage", "Cancellation failed.");
        assertEquals("redirect:/cancelSpot/", viewName);
    }
}
