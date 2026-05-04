package com.topick.superapp.mhc.doctorAvailability.Controller;

import com.topick.superapp.mhc.ApiResponse;

import com.topick.superapp.mhc.doctorAvailability.Dto.CreateSlotRequest;
import com.topick.superapp.mhc.doctorAvailability.Service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/doctor/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse> bulkCreateSlots(@Validated @RequestBody CreateSlotRequest request){
        var response = doctorAvailabilityService.bulkCreateSlots(request);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        // Conflict thì trả về 409
        if(response.getMessage().contains("Phát hiện trùng lặp")){
            return ResponseEntity.status(409).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping()
    public ResponseEntity<ApiResponse> getSlots(@RequestParam(required = false) String status){
        var response = doctorAvailabilityService.getSlots(status);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<ApiResponse> deleteSlot(@PathVariable UUID slotId){
        var response = doctorAvailabilityService.deleteSlot(slotId);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        // Xử lý forbidden nếu cố tình xóa slot của người khác
        if(response.getMessage().equals("Bạn không có quyền xóa slot này")){
            return ResponseEntity.status(403).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}