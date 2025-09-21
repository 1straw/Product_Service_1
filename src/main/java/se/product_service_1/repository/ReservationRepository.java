package se.product_service_1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.product_service_1.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByProductId(Long productId);
    List<Reservation> findByOrderId(Long orderId);
    List<Reservation> findByStatusAndReservedAtBefore(Reservation.ReservationStatus status, LocalDateTime time);
}