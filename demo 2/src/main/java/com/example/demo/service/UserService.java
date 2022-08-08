package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import com.example.demo.model.UserModel;
import org.springframework.stereotype.Service;

import java.util.Optional;

public interface UserService {
    User registerUser(UserModel userModel);


    void saveVerificationTokenForUser(String token, User user);


    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);
}
