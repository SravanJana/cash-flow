package com.sravan.moneymanager.Service;

import com.sravan.moneymanager.DTO.AuthDTO;
import com.sravan.moneymanager.DTO.ProfileDTO;
import com.sravan.moneymanager.Entity.ProfileEntity;
import com.sravan.moneymanager.Repo.ProfileRepo;
import com.sravan.moneymanager.jwtUtilPackage.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";


    private final ProfileRepo profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    @Value("${moneyManger.backend.url}")
    private String backendUrl;


    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        if (!profileDTO.getEmail().matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID()
                                          .toString());
        
        ProfileEntity saveProfile = profileRepository.save(newProfile);

        ProfileDTO dto = toDTO(saveProfile);
        dto.setPassword("Not Disclosed");

        //Send Activation Email
        String activationLink = backendUrl + "api/v1/activate?token=" + saveProfile.getActivationToken();
        String subject = "Account Activation";
        String body = "Please click on the link below to activate your account: " + activationLink;
        emailService.sendEmail(saveProfile.getEmail(), subject, body);

        return dto;
    }

    public boolean activateProfile(String activationToken) {
        //Retrieve Profile by Activation Token
        Optional<ProfileEntity> profileEntity = profileRepository.findByActivationToken(activationToken);

        Boolean activationResult = profileEntity.map(profile -> {
                                                    profile.setIsActive(true);
                                                    profileRepository.save(profile);
                                                    return true;
                                                })
                                                .orElse(false);

        return activationResult;

    }

    public boolean isAccountActive(String email) {
        Optional<ProfileEntity> profileEntity = profileRepository.findByEmail(email);
        return profileEntity.map(ProfileEntity::getIsActive)
                            .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {

        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();
        String email = authentication.getName();
        return profileRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Profile not found with email: " + email));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity profileEntity;
        if (email == null) {
            profileEntity = getCurrentProfile();
        } else {
            profileEntity = profileRepository.findByEmail(email)
                                             .orElseThrow(() -> new RuntimeException(
                                                     "Profile not found with email: " + email));
        }
        ProfileDTO dto = toDTO(profileEntity);

        return dto;
    }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));

            if (authentication.isAuthenticated()) {
                return Map.of("token", jwtUtil.generateToken((UserDetails) authentication.getPrincipal()),
                              "User", getPublicProfile(authDTO.getEmail()));
            } else {
                throw new RuntimeException("Invalid Email or Password");
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid Email or Password");


        }


    }

    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                            .id(profileDTO.getId())
                            .fullName(profileDTO.getFullName())
                            .email(profileDTO.getEmail())
                            .password(passwordEncoder.encode(profileDTO.getPassword()))
                            .profileImageUrl(profileDTO.getProfileImageUrl())
                            .createdAt(profileDTO.getCreatedAt())
                            .updatedAt(profileDTO.getUpdatedAt())
                            .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                         .id(profileEntity.getId())
                         .fullName(profileEntity.getFullName())
                         .email(profileEntity.getEmail())
                         .profileImageUrl(profileEntity.getProfileImageUrl())
                         .password("Not Disclosed")
                         .createdAt(profileEntity.getCreatedAt())
                         .updatedAt(profileEntity.getUpdatedAt())
                         .build();
    }


}
