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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorAvailabilityService {
    @Autowired
    private DoctorAvailabilityRepository doctorAvailabilityRepository;
    @Autowired
    private DoctoRepository  doctoRepository;


    private UUID getLoggedInDoctorId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public ApiResponse bulkCreateSlots(CreateSlotRequest request) {
        UUID doctorId = getLoggedInDoctorId();
        Doctor doctor = doctoRepository.getReferenceById(doctorId);
        LocalDate today = LocalDate.now();

        // 1. Validate dates
        for (LocalDate date : request.getDates()) {
            if (!date.isAfter(today)) {
                return new ApiResponse("Chỉ có thể tạo slot cho các ngày trong tương lai: " + date, false, null);
            }
        }

        // 2. Generate slots liên tục (không có break time)
        List<DoctorAvailability> newSlots = new ArrayList<>();

        for (LocalDate date : request.getDates()) {
            LocalTime currentTime = request.getStartOfDay();

            while (currentTime.plusMinutes(request.getSlotDurationMinutes()).isBefore(request.getEndOfDay()) ||
                    currentTime.plusMinutes(request.getSlotDurationMinutes()).equals(request.getEndOfDay())) {

                LocalTime slotEndTime = currentTime.plusMinutes(request.getSlotDurationMinutes());

                DoctorAvailability newSlot = DoctorAvailability.builder()
                        .date(date)
                        .startTime(currentTime)
                        .endTime(slotEndTime)
                        .status(DoctorAvailabilityStatus.AVAILABLE.toString())
                        .doctor(doctor)
                        .build();

                newSlots.add(newSlot);
                // Slot tiếp theo bắt đầu ngay khi slot trước kết thúc
                currentTime = slotEndTime;
            }
        }

        // 3. Kiểm tra overlap (Trùng lặp)
        List<DoctorAvailability> existingSlots = doctorAvailabilityRepository.findAllByDoctorIdAndDateIn(doctorId, request.getDates());
        List<DoctorAvailability> conflictSlots = new ArrayList<>();

        for (DoctorAvailability newSlot : newSlots) {
            boolean isConflict = existingSlots.stream().anyMatch(existing ->
                    existing.getDate().equals(newSlot.getDate()) &&
                            existing.getStartTime().isBefore(newSlot.getEndTime()) &&
                            existing.getEndTime().isAfter(newSlot.getStartTime())
            );

            if (isConflict) {
                conflictSlots.add(newSlot);
            }
        }

        if (!conflictSlots.isEmpty()) {
            List<SlotResponse> conflicts = conflictSlots.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            return new ApiResponse("Phát hiện trùng lặp thời gian với các slot đã tồn tại", false, conflicts);
        }

        // 4. Lưu toàn bộ
        doctorAvailabilityRepository.saveAll(newSlots);

        return new ApiResponse("Tạo hàng loạt slot thành công", true,
                newSlots.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    public ApiResponse getSlots(String status) {
        UUID doctorId = getLoggedInDoctorId();
        List<DoctorAvailability> listSlots;

        if (status == null || status.trim().isEmpty()) {
            listSlots = doctorAvailabilityRepository.findAllByDoctorId(doctorId);
        } else {
            listSlots = doctorAvailabilityRepository.findAllByDoctorIdAndStatus(doctorId, status);
        }

        if (listSlots.isEmpty()) {
            return new ApiResponse("Không có slot nào cả", true, null);
        }

        return new ApiResponse("Lấy danh sách slot thành công", true,
                listSlots.stream().map(this::mapToResponse).collect(Collectors.toList()));
    }

    public ApiResponse deleteSlot(UUID slotId) {
        UUID doctorId = getLoggedInDoctorId();
        Optional<DoctorAvailability> doctorAvailabilityOpt = doctorAvailabilityRepository.findById(slotId);

        if (doctorAvailabilityOpt.isEmpty()) {
            return new ApiResponse("Không tìm thấy slot", false, null);
        }

        DoctorAvailability slot = doctorAvailabilityOpt.get();

        if (!slot.getDoctor().getId().equals(doctorId)) {
            return new ApiResponse("Bạn không có quyền xóa slot này", false, null);
        }

        if (slot.getStatus().equals(DoctorAvailabilityStatus.BOOKED.toString()) ||
                slot.getStatus().equals(DoctorAvailabilityStatus.PENDING.toString())) {
            return new ApiResponse("Không thể xoá slot đang có người đặt hoặc chờ xử lý", false, null);
        }

        doctorAvailabilityRepository.delete(slot);
        return new ApiResponse("Xoá thành công slot", true, null);
    }

    private SlotResponse mapToResponse(DoctorAvailability slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getDoctor().getId(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        );
    }
}
