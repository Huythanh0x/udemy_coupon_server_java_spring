package com.huythanh0x.udemycoupons.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
//No extend here
public class ErrorObject {
    @JsonProperty("timestamp")
    Date timeStamp;
    int status;
    String error;
    String message;
    String path;
}
