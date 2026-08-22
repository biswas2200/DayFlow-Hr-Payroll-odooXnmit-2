package com.dayflow.hrmtool.company;

import com.dayflow.hrmtool.common.ApiResponse;
import com.dayflow.hrmtool.company.dto.CompanyResponse;
import com.dayflow.hrmtool.company.dto.CreateCompanyRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ApiResponse.success("Company created successfully", response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanyResponse> getCompany(@PathVariable Long id) {
        CompanyResponse response = companyService.getCompany(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CompanyResponse> updateCompany(@PathVariable Long id, @Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = companyService.updateCompany(id, request);
        return ApiResponse.success("Company updated successfully", response);
    }
}
