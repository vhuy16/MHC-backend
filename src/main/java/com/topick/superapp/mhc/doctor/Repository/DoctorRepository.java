package com.topick.superapp.mhc.doctor.Repository;

import com.topick.superapp.mhc.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    List<Doctor> findByFullNameContainingIgnoreCaseOrSpecialtyContainingIgnoreCase(
            String fullName, String specialty);
}
