package ml.apes.rh.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;

class CompteEmailServiceTest {

    @Test
    void welcomeAndPasswordResetEmailsUseTheNewApplicationName() throws Exception {
        RecordingMailSender sender = new RecordingMailSender();
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("mailSender", sender);
        CompteEmailService service = new CompteEmailService(
            beans.getBeanProvider(JavaMailSender.class)
        );
        ReflectionTestUtils.setField(service, "host", "mail.example.invalid");
        ReflectionTestUtils.setField(service, "from", "no-reply@example.invalid");
        ReflectionTestUtils.setField(service, "frontendUrl", "https://example.invalid");

        service.envoyerMotDePasseTemporaire(
            "agent@example.invalid",
            "AGENT-TEST",
            "Nom <test>",
            "temporary-test"
        );
        service.envoyerLienReinitialisation(
            "agent@example.invalid",
            "https://example.invalid/login?reset=test"
        );

        assertEquals(2, sender.messages.size());
        for (MimeMessage message : sender.messages) {
            String html = (String) message.getContent();
            assertTrue(message.getSubject().contains("SIGPAP-MALI"));
            assertTrue(html.contains("SIGPAP-MALI"));
            assertTrue(html.contains("Administration pénitentiaire (AP)"));
            assertFalse(message.getSubject().contains("APES"));
            assertFalse(html.contains("APES"));
        }
        assertTrue(((String) sender.messages.get(0).getContent()).contains("Nom &lt;test&gt;"));
    }

    private static class RecordingMailSender extends JavaMailSenderImpl {

        final List<MimeMessage> messages = new ArrayList<>();

        @Override
        public void send(MimeMessage message) {
            messages.add(message);
        }
    }
}
