package ml.apes.rh.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.exception.BusinessException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Génère et transmet les mots de passe temporaires remis aux nouveaux agents. */
@Service
@RequiredArgsConstructor
public class CompteEmailService {

    private static final String MAJUSCULES = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULES = "abcdefghijkmnopqrstuvwxyz";
    private static final String CHIFFRES = "23456789";
    private static final String SPECIAUX = "!@#$%*-_";
    private static final String TOUS = MAJUSCULES + MINUSCULES + CHIFFRES + SPECIAUX;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ObjectProvider<JavaMailSender> senders;

    @Value("${spring.mail.host:}")
    private String host;

    @Value("${app.mail-from:no-reply@ap.local}")
    private String from;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public String genererMotDePasse() {
        List<Character> chars = new ArrayList<>(16);
        chars.add(choisir(MAJUSCULES));
        chars.add(choisir(MINUSCULES));
        chars.add(choisir(CHIFFRES));
        chars.add(choisir(SPECIAUX));
        while (chars.size() < 16) chars.add(choisir(TOUS));
        for (int i = chars.size() - 1; i > 0; i--) Collections.swap(
            chars,
            i,
            RANDOM.nextInt(i + 1)
        );
        StringBuilder result = new StringBuilder(chars.size());
        chars.forEach(result::append);
        return result.toString();
    }

    public void verifierConfiguration() {
        if (!StringUtils.hasText(host) || senders.getIfAvailable() == null) {
            throw new BusinessException(
                "L'envoi du mot de passe par e-mail n'est pas configuré. Renseignez MAIL_HOST et les paramètres SMTP."
            );
        }
    }

    public void envoyerMotDePasseTemporaire(
        String email,
        String matricule,
        String nomComplet,
        String motDePasse
    ) {
        verifierConfiguration();
        String contenu =
            "<p>Bonjour " +
            echapper(nomComplet) +
            ",</p>" +
            "<p>Votre compte SIGPAP-MALI de l’Administration pénitentiaire (AP) est prêt. Voici vos identifiants de première connexion :</p>" +
            "<div style=\"background:#faf8f7;border:1px solid #e8dfdc;border-radius:8px;padding:16px;margin:20px 0\">" +
            "<div style=\"margin:5px 0\"><span style=\"display:inline-block;width:190px;color:#756969\">Matricule</span><strong style=\"color:#4b0d0d\">" +
            echapper(matricule) +
            "</strong></div>" +
            "<div style=\"margin:5px 0\"><span style=\"display:inline-block;width:190px;color:#756969\">Mot de passe temporaire</span>" +
            "<strong style=\"color:#4b0d0d;font-family:monospace;font-size:16px\">" +
            echapper(motDePasse) +
            "</strong></div></div>" +
            "<p>À votre première connexion, l’application vous demandera de choisir un nouveau mot de passe. " +
            "Ne communiquez jamais ce mot de passe.</p>" +
            "<p><a style=\"display:inline-block;background:#8b1e1e;color:#fff;padding:12px 20px;border-radius:6px;text-decoration:none;font-weight:bold\" href=\"" +
            echapper(frontendUrl.replaceAll("/$", "")) +
            "/login\">Accéder à SIGPAP-MALI</a></p>";
        envoyer(email, "Votre accès à SIGPAP-MALI", modele("Bienvenue sur SIGPAP-MALI", contenu));
    }

    public void envoyerLienReinitialisation(String email, String lien) {
        verifierConfiguration();
        String contenu =
            "<p>Bonjour,</p><p>Une demande de réinitialisation du mot de passe de votre compte SIGPAP-MALI a été reçue.</p>" +
            "<p><a style=\"display:inline-block;background:#8b1e1e;color:#fff;padding:12px 20px;border-radius:6px;text-decoration:none;font-weight:bold\" href=\"" +
            echapper(lien) +
            "\">Choisir un nouveau mot de passe</a></p>" +
            "<p>Ce lien est valable 30 minutes. Si vous n’êtes pas à l’origine de cette demande, vous pouvez ignorer ce message.</p>";
        envoyer(
            email,
            "Réinitialisation de votre mot de passe SIGPAP-MALI",
            modele("Sécurisez votre compte", contenu)
        );
    }

    private void envoyer(String email, String sujet, String html) {
        try {
            MimeMessage message = senders.getObject().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject(sujet);
            helper.setText(html, true);
            senders.getObject().send(message);
        } catch (MessagingException | MailException ex) {
            throw new BusinessException(
                "L’e-mail n’a pas pu être envoyé. Vérifiez la configuration SMTP."
            );
        }
    }

    private static String modele(String titre, String contenu) {
        return (
            "<!doctype html><html lang=\"fr\"><head><meta charset=\"UTF-8\"></head>" +
            "<body style=\"margin:0;background:#f4f1ef;font-family:Arial,Helvetica,sans-serif;color:#302626\">" +
            "<div style=\"display:none;max-height:0;overflow:hidden\">Administration pénitentiaire (AP) · SIGPAP-MALI</div>" +
            "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f4f1ef;padding:32px 12px\"><tr><td align=\"center\">" +
            "<table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:600px;width:100%;background:#fff;border-radius:12px;overflow:hidden\">" +
            "<tr><td style=\"background:#4b0d0d;padding:24px 32px;color:#fff\"><div style=\"font-size:12px;letter-spacing:1.5px;text-transform:uppercase;opacity:.85\">Administration pénitentiaire (AP)</div>" +
            "<div style=\"font-size:24px;font-weight:bold;margin-top:6px\">SIGPAP-MALI</div></td></tr>" +
            "<tr><td style=\"height:5px;background:linear-gradient(90deg,#009245 33%,#ffd600 33%,#ffd600 66%,#e30613 66%)\"></td></tr>" +
            "<tr><td style=\"padding:32px\"><h1 style=\"font-size:21px;color:#4b0d0d;margin:0 0 20px\">" +
            titre +
            "</h1>" +
            contenu +
            "</td></tr><tr><td style=\"background:#faf8f7;padding:18px 32px;color:#756969;font-size:12px;line-height:1.6\">" +
            "Ce message a été envoyé automatiquement par SIGPAP-MALI. Merci de ne pas y répondre.<br>Administration pénitentiaire (AP)</td></tr>" +
            "</table></td></tr></table><style>.info{background:#faf8f7;border:1px solid #e8dfdc;border-radius:8px;padding:16px;margin:20px 0}.info div{margin:5px 0}.info span{display:inline-block;width:190px;color:#756969}.info strong{color:#4b0d0d}.secret{font-family:monospace;font-size:16px}.cta a{display:inline-block;background:#8b1e1e;color:#fff!important;padding:12px 20px;border-radius:6px;text-decoration:none;font-weight:bold}</style></body></html>"
        );
    }

    private static String echapper(String valeur) {
        if (valeur == null) return "";
        return valeur
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    public String lienReinitialisation(String jeton) {
        return frontendUrl.replaceAll("/$", "") + "/login?reset=" + jeton;
    }

    private static char choisir(String chars) {
        return chars.charAt(RANDOM.nextInt(chars.length()));
    }
}
