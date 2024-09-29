package com.practical.myblog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDTO {

    @NotEmpty
    @Size(max = 60, message = "Title length must be less than 60 characters")
    private String title;

    @Size(max = 1000, message = "Post length must be less than 1000 characters")
    private String text;
}
