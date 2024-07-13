package com.twitter.email.listener;

import com.gmail.merikbest2015.dto.request.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class AmqpConsumer {
    private final JavaMailSender emailSender;
    private final SpringTemplateEngine thymeleafTemplateEngine;

    @Transactional
    @RabbitListener(queues = "${rabbitmq.internal-mail.listener.queue-name}")
    public void onRabbitMQMessageListener(EmailRequest emailRequest) throws MessagingException {
        System.out.println(emailRequest);
        MimeMessage message = createMimeMessage();
        mimeMessageHelper(message, emailRequest);
        sendEmail(message);
    }

    private MimeMessage createMimeMessage() {
        return emailSender.createMimeMessage();
    }

    private void mimeMessageHelper(MimeMessage message, EmailRequest emailRequest) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context thymeleafContext = new Context();
        setEmailVariables(thymeleafContext, emailRequest);

        String htmlBody = thymeleafTemplateEngine.process("registration-template.html", thymeleafContext);
        helper.setTo(emailRequest.getTo());
        helper.setSubject(emailRequest.getSubject());
        helper.setText(htmlBody, true);
    }

    private void setEmailVariables(Context thymeleafContext, EmailRequest emailRequest) {
        thymeleafContext.setVariables(emailRequest.getAttributes());
    }

    private void sendEmail(MimeMessage message) {
        emailSender.send(message);
    }

}
