package com.edusphere.vos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
@AllArgsConstructor
public class ApiErrorVO {

    private HttpStatus status;
    private String message;
    private String error;
}
