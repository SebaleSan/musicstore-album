package com.duoc.album.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlbumRequest(

        @NotBlank(message = "El artista es obligatorio")
        @Size(max = 120, message = "El artista no puede tener más de 120 caracteres")
        String artist,

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede tener más de 150 caracteres")
        String title,

        @Min(value = 1900, message = "El año debe ser mayor o igual a 1900")
        @Max(value = 2100, message = "El año debe ser menor o igual a 2100")
        Integer year,

        @NotBlank(message = "El género es obligatorio")
        @Size(max = 60, message = "El género no puede tener más de 60 caracteres")
        String genre,

        @NotNull(message = "El precio es obligatorio")
        @Min(value = 0, message = "El precio no puede ser negativo")
        Long price,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @Size(max = 255, message = "La portada no puede tener más de 255 caracteres")
        String coverArt) {
}
