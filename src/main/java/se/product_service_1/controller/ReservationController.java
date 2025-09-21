package se.product_service_1.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.product_service_1.dto.ReservationRequest;
import se.product_service_1.dto.ReservationResponse;
import se.product_service_1.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Reserve products", description = "Reserve products for an order")
    @PostMapping
    public ResponseEntity<List<ReservationResponse>> reserveProducts(@RequestBody ReservationRequest request) {
        List<ReservationResponse> responses = reservationService.reserveProducts(request);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Confirm reservations", description = "Confirm reservations for an order")
    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<String> confirmReservations(@PathVariable Long orderId) {
        reservationService.confirmReservations(orderId);
        return ResponseEntity.ok("Reservations confirmed for order: " + orderId);
    }

    @Operation(summary = "Cancel reservations", description = "Cancel reservations for an order")
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelReservations(@PathVariable Long orderId) {
        reservationService.cancelReservations(orderId);
        return ResponseEntity.ok("Reservations cancelled for order: " + orderId);
    }

    @Operation(summary = "Cancel product reservation", description = "Cancel a specific product reservation for an order")
    @PostMapping("/cancel/{orderId}/product/{productId}/quantity/{quantity}")
    public ResponseEntity<String> cancelProductReservation(
            @PathVariable Long orderId,
            @PathVariable Long productId,
            @PathVariable Integer quantity) {
        reservationService.cancelProductReservation(orderId, productId, quantity);
        return ResponseEntity.ok("Product reservation cancelled: orderId=" + orderId +
                ", productId=" + productId +
                ", quantity=" + quantity);
    }
}