package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import com.example.demo.event.RegistrationCompleteEvent;
import com.example.demo.model.PasswordModel;
import com.example.demo.model.UserModel;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request){
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user, applicationUrl(request)
        ));
        return "success";

    }

    @GetMapping(path = "/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token){
        String result = userService.validateVerificationToken(token);
        if (result.equalsIgnoreCase("valid")){
            return "User verified successfully";
        }
        return "bad user";
    }

    @GetMapping(path = "/resendVerifyToken")
    private String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request){
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent Successfully";

    }

    @PostMapping(path = "/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request){

        User user = userService.findByEmail(passwordModel.getEmail());
        String url = "";
        if(user!= null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetTokenMail(user, applicationUrl(request), token);
        }
        return url;
    }



    @PostMapping(path = "/savePassword")
    private String savePassword(@RequestParam("token") String token,
                                @RequestBody PasswordModel passwordModel){
        String result = userService.validatePasswordToken(token);
        if (!result.equalsIgnoreCase("valid")){
            return "invalid token";

        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()){
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            return "password reset successfully";

        }else {
            return "Invalid Token";

        }

    }
    @PostMapping(path = "/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        User user = userService.findByEmail(passwordModel.getEmail());
        if (!userService.checkIfValidOldPassword(user, passwordModel.getOldPassword())){
            return "Invalid old password";
        }
        // save new password
        userService.changePassword(user, passwordModel.getNewPassword());
        return "password changed successfully";

    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl
                + "/savePassword?token="
                + token;
        // send verification Email
        log.info("Click the link below to reset your password: {}", url);
        return url;
    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url = applicationUrl
                + "/verifyRegistration?token="
                + verificationToken.getToken();
        // send verification Email
        log.info("Click the link to verify your account: {}", url);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName()
                + ":"
                + request.getServerPort()
                + request.getContextPath();
    }
}
