package com.topick.superapp.mhc.doctorAvailability.Repository;

import com.topick.superapp.mhc.model.DoctorAvailability;
import org.springframework.data.repository.query.Param; // ĐÚNG
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.rmi.server.UID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
@Repository
public interface DoctorAvailabilityRepository  extends JpaRepository<DoctorAvailability, UUID> {

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM doctor_availability WHERE doctor_id = :doctorId AND [date] = :date AND start_time = CAST(:startTime AS time)", nativeQuery = true)
    Integer existsByDoctorIdAndDateAndStartTime(
            @Param("doctorId") UUID doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") String startTime
    );
    List<DoctorAvailability> findAllByDoctorIdAndStatus(UUID doctorId, String status);
    List<DoctorAvailability> findAllByDoctorId(UUID doctorId);
    @Transactional
    @Modifying
    @Query(value = "UPDATE doctor_availability  SET status = 'PENDING' WHERE id = :avaiId AND status = 'AVAILABLE'", nativeQuery = true)
    Integer executeUpdate(@Param("avaiId") UUID availabilityId);

    List<DoctorAvailability> findByDoctorIdAndStatus(UUID id, String available);
    List<DoctorAvailability> findAllByDoctorIdAndDateIn(UUID doctorId, List<LocalDate> dates);
}
