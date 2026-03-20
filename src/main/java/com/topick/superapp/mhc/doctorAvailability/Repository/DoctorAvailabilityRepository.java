package com.topick.superapp.mhc.doctorAvailability.Repository;

import com.topick.superapp.mhc.model.DoctorAvailability;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.rmi.server.UID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface DoctorAvailabilityRepository  extends JpaRepository<DoctorAvailability, UUID> {

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM doctor_availability WHERE doctor_id = :doctorId AND [date] = :date AND start_time = CAST(:startTime AS time)", nativeQuery = true)
    Integer existsByDoctorIdAndDateAndStartTime(
            @Param("doctorId") UUID doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") String startTime
    );
    List<DoctorAvailability> findAllByDoctorIdAndStatus(UUID doctorId, String status);
    List<DoctorAvailability> findAllByDoctorId(UUID doctorId);
}
