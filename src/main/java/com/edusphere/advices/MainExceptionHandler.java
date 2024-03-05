package com.edusphere.advices;

import com.edusphere.vos.ApiErrorVO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@ControllerAdvice
public class MainExceptionHandler {
    @ExceptionHandler({Exception.class})
    public final ResponseEntity<ApiErrorVO> handleAllException(Exception e) {
        ApiErrorVO apiErrorVO = new ApiErrorVO(BAD_REQUEST, e.getMessage(), "Ups! A aparut o eroare!");

        return new ResponseEntity<>(apiErrorVO, apiErrorVO.getStatus());
    }

    @ExceptionHandler({AccessDeniedException.class})
    public final ResponseEntity<ApiErrorVO> handleAccessDeniedAllException(AccessDeniedException e) {
        ApiErrorVO apiErrorVO = new ApiErrorVO(FORBIDDEN, "Nu aveti suficiente drepturi pentru aceasta operatiune!",
                e.getMessage());

        return new ResponseEntity<>(apiErrorVO, apiErrorVO.getStatus());
    }

    @ExceptionHandler({NullPointerException.class})
    public final ResponseEntity<ApiErrorVO> handleAccessDeniedAllException(NullPointerException e) {
        ApiErrorVO apiErrorVO = new ApiErrorVO(BAD_REQUEST, "Ups! Ceva nu este in regula.", e.getMessage());

        return new ResponseEntity<>(apiErrorVO, apiErrorVO.getStatus());
    }

    @ExceptionHandler({IllegalStateException.class})
    public final ResponseEntity<ApiErrorVO> handleAccessDeniedAllException(IllegalStateException e) {
        ApiErrorVO apiErrorVO = new ApiErrorVO(FORBIDDEN, "Ups! Ceva nu este in regula.", e.getMessage());

        return new ResponseEntity<>(apiErrorVO, apiErrorVO.getStatus());
    }
}
