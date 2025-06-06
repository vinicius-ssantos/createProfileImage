package com.example.matchapp.model;

public record Profile(
        String id,
        String firstName,
        String lastName,
        int age,
        String ethnicity,
        String gender,
        String bio,
        String imageUrl,
        String myersBriggsPersonalityType,
        boolean imageGenerated
) {
    public Profile(
            String id,
            String firstName,
            String lastName,
            int age,
            String ethnicity,
            String gender,
            String bio,
            String imageUrl,
            String myersBriggsPersonalityType
    ) {
        this(id, firstName, lastName, age, ethnicity, gender, bio, imageUrl,
                myersBriggsPersonalityType, false);
    }
}
