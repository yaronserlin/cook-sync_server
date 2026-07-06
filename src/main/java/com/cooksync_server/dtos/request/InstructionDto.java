package com.cooksync_server.dtos.request;

import lombok.Data;

@Data
public class InstructionDto {

    private int stepNumber;
    private String description;
    private boolean hasTimer;
    private Integer timeSeconds; // יכול להיות null אם אין טיימר
}
