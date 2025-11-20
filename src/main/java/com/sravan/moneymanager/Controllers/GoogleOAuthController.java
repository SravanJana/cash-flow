package com.sravan.moneymanager.Controllers;

import com.sravan.moneymanager.DTO.GoogleAuthDTO;
import com.sravan.moneymanager.Service.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class GoogleOAuthController {

    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("credential");
            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID token is required"));
            }

            GoogleAuthDTO response = googleOAuthService.authenticateWithGoogle(idToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Google authentication failed: " + e.getMessage()));
        }
    }
}
