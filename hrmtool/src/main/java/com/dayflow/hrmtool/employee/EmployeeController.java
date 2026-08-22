package com.dayflow.hrmtool.employee;

import com.dayflow.hrmtool.auth.Role;
import com.dayflow.hrmtool.employee.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    
    @PostMapping("/")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request,
                                                           @RequestAttribute("companyId") Long companyId,
                                                           @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(employeeService.createEmployee(request, companyId, userId));
    }
    
    @GetMapping("/")
    public ResponseEntity<Page<EmployeeCardDto>> getDirectory(
            @RequestAttribute("companyId") Long companyId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(employeeService.getDirectory(companyId, search, pageable));
    }
    
    @GetMapping("/me")
    public ResponseEntity<EmployeeProfileDto> getMyProfile(@RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(employeeService.getMyProfile(userId));
    }
    
    @PutMapping("/me")
    public ResponseEntity<EmployeeProfileDto> updateOwnProfile(
            @RequestAttribute("userId") Long userId,
            @RequestBody EmployeeSelfEditRequest request) {
        return ResponseEntity.ok(employeeService.updateOwnProfile(userId, request));
    }
    
    @PostMapping("/me/photo")
    public ResponseEntity<Void> uploadPhoto(@RequestAttribute("userId") Long userId, 
                                            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeProfileDto> getById(@PathVariable Long id, 
                                                      @RequestAttribute("userId") Long viewerId,
                                                      @RequestAttribute("role") Role role) {
        return ResponseEntity.ok(employeeService.getById(id, viewerId, role));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeProfileDto> updateAsAdmin(@PathVariable Long id, 
                                                            @RequestBody EmployeeAdminEditRequest request) {
        return ResponseEntity.ok(employeeService.updateAsAdmin(id, request));
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        employeeService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/resume")
    public ResponseEntity<ResumeDto> getResume(@PathVariable Long id) {
        return ResponseEntity.ok(ResumeDto.builder().build());
    }
    
    @PutMapping("/{id}/resume")
    public ResponseEntity<ResumeDto> updateResume(@PathVariable Long id, @RequestBody ResumeDto request) {
        return ResponseEntity.ok(request);
    }
    
    @GetMapping("/{id}/skills")
    public ResponseEntity<Void> getSkills(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/skills")
    public ResponseEntity<Void> addSkill(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id, @PathVariable Long skillId) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/certifications")
    public ResponseEntity<Void> getCertifications(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/certifications")
    public ResponseEntity<Void> addCertification(@PathVariable Long id) {
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}/certifications/{certId}")
    public ResponseEntity<Void> deleteCertification(@PathVariable Long id, @PathVariable Long certId) {
        return ResponseEntity.ok().build();
    }
}
