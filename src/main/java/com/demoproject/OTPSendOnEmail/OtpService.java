package com.demoproject.OTPSendOnEmail;

import java.util.HashMap;
import java.util.Map;

// for generating random number
// import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Makes this class as a Spring "service" component or Spring-managed service
@Service
public class OtpService {


    // Spring automatically injects JavaMailSender here or Spring give us an object of JavaMailSender
    @Autowired
    private JavaMailSender mailSender;

    // ✅ Class to hold OTP + time
    private static class OtpData {

        String otp;
        long timestamp;
        int attempts;
        long blockedUntil;

        OtpData(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
            this.attempts = 0;
            this.blockedUntil = 0;
        }
    }


    // ✅ Store email → OTP data
    private final Map<String, OtpData> otpStore = new HashMap<>();


    // Generate & send OTP to given email
    public void sendOtp(String userEmail) {

        OtpData existing = otpStore.get(userEmail);

        long now = System.currentTimeMillis();

        if (existing != null && existing.blockedUntil > now) {
            throw new RuntimeException("Too many attempts. Try again later.");
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        otpStore.put(userEmail, new OtpData(otp, now));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP: " + otp + "\nValid for 5 minutes.");

        mailSender.send(message);
    }

    // Verify OTP with expiry check
    public boolean verifyOtp(String email, String otp) {

        OtpData data = otpStore.get(email);

        if (data == null) return false;

        long now = System.currentTimeMillis();

        if (data.blockedUntil > now) {
            throw new RuntimeException("Account blocked for 10 minutes due to multiple wrong attempts.");
        }

        long diff = now - data.timestamp;

        if (diff > 300000) { // 5 minutes expiry
            otpStore.remove(email);
            return false;
        }

        if (data.otp.equals(otp)) {
            otpStore.remove(email);
            return true;
        }

        data.attempts++;

        if (data.attempts >= 3) {
            data.blockedUntil = now + (10 * 60 * 1000);
            throw new RuntimeException("Too many wrong attempts. Try after 10 minutes.");
        }

        return false;
    }

}
