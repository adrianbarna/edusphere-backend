package com.edusphere.controllers.utils;

import com.edusphere.controllers.AuthController;
import com.edusphere.vos.LoginRequestVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenTestUtils {

    @Autowired
    private AuthController authController;

    public String getTokenForUser(String userName, String password) {
        LoginRequestVO requestVO = new LoginRequestVO();
        requestVO.setUsername(userName);
        requestVO.setPassword(password);
        return authController.authenticateUser(requestVO).getBody();
    }
}
