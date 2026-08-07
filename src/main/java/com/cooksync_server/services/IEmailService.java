package com.cooksync_server.services;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

public interface IEmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}