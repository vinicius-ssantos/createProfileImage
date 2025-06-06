package com.example.matchapp.service;

import com.example.matchapp.model.Profile;

public interface ImageGenerationService {
    byte[] generateImage(Profile profile);
}
