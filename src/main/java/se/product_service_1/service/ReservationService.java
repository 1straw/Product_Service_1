package se.product_service_1.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.product_service_1.dto.ProductReservation;
import se.product_service_1.dto.ReservationRequest;
import se.product_service_1.dto.ReservationResponse;
import se.product_service_1.exception.NotEnoughStockException;
import se.product_service_1.model.Product;
import se.product_service_1.model.Reservation;
import se.product_service_1.repository.ProductRepository;
import se.product_service_1.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional
    public List<ReservationResponse> reserveProducts(ReservationRequest request) {
        log.info("Reserving products for orderId={}", request.getOrderId());
        List<ReservationResponse> responses = new ArrayList<>();

        for (ProductReservation productReservation : request.getProductReservations()) {
            Product product = productService.getProductById(productReservation.getProductId());

            // Kontrollera om det finns tillräckligt med lager
            if (product.getStockQuantity() < productReservation.getQuantity()) {
                throw new NotEnoughStockException("Inte tillräckligt med lager för produkt: " + product.getName() +
                        ". Tillgängligt: " + product.getStockQuantity() +
                        ", Begärt: " + productReservation.getQuantity());
            }

            // Reservera lager
            product.setStockQuantity(product.getStockQuantity() - productReservation.getQuantity());
            productRepository.save(product);

            // Skapa reservation
            Reservation reservation = Reservation.builder()
                    .productId(productReservation.getProductId())
                    .orderId(request.getOrderId())
                    .quantity(productReservation.getQuantity())
                    .reservedAt(LocalDateTime.now())
                    .confirmed(false)
                    .status(Reservation.ReservationStatus.PENDING)
                    .build();

            Reservation savedReservation = reservationRepository.save(reservation);

            responses.add(ReservationResponse.builder()
                    .reservationId(savedReservation.getId())
                    .productId(savedReservation.getProductId())
                    .quantity(savedReservation.getQuantity())
                    .status(savedReservation.getStatus().toString())
                    .build());
        }

        return responses;
    }

    @Transactional
    public void confirmReservations(Long orderId) {
        log.info("Confirming reservations for orderId={}", orderId);
        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);

        for (Reservation reservation : reservations) {
            reservation.setConfirmed(true);
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
        }
    }

    @Transactional
    public void cancelReservations(Long orderId) {
        log.info("Cancelling reservations for orderId={}", orderId);
        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);

        for (Reservation reservation : reservations) {
            // Återställ lager
            Product product = productService.getProductById(reservation.getProductId());
            product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
            productRepository.save(product);

            // Uppdatera reservation status
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
        }
    }

    @Transactional
    public void cancelProductReservation(Long orderId, Long productId, Integer quantity) {
        log.info("Cancelling product reservation for orderId={}, productId={}, quantity={}",
                orderId, productId, quantity);

        List<Reservation> reservations = reservationRepository.findByOrderId(orderId);

        for (Reservation reservation : reservations) {
            if (reservation.getProductId().equals(productId)) {
                // Om vi vill avbryta exakt hela reservationen eller mer
                if (reservation.getQuantity() <= quantity) {
                    // Återställ hela kvantiteten
                    Product product = productService.getProductById(reservation.getProductId());
                    product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
                    productRepository.save(product);

                    // Uppdatera reservation status
                    reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation);

                    log.info("Cancelled entire reservation: productId={}, quantity={}, orderId={}",
                            reservation.getProductId(), reservation.getQuantity(), reservation.getOrderId());
                }
                // Om vi bara vill avbryta en del av reservationen
                else {
                    // Återställ bara den specificerade kvantiteten
                    Product product = productService.getProductById(reservation.getProductId());
                    product.setStockQuantity(product.getStockQuantity() + quantity);
                    productRepository.save(product);

                    // Uppdatera reservation kvantitet
                    int originalQuantity = reservation.getQuantity();
                    reservation.setQuantity(reservation.getQuantity() - quantity);
                    reservationRepository.save(reservation);

                    log.info("Partially cancelled reservation: productId={}, original quantity={}, cancelled quantity={}, remaining quantity={}, orderId={}",
                            reservation.getProductId(), originalQuantity, quantity, reservation.getQuantity(), reservation.getOrderId());
                }
                break;
            }
        }
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // Kör varje timme
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Cleaning up expired reservations");
        // Hitta reservationer äldre än 1 timme och som fortfarande är i PENDING status
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndReservedAtBefore(
                Reservation.ReservationStatus.PENDING, cutoffTime);

        for (Reservation reservation : expiredReservations) {
            // Återställ lager
            Product product = productService.getProductById(reservation.getProductId());
            product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
            productRepository.save(product);

            // Uppdatera reservation status
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);

            log.info("Expired reservation released: productId={}, quantity={}, orderId={}",
                    reservation.getProductId(), reservation.getQuantity(), reservation.getOrderId());
        }
    }
}