package com.kartingrm.Repositories;

import com.kartingrm.Entities.BookingEntity;
import com.kartingrm.Entities.ClientEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {


    @Query("SELECT b FROM BookingEntity b WHERE MONTH(b.bookingDate) = :month")
    List<BookingEntity> findByMonth(@Param("month") int month);

    @Query("SELECT b FROM BookingEntity b WHERE DAY(b.bookingDate) = :day")
    List<BookingEntity> findByDay(@Param("day") int day);

    @Query("SELECT b FROM BookingEntity b WHERE YEAR(b.bookingDate) = :year")
    List<BookingEntity> findByYear(@Param("year") int year);

    @Query("SELECT b FROM BookingEntity b WHERE CAST(b.bookingDate AS DATE) BETWEEN :startDate AND :endDate")
    List<BookingEntity> findBookingsBetweenDates(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM BookingEntity b WHERE CAST(b.bookingDate AS date) = :date")
    List<BookingEntity> findByDate(LocalDate date);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM booking_client WHERE booking_id = :bookingId", nativeQuery = true)
    void deleteBookingClientLinks(@Param("bookingId") Long bookingId);

    @Query("SELECT COUNT(DISTINCT b) FROM BookingEntity b JOIN b.participants p WHERE (b.client.id = :clientId OR p.id = :clientId) AND EXTRACT(MONTH FROM b.bookingDate) = :month AND EXTRACT(YEAR FROM b.bookingDate) = :year")
    long countVisitsInMonthAndYear(@Param("clientId") Long clientId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT b FROM BookingEntity b WHERE b.numberLap = :numberLap AND MONTH(b.bookingDate) = :month AND YEAR(b.bookingDate) = :year")
    List<BookingEntity> findBookingLapsByMY(@Param("numberLap") int numberLap, @Param("month") int month, @Param("year") int year);

}


