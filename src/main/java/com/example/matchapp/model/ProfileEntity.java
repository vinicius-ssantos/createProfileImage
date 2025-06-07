package com.example.matchapp.model;

/**
 * Domain entity representing a user profile with personal information and image generation status.
 * This class is the core domain model for profiles in the application.
 */
public class ProfileEntity {
    private String id;
    private String firstName;
    private String lastName;
    private int age;
    private String ethnicity;
    private Gender gender;
    private String bio;
    private String imageUrl;
    private String myersBriggsPersonalityType;
    private boolean imageGenerated;

    /**
     * Default constructor for JPA and other frameworks.
     */
    public ProfileEntity() {
    }

    /**
     * Full constructor for creating a profile entity with all fields.
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
     * @param imageGenerated flag indicating whether an image has been generated for this profile
     */
    public ProfileEntity(
            String id,
            String firstName,
            String lastName,
            int age,
            String ethnicity,
            Gender gender,
            String bio,
            String imageUrl,
            String myersBriggsPersonalityType,
            boolean imageGenerated
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.ethnicity = ethnicity;
        this.gender = gender;
        this.bio = bio;
        this.imageUrl = imageUrl;
        this.myersBriggsPersonalityType = myersBriggsPersonalityType;
        this.imageGenerated = imageGenerated;
    }

    /**
     * Constructor that initializes a profile with imageGenerated set to false.
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
    public ProfileEntity(
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

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMyersBriggsPersonalityType() {
        return myersBriggsPersonalityType;
    }

    public void setMyersBriggsPersonalityType(String myersBriggsPersonalityType) {
        this.myersBriggsPersonalityType = myersBriggsPersonalityType;
    }

    public boolean isImageGenerated() {
        return imageGenerated;
    }

    public void setImageGenerated(boolean imageGenerated) {
        this.imageGenerated = imageGenerated;
    }
}