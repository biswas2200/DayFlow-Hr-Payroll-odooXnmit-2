package com.dayflow.hrmtool.auth;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LoginIdGenerator {

    public String generate(String companyInitials, String firstName, String lastName, int joinYear, int serial) {
        String fName = StringUtils.hasText(firstName) ? firstName.toUpperCase() : "XX";
        String lName = StringUtils.hasText(lastName) ? lastName.toUpperCase() : "XX";

        String fPart = fName.length() >= 2 ? fName.substring(0, 2) : fName;
        String lPart = lName.length() >= 2 ? lName.substring(0, 2) : lName;

        String namePart = fPart + lPart;
        String serialPart = String.format("%04d", serial);

        return (companyInitials != null ? companyInitials : "") + namePart + joinYear + serialPart;
    }
}
