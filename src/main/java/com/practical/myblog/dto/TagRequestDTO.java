package com.practical.myblog.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagRequestDTO {

    @NotEmpty
    private List<@NotEmpty @Size(max = 20, message = "Tag length must be less than 20 characters") String> tags;
}
