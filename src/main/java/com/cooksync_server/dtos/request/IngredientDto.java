package com.cooksync_server.dtos.request;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class IngredientDto {

    private String name;
    private BigDecimal quantity;
    private String unitCode; // למשל "g", "cup", "tbsp" (חייב להיות קיים בטבלת units)
}
