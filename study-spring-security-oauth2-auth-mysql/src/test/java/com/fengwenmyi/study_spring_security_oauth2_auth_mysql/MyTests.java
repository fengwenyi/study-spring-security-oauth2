package com.fengwenmyi.study_spring_security_oauth2_auth_mysql;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author Erwin Feng
 * @since 2019/8/28 17:00
 */
public class MyTests {

    @Test
    public void testBCryptPasswordEncoder() {
        System.out.println(new BCryptPasswordEncoder().encode("client@1234"));
    }

}
