package com.practical.myblog.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    @NotNull
    @Size(max = 15, message = "Username length must be less than 15 characters")
    private String username;

    @NotNull
    @Size(max = 25, message = "Password length must be less than 25 characters")
    private String password;

    @NotNull
    @Size(max = 20, message = "Display name length must be less than 20 characters")
    private String displayName;
}
