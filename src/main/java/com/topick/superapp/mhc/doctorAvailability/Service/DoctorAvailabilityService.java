package com.topick.superapp.mhc.doctorAvailability.Service;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.auth.repository.DoctoRepository;
import com.topick.superapp.mhc.doctorAvailability.Dto.CreateSlotRequest;
import com.topick.superapp.mhc.doctorAvailability.Dto.SlotResponse;
import com.topick.superapp.mhc.doctorAvailability.Repository.DoctorAvailabilityRepository;
import com.topick.superapp.mhc.enums.DoctorAvailabilityStatus;
import com.topick.superapp.mhc.model.Doctor;
import com.topick.superapp.mhc.model.DoctorAvailability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DoctorAvailabilityService {
    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;
    @Autowired
    private DoctoRepository  doctoRepository;


    public ApiResponse CreateSlot(CreateSlotRequest createSlotRequest){
        UUID doctorId = (UUID) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        Doctor doctor = doctoRepository.getReferenceById(doctorId);
        var slot = doctorAvailabilityRepository.existsByDoctorIdAndDateAndStartTime(
                doctorId,
                createSlotRequest.getDate(),
                createSlotRequest.getStartTime().toString()
        );
        if(slot > 0){
          return  new ApiResponse("Slot này đã tồn tại", false, null);
        }
        DoctorAvailability doctorAvailability = DoctorAvailability.builder()
                .date(createSlotRequest.getDate())
                .startTime(createSlotRequest.getStartTime())
                .endTime(createSlotRequest.getEndTime())
                .status(DoctorAvailabilityStatus.AVAILABLE.toString())
                .doctor(doctor)
                .build();
    doctorAvailabilityRepository.save(doctorAvailability);
    return new ApiResponse( "Tạo doctorAvailability thành công", true, new SlotResponse(
            doctorAvailability.getId(),
            doctorAvailability.getDoctor().getId(),
            doctorAvailability.getDate(),
            doctorAvailability.getStartTime(),
            doctorAvailability.getEndTime(),
            doctorAvailability.getStatus()
    ));
    }

    public ApiResponse getSlots(String status) {
        UUID doctorId = (UUID) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        List<DoctorAvailability> listSlots;

        // Nếu status không được truyền vào (null hoặc rỗng)
        if (status == null || status.trim().isEmpty()) {
            listSlots = doctorAvailabilityRepository.findAllByDoctorId(doctorId);
        } else {
            // Nếu có status thì mới lọc theo status
            listSlots = doctorAvailabilityRepository.findAllByDoctorIdAndStatus(doctorId, status);
        }

        if (listSlots.isEmpty()) {
            return new ApiResponse("Không có slot nào cả", true, null);
        }
        return new ApiResponse("Lấy danh sách slot thành công", true, listSlots);
    }

    public ApiResponse deleteSlot(UUID slotId){
        Optional<DoctorAvailability> doctorAvailability = doctorAvailabilityRepository.findById(slotId);;
        if(doctorAvailability.isEmpty()){
            return new ApiResponse("Không tìm thấy slot", false, null);
        }
        if(doctorAvailability.get().getStatus().equals(DoctorAvailabilityStatus.BOOKED.toString()) ||
                doctorAvailability.get().getStatus().equals(DoctorAvailabilityStatus.PENDING.toString())){
            return new ApiResponse("không thể xoá slot này", false, null);
        }
        doctorAvailabilityRepository.delete(doctorAvailability.get());
        return new ApiResponse("xoá thành công slot", true, null);
    }
}
