package com.sravan.moneymanager.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sravan.moneymanager.DTO.GoogleAuthDTO;
import com.sravan.moneymanager.DTO.ProfileDTO;
import com.sravan.moneymanager.Entity.AuthProvider;
import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Repo.ProfileRepo;
import com.sravan.moneymanager.jwtUtilPackage.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final ProfileRepo profileRepository;
    private final JwtUtil jwtUtil;
    private final ProfileService profileService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public GoogleAuthDTO authenticateWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // Check if user exists
            Optional<ProfileEntity> existingProfileOpt = profileRepository.findByEmail(email);
            ProfileEntity profile;

            if (existingProfileOpt.isPresent()) {
                profile = existingProfileOpt.get();
                // Update Google ID if not set
                if (profile.getGoogleId() == null) {
                    profile.setGoogleId(googleId);
                    profile.setAuthProvider(AuthProvider.GOOGLE);
                    profileRepository.save(profile);
                }
            } else {
                // Create new user
                profile = ProfileEntity.builder()
                        .email(email)
                        .fullName(name)
                        .googleId(googleId)
                        .profileImageUrl(pictureUrl)
                        .authProvider(AuthProvider.GOOGLE)
                        .isActive(true) // Google accounts are automatically activated
                        .build();
                profile = profileRepository.save(profile);
            }

            // Generate JWT token
            UserDetails userDetails = User.builder()
                    .username(profile.getEmail())
                    .password("") // No password for OAuth users
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                    .build();

            String jwtToken = jwtUtil.generateToken(userDetails);

            // Build response
            ProfileDTO profileDTO = profileService.toDTO(profile);

            return GoogleAuthDTO.builder()
                    .token(jwtToken)
                    .user(profileDTO)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to authenticate with Google: " + e.getMessage(), e);
        }
    }
}
