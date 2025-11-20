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

    private static final String EMAIL_REGEX
            = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

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
        newProfile.setAuthProvider(com.sravan.moneymanager.Entity.AuthProvider.LOCAL);

        ProfileEntity saveProfile = profileRepository.save(newProfile);

        ProfileDTO dto = toDTO(saveProfile);
        dto.setPassword("Not Disclosed");

        //Send Activation Email
        String activationLink = backendUrl + "api/v1/activate?token=" + saveProfile.getActivationToken();
        String subject = "🎉 Welcome to Money Manager - Activate Your Account";

        String body = "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "</head>"
                + "<body style='margin: 0; padding: 0; background-color: #0a0a0a; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='background-color: #0a0a0a; padding: 40px 20px;'>"
                + "<tr><td align='center'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='background-color: #1a1a1a; border-radius: 12px; border: 1px solid #2a2a2a; overflow: hidden;'>"
                + "<!-- Header -->"
                + "<tr><td style='background: linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%); padding: 40px 30px; text-align: center;'>"
                + "<h1 style='margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;'>🎉 Welcome to Money Manager!</h1>"
                + "<p style='margin: 10px 0 0 0; color: #fce7f3; font-size: 14px;'>Your journey to financial freedom starts here</p>"
                + "</td></tr>"
                + "<!-- Content -->"
                + "<tr><td style='padding: 40px;'>"
                + "<h2 style='margin: 0 0 20px 0; color: #e0e0e0; font-size: 20px; font-weight: 500;'>Hi " + saveProfile.getFullName() + "! 👋</h2>"
                + "<p style='margin: 0 0 25px 0; color: #b0b0b0; font-size: 15px; line-height: 1.6;'>Thank you for signing up! We're excited to help you take control of your finances. To get started, please activate your account by clicking the button below.</p>"
                + "<div style='background-color: #242424; border-left: 3px solid #8b5cf6; padding: 18px; margin: 25px 0; border-radius: 6px;'>"
                + "<p style='margin: 0; color: #c0c0c0; font-size: 14px; line-height: 1.6;'>💡 <strong>Why activate?</strong> This helps us verify your email and keep your account secure.</p>"
                + "</div>"
                + "<!-- CTA Button -->"
                + "<table width='100%' cellpadding='0' cellspacing='0' style='margin: 30px 0;'>"
                + "<tr><td align='center'>"
                + "<a href='" + activationLink + "' style='display: inline-block; background: linear-gradient(135deg, #8b5cf6 0%, #ec4899 100%); color: #ffffff; padding: 16px 48px; border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 15px;'>Activate My Account</a>"
                + "</td></tr></table>"
                + "<p style='margin: 25px 0 0 0; color: #888888; font-size: 13px; line-height: 1.6; text-align: center;'>If the button doesn't work, copy and paste this link into your browser:<br/>"
                + "<span style='color: #8b5cf6; word-break: break-all;'>" + activationLink + "</span></p>"
                + "</td></tr>"
                + "<!-- Footer -->"
                + "<tr><td style='background-color: #242424; padding: 25px 40px; text-align: center; border-top: 1px solid #3a3a3a;'>"
                + "<p style='margin: 0; color: #888888; font-size: 13px;'>Best regards,<br/><strong style='color: #a0a0a0;'>The Money Manager Team</strong></p>"
                + "<p style='margin: 15px 0 0 0; color: #666666; font-size: 12px;'>If you didn't create this account, you can safely ignore this email.</p>"
                + "</td></tr>"
                + "</table>"
                + "</td></tr></table>"
                + "</body>"
                + "</html>";

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
