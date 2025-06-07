package com.example.matchapp.model;

/**
 * Represents a user profile with personal information and image generation status.
 * This record is immutable and contains all the information needed to generate a profile image.
 */
public record Profile(
        /**
         * Unique identifier for the profile
         */
        String id,

        /**
         * First name of the person
         */
        String firstName,

        /**
         * Last name of the person
         */
        String lastName,

        /**
         * Age of the person in years
         */
        int age,

        /**
         * Ethnicity of the person
         */
        String ethnicity,

        /**
         * Gender of the person (MALE, FEMALE, NON_BINARY, OTHER)
         */
        Gender gender,

        /**
         * Biographical information about the person
         */
        String bio,

        /**
         * URL or path to the profile image
         */
        String imageUrl,

        /**
         * Myers-Briggs personality type (e.g., INTJ, ENFP)
         */
        String myersBriggsPersonalityType,

        /**
         * Flag indicating whether an image has been generated for this profile
         */
        boolean imageGenerated
) {
    /**
     * Secondary constructor that initializes a profile with imageGenerated set to false.
     *
     * @param id unique identifier for the profile
     * @param firstName first name of the person
     * @param lastName last name of the person
     * @param age age of the person in years
     * @param ethnicity ethnicity of the person
     * @param gender gender of the person
     * @param bio biographical information about the person
     * @param imageUrl URL or path to the profile image
     * @param myersBriggsPersonalityType Myers-Briggs personality type
     */
    public Profile(
            String id,
            String firstName,
            String lastName,
            int age,
            String ethnicity,
            Gender gender,
            String bio,
            String imageUrl,
            String myersBriggsPersonalityType
    ) {
        this(id, firstName, lastName, age, ethnicity, gender, bio, imageUrl,
                myersBriggsPersonalityType, false);
    }
}
