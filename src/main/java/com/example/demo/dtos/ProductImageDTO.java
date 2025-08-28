package com.example.demo.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageDTO {
    @JsonProperty("product_id")
    @Min(value = 1, message = "Product's Id must be > 0")
    private Long productId;

    @JsonProperty("iamge_url")
    @Size(min = 5, max = 200, message = "Image's name")
    private String imageUrl;
}
