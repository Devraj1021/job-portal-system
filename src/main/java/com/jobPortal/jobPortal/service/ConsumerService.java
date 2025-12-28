package com.jobPortal.jobPortal.service;

import com.jobPortal.jobPortal.dto.JobApplicantDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "job-applications", groupId = "job-portal-group")
    public void consume(
            JobApplicantDetails value,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        emailService.sendEmail(key, value);
    }

}
