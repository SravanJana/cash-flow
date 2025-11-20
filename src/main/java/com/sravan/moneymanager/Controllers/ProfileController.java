package com.sravan.moneymanager.Controllers;

import com.sravan.moneymanager.DTO.AuthDTO;
import com.sravan.moneymanager.DTO.ProfileDTO;
import com.sravan.moneymanager.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @Value("${moneyManger.frontend.url}")
    private String frontendUrl;

    @PostMapping(value = "/register")
    public ResponseEntity<ProfileDTO> registerProfile(@Valid @RequestBody ProfileDTO profileDTO) {
        ProfileDTO registeredProfile = profileService.registerProfile(profileDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registeredProfile);
    }

    @GetMapping(value = "/activate", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> activateProfile(@RequestParam("token") String activationToken) {
        boolean isActivated = profileService.activateProfile(activationToken);

        String htmlResponse;

        if (isActivated) {
            htmlResponse = "<!DOCTYPE html>"
                    + "<html lang='en'>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "<meta http-equiv='refresh' content='3;url=" + frontendUrl + "/login'>"
                    + "<title>Account Activated</title>"
                    + "</head>"
                    + "<body style='margin: 0; padding: 0; background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%); font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif; display: flex; align-items: center; justify-content: center; min-height: 100vh;'>"
                    + "<div style='text-align: center; background-color: #ffffff; padding: 60px 40px; border-radius: 16px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 500px; margin: 20px;'>"
                    + "<div style='font-size: 64px; margin-bottom: 20px;'>✅</div>"
                    + "<h1 style='margin: 0 0 15px 0; color: #1a202c; font-size: 32px; font-weight: 700;'>Account Activated!</h1>"
                    + "<p style='margin: 0 0 30px 0; color: #4a5568; font-size: 18px; line-height: 1.6;'>Your account has been successfully activated.</p>"
                    + "<div style='background-color: #f7fafc; border-radius: 8px; padding: 20px; margin: 25px 0;'>"
                    + "<p style='margin: 0; color: #2d3748; font-size: 15px;'>Redirecting you to login page in <strong>3 seconds</strong>...</p>"
                    + "</div>"
                    + "<a href='" + frontendUrl + "/login' style='display: inline-block; background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%); color: #ffffff; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 16px; margin-top: 10px;'>Go to Login Now</a>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlResponse);
        } else {
            htmlResponse = "<!DOCTYPE html>"
                    + "<html lang='en'>"
                    + "<head>"
                    + "<meta charset='UTF-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                    + "<title>Activation Failed</title>"
                    + "</head>"
                    + "<body style='margin: 0; padding: 0; background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, \"Helvetica Neue\", Arial, sans-serif; display: flex; align-items: center; justify-content: center; min-height: 100vh;'>"
                    + "<div style='text-align: center; background-color: #ffffff; padding: 60px 40px; border-radius: 16px; box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 500px; margin: 20px;'>"
                    + "<div style='font-size: 64px; margin-bottom: 20px;'>❌</div>"
                    + "<h1 style='margin: 0 0 15px 0; color: #1a202c; font-size: 32px; font-weight: 700;'>Activation Failed</h1>"
                    + "<p style='margin: 0 0 30px 0; color: #4a5568; font-size: 18px; line-height: 1.6;'>The activation link is invalid or has already been used.</p>"
                    + "<div style='background-color: #fef2f2; border-left: 4px solid #ef4444; border-radius: 8px; padding: 20px; margin: 25px 0; text-align: left;'>"
                    + "<p style='margin: 0; color: #991b1b; font-size: 14px; line-height: 1.6;'><strong>Possible reasons:</strong><br/>• Link has expired<br/>• Account already activated<br/>• Invalid token</p>"
                    + "</div>"
                    + "<a href='" + frontendUrl + "/signup' style='display: inline-block; background: linear-gradient(135deg, #8b5cf6 0%, #6366f1 100%); color: #ffffff; padding: 14px 32px; border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 16px; margin-top: 10px;'>Sign Up Again</a>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthDTO authDTO) {
        try {
            if (!profileService.isAccountActive(authDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Account is not activated"));
            }

            Map<String, Object> response = profileService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        }
    }

}
