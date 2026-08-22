package com.dayflow.hrmtool.employee;

import com.dayflow.hrmtool.auth.Role;
import com.dayflow.hrmtool.employee.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.dayflow.hrmtool.auth.AppUser;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    
    private Long resolveCompanyId(AppUser currentUser) {
        Long empId = currentUser.getEmployeeId();
        if (empId == null) {
            throw new com.dayflow.hrmtool.common.ResourceNotFoundException("Admin has no associated Employee profile");
        }
        return employeeService.getCompanyIdForEmployee(empId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request,
                                                           @RequestParam(required = false) Long companyId,
                                                           @AuthenticationPrincipal AppUser currentUser) {
        Long resolvedCompanyId = companyId != null ? companyId : resolveCompanyId(currentUser);
        return ResponseEntity.ok(employeeService.createEmployee(request, resolvedCompanyId, currentUser.getId()));
    }
    
    @GetMapping
    public ResponseEntity<Page<EmployeeCardDto>> getDirectory(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Long companyId = resolveCompanyId(currentUser);
        return ResponseEntity.ok(employeeService.getDirectory(companyId, search, pageable));
    }
    
    @GetMapping("/me")
    public ResponseEntity<EmployeeProfileDto> getMyProfile(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(employeeService.getMyProfile(currentUser.getId()));
    }
    
    @PutMapping("/me")
    public ResponseEntity<EmployeeProfileDto> updateOwnProfile(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestBody EmployeeSelfEditRequest request) {
        return ResponseEntity.ok(employeeService.updateOwnProfile(currentUser.getId(), request));
    }
    
    @PostMapping("/me/photo")
    public ResponseEntity<Void> uploadPhoto(@AuthenticationPrincipal AppUser currentUser, 
                                            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeProfileDto> getById(@PathVariable Long id, 
                                                      @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(employeeService.getById(id, currentUser.getId(), currentUser.getRole()));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeProfileDto> updateAsAdmin(@PathVariable Long id,
                                                            @RequestBody EmployeeAdminEditRequest request) {
        return ResponseEntity.ok(employeeService.updateAsAdmin(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
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
