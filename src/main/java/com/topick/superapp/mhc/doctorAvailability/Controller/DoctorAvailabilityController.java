package com.topick.superapp.mhc.doctorAvailability.Controller;

import com.topick.superapp.mhc.ApiResponse;
import com.topick.superapp.mhc.doctorAvailability.Dto.CreateSlotRequest;
import com.topick.superapp.mhc.doctorAvailability.Service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/doctor/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    @PostMapping()
    public ResponseEntity<ApiResponse> createSlot(@RequestBody CreateSlotRequest createSlotRequest){
        var response = doctorAvailabilityService.CreateSlot(createSlotRequest);
        if(response.isSuccess()){
            return ResponseEntity.ok(response);
        }
        if(response.getMessage().equals("Slot này đã tồn tại")){ return ResponseEntity.status(409).body(response);}
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
        return ResponseEntity.badRequest().body(response);
    }
}
