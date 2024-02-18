package com.edusphere.loaders;

import org.springframework.stereotype.Component;

@Component
public class UserLoader {
    // uncomment the line from below if you want to insert a user with a password into db
// implements CommandLineRunner {
/*
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final OrganiziationRepository organiziationRepository;


    public UserLoader(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganiziationRepository organiziationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organiziationRepository = organiziationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create a new user
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("password"));
        user.setOrganization(organiziationRepository.getReferenceById(1));// Encrypt the password
        // Set other properties as needed

        // Save the user to the database
        userRepository.save(user);
    }*/
}
