package com.thanh0x.coursedeal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Standardized error response for the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorDTO {
    private Date timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
