package com.example.cvcreator.userTest;

import com.example.cvcreator.security.CryptoService;
import com.example.cvcreator.user.User;
import com.example.cvcreator.user.UserRepository;
import com.example.cvcreator.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CryptoService cryptoService;

    @Test
    void testCreateUserAndEncryptPassword() throws Exception {
        User user = userService.createUser(
                "testuser",
                "test@example.com",
                "SuperSecret123",
                User.Role.USER
        );

        assertNotNull(user.getId(), "ID powinno być wygenerowane");
        assertNotNull(user.getPassword(), "Hasło powinno być zaszyfrowane");

        User savedUser = userRepository.findById(user.getId()).orElseThrow();
    }
}
