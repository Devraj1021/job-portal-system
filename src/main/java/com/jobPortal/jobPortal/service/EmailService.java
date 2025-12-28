package com.jobPortal.jobPortal.service;

import com.jobPortal.jobPortal.dto.JobApplicantDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, JobApplicantDetails jobApplicantDetails) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject("Job application");
            String body = "Received an application for " + jobApplicantDetails.getJobId()
                    + " at " + jobApplicantDetails.getAppliedAt() + " by "
                    + jobApplicantDetails.getApplicantName() + " with email as "
                    + jobApplicantDetails.getApplicantName();
            mail.setText(body);
            javaMailSender.send(mail);
        } catch (Exception e) {
            System.out.println(e);
//            log.error("Exception while sendEmail ", e);
        }
    }

}
