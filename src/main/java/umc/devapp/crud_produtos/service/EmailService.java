package umc.devapp.crud_produtos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.reset.subject}")
    private String resetSubject;

    @Value("${app.mail.reset.template:Olá %s,\n\nVocê solicitou a recuperação de senha para sua conta no CRUD Produtos.\n\nPara redefinir sua senha, use o seguinte código:\n\n%s\n\nEste código é válido por 30 minutos.\n\nSe você não solicitou esta recuperação, ignore este email.\n\nAtenciosamente,\nEquipe CRUD Produtos}")
    private String resetTemplate;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        String subject = resetSubject;
        String body = String.format(resetTemplate, userName, resetToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
