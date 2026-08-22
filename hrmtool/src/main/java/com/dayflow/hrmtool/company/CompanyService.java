package com.dayflow.hrmtool.company;

import com.dayflow.hrmtool.common.ResourceNotFoundException;
import com.dayflow.hrmtool.company.dto.CompanyResponse;
import com.dayflow.hrmtool.company.dto.CreateCompanyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse createCompany(CreateCompanyRequest request) {
        Company company = Company.builder()
                .name(request.getName())
                .logoUrl(request.getLogoUrl())
                .initials(request.getInitials())
                .workingDaysPerWeek(request.getWorkingDaysPerWeek())
                .breakHours(request.getBreakHours())
                .build();
        
        Company saved = companyRepository.save(company);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(Long id) {
        Company company = findById(id);
        return mapToResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CreateCompanyRequest request) {
        Company company = findById(id);
        company.setName(request.getName());
        company.setLogoUrl(request.getLogoUrl());
        company.setInitials(request.getInitials());
        company.setWorkingDaysPerWeek(request.getWorkingDaysPerWeek());
        company.setBreakHours(request.getBreakHours());
        
        Company updated = companyRepository.save(company);
        return mapToResponse(updated);
    }

    Company findById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .initials(company.getInitials())
                .workingDaysPerWeek(company.getWorkingDaysPerWeek())
                .breakHours(company.getBreakHours())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
