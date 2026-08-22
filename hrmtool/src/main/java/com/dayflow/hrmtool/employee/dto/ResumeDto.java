package com.dayflow.hrmtool.employee.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ResumeDto {
    private String about;
    private String whatILoveAboutMyJob;
    private String interestsAndHobbies;
    private List<String> skills;
    private List<CertificationDto> certifications;
}
