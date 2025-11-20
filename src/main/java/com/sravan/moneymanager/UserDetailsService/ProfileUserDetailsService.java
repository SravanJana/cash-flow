package com.sravan.moneymanager.UserDetailsService;

import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Repo.ProfileRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ProfileUserDetailsService implements UserDetailsService {

    private final ProfileRepo profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        ProfileEntity existingProfile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                "Profile not found with email: " + email));

        // Handle Google OAuth users who don't have passwords
        String password = existingProfile.getPassword();
        if (password == null || password.isEmpty()) {
            password = ""; // Use empty string for OAuth users
        }

        return User.builder()
                .username(existingProfile.getEmail())
                .password(password)
                .authorities(Collections.emptyList())
                .build();

    }
}
