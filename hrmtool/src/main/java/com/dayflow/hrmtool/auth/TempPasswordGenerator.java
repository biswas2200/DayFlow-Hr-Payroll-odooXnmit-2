package com.dayflow.hrmtool.auth;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class TempPasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIALS = "!@#$%^&*()-_=+";
    
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        int length = 12;
        StringBuilder password = new StringBuilder();
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIALS.charAt(random.nextInt(SPECIALS.length())));

        String allAllowed = UPPER + LOWER + DIGITS + SPECIALS;
        for (int i = 4; i < length; i++) {
            password.append(allAllowed.charAt(random.nextInt(allAllowed.length())));
        }

        List<Character> chars = IntStream.range(0, password.length())
                .mapToObj(password::charAt)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        return chars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
