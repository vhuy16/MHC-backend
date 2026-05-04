package com.topick.superapp.mhc.doctor.Service;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.doctor.Dto.AvailabilityResponse;
import com.topick.superapp.mhc.doctor.Dto.DoctorDetailResponse;
import com.topick.superapp.mhc.doctor.Dto.DoctorResponse;
import com.topick.superapp.mhc.doctor.Repository.DoctorRepository;
import com.topick.superapp.mhc.doctorAvailability.Repository.DoctorAvailabilityRepository;
import com.topick.superapp.mhc.enums.DoctorAvailabilityStatus;
import com.topick.superapp.mhc.exception.BusinessException;
import com.topick.superapp.mhc.model.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository  doctorRepository;
    private final DoctorAvailabilityRepository  doctorAvailabilityRepository;

    public ApiResponse findAllDoctor() {
        List<DoctorResponse> doctors = doctorRepository.findAll()
                .stream()
                .map(d -> DoctorResponse.builder()
                        .id(d.getId())
                        .fullName(d.getFullName())
                        .specialty(d.getSpecialty())
                        .bio(d.getBio())
                        .pricePerSession(d.getPricePerSession())
                        .rating(d.getRating())
                        .build())
                .toList();
        return new ApiResponse("Success", true, doctors);
    }
    public ApiResponse findDoctorById(UUID id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Doctor not found"));

        List<AvailabilityResponse> slots = doctorAvailabilityRepository
                .findByDoctorIdAndStatus(id, "AVAILABLE")
                .stream()
                .map(a -> AvailabilityResponse.builder()
                        .id(a.getId())
                        .date(a.getDate())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .status(a.getStatus())
                        .build())
                .toList();

        DoctorDetailResponse response = DoctorDetailResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .specialty(doctor.getSpecialty())
                .bio(doctor.getBio())
                .pricePerSession(doctor.getPricePerSession())
                .rating(doctor.getRating())
                .availabilities(slots)
                .build();

        return new ApiResponse("Success", true, response);
    }
    public ApiResponse searchDoctors(String q) {
        List<DoctorResponse> doctors = doctorRepository
                .findByFullNameContainingIgnoreCaseOrSpecialtyContainingIgnoreCase(q, q)
                .stream()
                .map(d -> DoctorResponse.builder()
                        .id(d.getId())
                        .fullName(d.getFullName())
                        .specialty(d.getSpecialty())
                        .bio(d.getBio())
                        .pricePerSession(d.getPricePerSession())
                        .rating(d.getRating())
                        .build())
                .toList();
        return new ApiResponse("Success", true, doctors);
    }
}
