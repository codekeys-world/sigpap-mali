package ml.apes.rh.personnel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.exception.BusinessException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** PVC CR80 landscape service card, two pages for front and back. */
@Service
public class CarteService {

    private static final Color BORDEAUX = new Color(75, 13, 13);
    private static final Color ROUGE = new Color(227, 6, 19);
    private static final Color VERT = new Color(0, 146, 69);
    private static final Color JAUNE = new Color(255, 214, 0);
    private static final Color TEXTE = new Color(42, 42, 42);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private void texte(PdfContentByte cb, String text, float x, float y, Font font) {
        ColumnText.showTextAligned(
            cb,
            Element.ALIGN_LEFT,
            new Phrase(text == null ? "" : text, font),
            x,
            y,
            0
        );
    }

    private void centre(PdfContentByte cb, String text, float x, float y, Font font) {
        ColumnText.showTextAligned(
            cb,
            Element.ALIGN_CENTER,
            new Phrase(text == null ? "" : text, font),
            x,
            y,
            0
        );
    }

    private void texteBoite(
        PdfContentByte cb,
        String text,
        float x,
        float y,
        float width,
        float height,
        Font font
    ) throws DocumentException {
        ColumnText ct = new ColumnText(cb);
        ct.setSimpleColumn(
            new Phrase(text == null ? "" : text, font),
            x,
            y,
            x + width,
            y + height,
            font.getSize() + 1,
            Element.ALIGN_LEFT
        );
        ct.go();
    }

    public byte[] generer(Personnel p, byte[] photo, byte[] signature, String nomDirecteur) {
        float w = (85.6f * 72f) / 25.4f;
        float h = (54f * 72f) / 25.4f;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(new Rectangle(w, h), 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            doc.open();
            doc.addCreator("SIGPAP-MALI - AP");
            PdfContentByte cb = writer.getDirectContent();
            fond(cb, w, h);
            dessinerDrapeau(cb, w - 83, h - 15, 72, 12);
            texte(
                cb,
                "R\u00C9PUBLIQUE DU MALI",
                10,
                h - 13,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, BORDEAUX)
            );
            texte(
                cb,
                "UN PEUPLE - UN BUT - UNE FOI",
                10,
                h - 23,
                FontFactory.getFont(FontFactory.HELVETICA, 5.4f, Font.NORMAL, TEXTE)
            );
            ajouterLogo(cb, w - 37, h - 36, 27, 27);

            cb.setColorFill(BORDEAUX);
            cb.rectangle(0, h - 48, w, 17);
            cb.fill();
            texte(
                cb,
                "ADMINISTRATION P\u00C9NITENTIAIRE",
                10,
                h - 42,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, Color.WHITE)
            );
            cb.setColorFill(ROUGE);
            cb.rectangle(0, h - 50, w, 2);
            cb.fill();

            boolean photoOk = false;
            if (photo != null) {
                try {
                    Image img = Image.getInstance(photo);
                    img.scaleAbsolute(66, 76);
                    img.setAbsolutePosition(10, 31);
                    cb.addImage(img);
                    photoOk = true;
                } catch (Exception ignored) {}
            }
            if (!photoOk) {
                cb.setColorFill(new Color(239, 239, 239));
                cb.roundRectangle(10, 31, 66, 76, 4);
                cb.fill();
                String first =
                    p.getPrenom() == null || p.getPrenom().isBlank()
                        ? ""
                        : p.getPrenom().substring(0, 1);
                String last =
                    p.getNom() == null || p.getNom().isBlank() ? "" : p.getNom().substring(0, 1);
                centre(
                    cb,
                    (first + last).toUpperCase(),
                    43,
                    63,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.NORMAL, BORDEAUX)
                );
            }

            float x = 85;
            Font label = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                5.2f,
                Font.NORMAL,
                BORDEAUX
            );
            Font value = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.4f, Font.NORMAL, TEXTE);
            texteBoite(
                cb,
                safe(p.getPrenom()) + " " + safe(p.getNom()).toUpperCase(),
                x,
                87,
                112,
                15,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Font.NORMAL, BORDEAUX)
            );
            texte(cb, "MATRICULE", x, 73, label);
            texteBoite(cb, limiter(p.getMatricule(), 17), x, 64, 55, 9, value);
            texte(cb, "DATE DE NAISSANCE", x, 55, label);
            texte(
                cb,
                safe(p.getDateNaissance() == null ? null : p.getDateNaissance().format(DATE)),
                x,
                46,
                value
            );
            texte(cb, "GROUPE SANGUIN", 146, 55, label);
            texte(cb, safe(p.getGroupeSanguin()), 146, 46, value);

            if (p.isSurveillant()) {
                texte(cb, "GRADE", x, 37, label);
                String grade =
                    p.getGradeActuel() != null ? p.getGradeActuel().getNom() : "Non renseigné";
                texteBoite(cb, limiter(grade, 35), x, 28, 135, 9, value);
            } else {
                texte(cb, "CORPS", x, 37, label);
                String corps =
                    p.getCorpsActuel() != null
                        ? p.getCorpsActuel().getNom()
                        : p.getGradeActuel() != null && p.getGradeActuel().getCorps() != null
                          ? p.getGradeActuel().getCorps().getNom()
                          : "Non renseigné";
                texteBoite(cb, limiter(corps, 35), x, 28, 135, 9, value);
            }
            texte(cb, "FONCTION", x, 19, label);
            texteBoite(cb, limiter(p.getFonction(), 20), x, 10, 55, 9, value);
            texte(cb, "SERVICE", 146, 19, label);
            String service =
                p.getServiceCourant() != null ? p.getServiceCourant().getNom() : "Non renseigné";
            texteBoite(cb, limiter(service, 20), 146, 10, 55, 9, value);
            texte(
                cb,
                "Delivree le " + LocalDate.now().format(DATE),
                65,
                5,
                FontFactory.getFont(FontFactory.HELVETICA, 4.8f, Font.NORMAL, TEXTE)
            );
            texte(
                cb,
                "N° " + safe(p.getNumeroCarte()),
                85,
                5,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 4, Font.NORMAL, BORDEAUX)
            );
            dessinerSignature(cb, signature, nomDirecteur, 10, 4, 50, 9);

            doc.newPage();
            cb = writer.getDirectContent();
            fond(cb, w, h);
            cb.setColorFill(BORDEAUX);
            cb.rectangle(0, h - 24, w, 24);
            cb.fill();
            dessinerDrapeau(cb, 10, h - 17, 42, 8);
            centre(
                cb,
                "ADMINISTRATION P\u00C9NITENTIAIRE",
                w / 2 + 25,
                h - 15,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Font.NORMAL, Color.WHITE)
            );
            ajouterLogo(cb, w - 37, h - 40, 25, 25);
            Image qr = Image.getInstance(genererQr(p.getNumeroCarte()));
            qr.scaleAbsolute(54, 54);
            qr.setAbsolutePosition(6, 49);
            cb.addImage(qr);
            texte(cb, "N° CARTE", 6, 41, label);
            texteBoite(
                cb,
                safe(p.getNumeroCarte()),
                6,
                32,
                54,
                8,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 4, Font.NORMAL, BORDEAUX)
            );
            texte(cb, "CONTACT D'URGENCE", 75, 96, label);
            texteBoite(cb, safe(p.getContactUrgence()), 75, 80, 145, 15, value);
            texteBoite(
                cb,
                "Cette carte est nominative et demeure la propriété de l'Administration Pénitentiaire. " +
                    "Elle doit être présentée à toute réquisition. En cas de perte, la remettre au service émetteur.",
                75,
                62,
                145,
                27,
                FontFactory.getFont(FontFactory.HELVETICA, 5.4f, Font.NORMAL, TEXTE)
            );
            texteBoite(
                cb,
                "Les autorités, tant civiles que militaires, sont priées de laisser librement circuler " +
                    "et d'apporter toutes assistances et protections nécessaires au porteur de la présente carte en cas de besoin.",
                75,
                20,
                155,
                40,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.2f, Font.NORMAL, TEXTE)
            );
            cb.setColorFill(VERT);
            cb.rectangle(0, 0, w / 3, 4);
            cb.fill();
            cb.setColorFill(JAUNE);
            cb.rectangle(w / 3, 0, w / 3, 4);
            cb.fill();
            cb.setColorFill(ROUGE);
            cb.rectangle((2 * w) / 3, 0, w / 3, 4);
            cb.fill();
            centre(
                cb,
                safe(p.getMatricule()) + " · " + LocalDate.now().format(DATE),
                w / 2,
                12,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, Font.NORMAL, BORDEAUX)
            );
            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException(
                "La carte de service ne peut pas être générée. Réessayez dans quelques instants."
            );
        }
    }

    public byte[] genererQr(String contenu) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(contenu, BarcodeFormat.QR_CODE, 240, 240);
            BufferedImage image = new BufferedImage(
                matrix.getWidth(),
                matrix.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );
            for (int y = 0; y < matrix.getHeight(); y++) {
                for (int x = 0; x < matrix.getWidth(); x++) {
                    image.setRGB(
                        x,
                        y,
                        matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB()
                    );
                }
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                if (!ImageIO.write(image, "PNG", out)) throw new BusinessException(
                    "Impossible de générer le QR code."
                );
                return out.toByteArray();
            }
        } catch (WriterException | IOException ex) {
            throw new BusinessException("Impossible de générer le QR code de la carte.");
        }
    }

    private void dessinerSignature(
        PdfContentByte cb,
        byte[] bytes,
        String nom,
        float x,
        float y,
        float width,
        float height
    ) throws Exception {
        if (bytes != null) {
            Image image = Image.getInstance(bytes);
            image.scaleToFit(width, height);
            image.setAbsolutePosition(x, y + 5);
            cb.addImage(image);
        } else {
            cb.setColorStroke(new Color(160, 160, 160));
            cb.moveTo(x, y + 5);
            cb.lineTo(x + width, y + 5);
            cb.stroke();
        }
        ColumnText texteNom = new ColumnText(cb);
        texteNom.setSimpleColumn(
            new Phrase(
                safe(nom),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 4.5f, Font.NORMAL, BORDEAUX)
            ),
            x,
            y - 2,
            x + width,
            y + 4,
            5,
            Element.ALIGN_CENTER
        );
        texteNom.go();
    }

    private static void fond(PdfContentByte cb, float w, float h) {
        cb.setColorFill(Color.WHITE);
        cb.rectangle(0, 0, w, h);
        cb.fill();
        cb.setColorStroke(new Color(220, 220, 220));
        cb.rectangle(.5f, .5f, w - 1, h - 1);
        cb.stroke();
    }

    private static void dessinerDrapeau(PdfContentByte cb, float x, float y, float w, float h) {
        cb.setColorFill(VERT);
        cb.rectangle(x, y, w / 3, h);
        cb.fill();
        cb.setColorFill(JAUNE);
        cb.rectangle(x + w / 3, y, w / 3, h);
        cb.fill();
        cb.setColorFill(ROUGE);
        cb.rectangle(x + (2 * w) / 3, y, w / 3, h);
        cb.fill();
    }

    private void ajouterLogo(PdfContentByte cb, float x, float y, float width, float height)
        throws Exception {
        Image logo = Image.getInstance(
            new ClassPathResource("logo-print.png").getInputStream().readAllBytes()
        );
        logo.scaleToFit(width, height);
        logo.setAbsolutePosition(x, y);
        cb.addImage(logo);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String limiter(String value, int max) {
        String text = safe(value);
        return text.length() > max ? text.substring(0, max - 1) + "..." : text;
    }
}
