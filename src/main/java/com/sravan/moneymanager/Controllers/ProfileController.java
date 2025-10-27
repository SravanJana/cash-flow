package com.sravan.moneymanager.Controllers;

import com.sravan.moneymanager.DTO.AuthDTO;
import com.sravan.moneymanager.DTO.ProfileDTO;
import com.sravan.moneymanager.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {


    private final ProfileService profileService;

    @PostMapping(value = "/register")
    public ResponseEntity<ProfileDTO> registerProfile(@Valid @RequestBody ProfileDTO profileDTO) {
        ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(registeredProfile);
    }


    @GetMapping(value = "/activate")
    public ResponseEntity<String> activateProfile(@RequestParam("token") String activationToken) {
        boolean isActivated = profileService.activateProfile(activationToken);


        if (isActivated) {
            return ResponseEntity.ok("Profile Activated Successfully");
        } else {
            return ResponseEntity.badRequest()
                                 .body("Invalid Activation Token");
        }


    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthDTO authDTO) {
        try {
            if (!profileService.isAccountActive(authDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                     .body(Map.of("message", "Account is not activated"));
            }

            Map<String,Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                                 .body(Map.of("message", e.getMessage()));

        }
    }


}
