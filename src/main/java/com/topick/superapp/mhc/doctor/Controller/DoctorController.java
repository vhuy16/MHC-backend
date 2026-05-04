    package com.topick.superapp.mhc.doctor.Controller;

    import com.topick.superapp.mhc.ApiResponse;
    import com.topick.superapp.mhc.doctor.Service.DoctorService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.UUID;

    @RestController
    @RequestMapping("/api/doctors")
    @RequiredArgsConstructor
    public class DoctorController {

        private final DoctorService doctorService;

        @GetMapping
        public ResponseEntity<ApiResponse> getAllDoctors() {
            return ResponseEntity.ok(doctorService.findAllDoctor());
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse> getDoctorById(@PathVariable UUID id) {
            return ResponseEntity.ok(doctorService.findDoctorById(id));
        }


        @GetMapping("/search")
        public ResponseEntity<ApiResponse> searchDoctors(@RequestParam String q) {
            return ResponseEntity.ok(doctorService.searchDoctors(q));
        }
    }