package io.deviceid.create_ia_profiles;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.service.ProfileService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = {"io.deviceid.create_ia_profiles", "com.example.matchapp"})
@EnableConfigurationProperties(ImageGenProperties.class)
public class CreateIaProfilesApplication implements CommandLineRunner {

    private final ProfileService profileService;

    public CreateIaProfilesApplication(ProfileService profileService) {
        this.profileService = profileService;
    }

    public static void main(String[] args) {
        Dotenv.configure().ignoreIfMissing().load();
        SpringApplication.run(CreateIaProfilesApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        profileService.generateImages(Paths.get("src/main/resources/static/images"));
    }
}
