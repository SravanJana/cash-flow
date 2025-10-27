package com.sravan.moneymanager.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthDTO {
    @NotBlank(message = "Please enter your email")
    @Email(message = "Please enter a valid email")
    public String email;
    public String password;
}
