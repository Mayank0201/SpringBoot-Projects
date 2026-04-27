package com.example.cinetrackerbackend.common;

import java.util.Arrays;
import java.util.List;

public class ContentModerator {
    // Severe slurs that are never allowed
    private static final List<String> SLURS = Arrays.asList(
        "nigger", "faggot", "kike", "retard", "cunt"
    );

    // General profanity blocked in usernames
    private static final List<String> PROFANITY = Arrays.asList(
        "fuck", "shit", "bitch", "asshole", "piss", "dick", "bastard"
    );

    public static boolean containsSlur(String text) {
        if (text == null) return false;
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9]", "");
        return SLURS.stream().anyMatch(normalized::contains);
    }

    public static boolean containsAnyProfanity(String text) {
        if (text == null) return false;
        if (containsSlur(text)) return true;
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9]", "");
        return PROFANITY.stream().anyMatch(normalized::contains);
    }
}
