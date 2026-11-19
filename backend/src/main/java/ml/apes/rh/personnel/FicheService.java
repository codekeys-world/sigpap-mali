package ml.apes.rh.personnel;

import com.lowagie.text.*;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ml.apes.rh.domain.*;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.personnel.Dtos.MutationEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/** A4 career record using the SIGPAP-MALI visual identity. */
@Service
public class FicheService {

    private static final Color BORDEAUX = new Color(75, 13, 13);
    private static final Color BORDEAUX_CLAIR = new Color(139, 30, 30);
    private static final Color VERT = new Color(0, 146, 69);
    private static final Color ROSE = new Color(244, 233, 230);
    private static final Color GRIS = new Color(247, 245, 245);
    private static final Color BORD = new Color(230, 222, 222);
    private static final Color TEXTE = new Color(42, 42, 42);
    private static final DateTimeFormatter FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static Font font(float size, boolean bold, Color color) {
        return FontFactory.getFont(
            bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA,
            size,
            Font.NORMAL,
            color
        );
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "Non renseigné" : value.trim();
    }

    private static String date(LocalDate value) {
        return value == null ? "Non renseignée" : value.format(FR);
    }

    private static String period(
        LocalDate start,
        LocalDate end,
        Integer yearStart,
        Integer yearEnd
    ) {
        String from = start != null ? date(start) : yearStart != null ? yearStart.toString() : "-";
        String to = end != null ? date(end) : yearEnd != null ? yearEnd.toString() : "En cours";
        return from + " - " + to;
    }

    private static String period(Periode value) {
        return period(
            value.getDateDebut(),
            value.getDateFin(),
            value.getAnneeDebut(),
            value.getAnneeFin()
        );
    }

    private static String typeMutation(String type) {
        return switch (type == null ? "" : type) {
            case "PREMIERE_AFFECTATION" -> "Première affectation";
            case "MUTATION" -> "Mutation";
            case "NOMINATION" -> "Nomination";
            case "DETACHEMENT" -> "Détachement";
            case "MISE_A_DISPOSITION" -> "Mise à disposition";
            default -> "Autre";
        };
    }

    private static PdfPCell cell(String text, Font font, Color background, int padding) {
        PdfPCell cell = new PdfPCell(new Phrase(safe(text), font));
        cell.setPadding(padding);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(BORD);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(0.5f);
        cell.setBackgroundColor(background);
        return cell;
    }

    private void addHeader(Document doc) throws DocumentException {
        Font official = FontFactory.getFont(FontFactory.TIMES_BOLD, 11, Font.NORMAL, Color.BLACK);
        PdfPTable header = new PdfPTable(new float[] { 1.15f, 1f });
        header.setWidthPercentage(100);
        header.setKeepTogether(true);

        PdfPCell ministry = new PdfPCell();
        ministry.setBorder(Rectangle.NO_BORDER);
        ministry.setPadding(0);
        ministry.setVerticalAlignment(Element.ALIGN_TOP);
        Paragraph ministryName = new Paragraph(
            "MINISTÈRE DE LA JUSTICE\nET DES DROITS DE L’HOMME", official
        );
        ministryName.setLeading(13);
        ministry.addElement(ministryName);

        Paragraph separator = new Paragraph("********", official);
        separator.setLeading(12);
        separator.setAlignment(Element.ALIGN_CENTER);
        ministry.addElement(separator);

        Paragraph directorate = new Paragraph(
            "DIRECTION GÉNÉRALE DE\nL’ADMINISTRATION PÉNITENTIAIRE", official
        );
        directorate.setLeading(13);
        ministry.addElement(directorate);

        try (var input = new ClassPathResource("logo-print.png").getInputStream()) {
            Image logo = Image.getInstance(input.readAllBytes());
            logo.scaleToFit(56, 66);
            logo.setSpacingBefore(6);
            logo.setAlignment(Element.ALIGN_CENTER);
            ministry.addElement(logo);
        } catch (Exception ignored) {
            Paragraph initials = new Paragraph("AP", font(18, true, Color.BLACK));
            initials.setSpacingBefore(6);
            initials.setAlignment(Element.ALIGN_CENTER);
            ministry.addElement(initials);
        }
        header.addCell(ministry);

        PdfPCell republic = new PdfPCell();
        republic.setBorder(Rectangle.NO_BORDER);
        republic.setPadding(0);
        republic.setVerticalAlignment(Element.ALIGN_TOP);
        Paragraph motto = new Paragraph(
            "RÉPUBLIQUE DU MALI\nUn Peuple – Un But – Une Foi\n********", official
        );
        motto.setLeading(13);
        motto.setAlignment(Element.ALIGN_CENTER);
        republic.addElement(motto);
        header.addCell(republic);

        PdfPCell titleCell = new PdfPCell(
            new Phrase("FICHE INDIVIDUELLE DE CARRIÈRE", font(15, true, Color.BLACK))
        );
        titleCell.setColspan(2);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPaddingTop(8);
        titleCell.setPaddingBottom(2);
        header.addCell(titleCell);
        doc.add(header);
    }

    private void addSection(
        Document doc,
        String title,
        String[] headers,
        float[] widths,
        List<String[]> rows
    ) throws DocumentException {
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(16);
        table.setSplitRows(true);
        table.setSplitLate(false);
        PdfPCell heading = cell(
            title.toUpperCase(Locale.FRENCH),
            font(10, true, BORDEAUX),
            ROSE,
            8
        );
        heading.setColspan(headers.length);
        table.addCell(heading);
        if (rows.isEmpty()) {
            PdfPCell empty = cell(
                "Aucune donnée enregistrée dans cette rubrique.",
                font(9, false, Color.GRAY),
                Color.WHITE,
                9
            );
            empty.setColspan(headers.length);
            table.addCell(empty);
            doc.add(table);
            return;
        }
        for (String header : headers)
            table.addCell(cell(header, font(8, true, BORDEAUX), GRIS, 7));
        for (int i = 0; i < rows.size(); i++) {
            for (String value : rows.get(i)) {
                table.addCell(
                    cell(value, font(8.5f, false, TEXTE), i % 2 == 0 ? Color.WHITE : GRIS, 7)
                );
            }
        }
        table.setHeaderRows(2);
        doc.add(table);
    }

    private void addInformation(Document doc, String title, String[][] fields)
        throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(13);
        table.setKeepTogether(true);
        PdfPCell headingCell = cell(
            title.toUpperCase(Locale.FRENCH), font(10, true, BORDEAUX), ROSE, 8
        );
        headingCell.setColspan(2);
        table.addCell(headingCell);
        for (String[] field : fields) {
            PdfPCell box = new PdfPCell();
            box.setBorderColor(BORD);
            box.setBorder(Rectangle.BOTTOM);
            box.setBorderWidth(0.5f);
            box.setPadding(8);
            box.setPaddingTop(6);
            box.setPaddingBottom(6);
            Paragraph label = new Paragraph(field[0], font(8, false, BORDEAUX_CLAIR));
            label.setLeading(10);
            box.addElement(label);
            Paragraph value = new Paragraph(safe(field[1]), font(9, true, TEXTE));
            value.setLeading(11);
            value.setSpacingBefore(2);
            box.addElement(value);
            table.addCell(box);
        }
        table.completeRow();
        doc.add(table);
    }

    public byte[] generer(
        Personnel p,
        byte[] photo,
        List<HistoriqueGrade> grades,
        List<MutationEvent> mutations,
        List<FaireFormation> formations,
        List<FaireEtude> etudes,
        List<SanctionPersonnel> sanctions,
        List<CongeAbsencePersonnel> conges,
        List<DistinctionPersonnel> distinctions,
        String generePar
    ) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 40, 48);
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            String edition = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            writer.setPageEvent(
                new PdfPageEventHelper() {
                    @Override
                    public void onEndPage(PdfWriter w, Document document) {
                        var canvas = w.getDirectContent();
                        canvas.setColorStroke(BORD);
                        canvas.moveTo(36, 36);
                        canvas.lineTo(PageSize.A4.getWidth() - 36, 36);
                        canvas.stroke();
                        ColumnText.showTextAligned(
                            canvas,
                            Element.ALIGN_LEFT,
                            new Phrase(
                                "SIGPAP-MALI | " +
                                    safe(p.getMatricule()) +
                                    " | Édité le " +
                                    edition,
                                font(7, false, Color.GRAY)
                            ),
                            36,
                            24,
                            0
                        );
                        ColumnText.showTextAligned(
                            canvas,
                            Element.ALIGN_RIGHT,
                            new Phrase("Page " + w.getPageNumber(), font(8, true, BORDEAUX)),
                            PageSize.A4.getWidth() - 36,
                            24,
                            0
                        );
                        if (w.getPageNumber() > 1) ColumnText.showTextAligned(
                            canvas,
                            Element.ALIGN_LEFT,
                            new Phrase(
                                "Fiche de carrière | " +
                                    safe(p.getPrenom()) +
                                    " " +
                                    safe(p.getNom()),
                                font(8, true, BORDEAUX)
                            ),
                            36,
                            PageSize.A4.getHeight() - 24,
                            0
                        );
                    }
                }
            );
            doc.open();
            doc.addCreator("SIGPAP-MALI - AP");
            doc.addTitle("Fiche individuelle de carrière - " + p.getMatricule());
            doc.addAuthor("Administration pénitentiaire");
            addHeader(doc);

            Paragraph identityTitle = new Paragraph(
                "IDENTITÉ DE L’AGENT",
                font(10, true, BORDEAUX)
            );
            identityTitle.setSpacingBefore(16);
            identityTitle.setSpacingAfter(7);
            doc.add(identityTitle);

            PdfPTable identity = new PdfPTable(new float[] { 1.35f, 5.65f });
            identity.setWidthPercentage(100);
            PdfPCell photoCell = new PdfPCell();
            photoCell.setBorderColor(BORD);
            photoCell.setBackgroundColor(ROSE);
            photoCell.setPadding(10);
            photoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            boolean hasPhoto = false;
            if (photo != null) {
                try {
                    Image portrait = Image.getInstance(photo);
                    portrait.scaleToFit(78, 104);
                    portrait.setAlignment(Element.ALIGN_CENTER);
                    photoCell.addElement(portrait);
                    hasPhoto = true;
                } catch (Exception ignored) {}
            }
            if (!hasPhoto) {
                String first =
                    p.getPrenom() == null || p.getPrenom().isBlank()
                        ? ""
                        : p.getPrenom().substring(0, 1);
                String last =
                    p.getNom() == null || p.getNom().isBlank() ? "" : p.getNom().substring(0, 1);
                Paragraph initials = new Paragraph(
                    (first + last).toUpperCase(Locale.FRENCH),
                    font(28, true, BORDEAUX)
                );
                initials.setAlignment(Element.ALIGN_CENTER);
                photoCell.addElement(initials);
            }
            identity.addCell(photoCell);

            PdfPCell summary = new PdfPCell();
            summary.setBorderColor(BORD);
            summary.setBorder(Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM);
            summary.setBackgroundColor(GRIS);
            summary.setPadding(14);
            summary.setVerticalAlignment(Element.ALIGN_MIDDLE);
            summary.addElement(
                new Paragraph(
                    safe(p.getPrenom()) + " " + safe(p.getNom()).toUpperCase(Locale.FRENCH),
                    font(19, true, BORDEAUX)
                )
            );
            Paragraph matricule = new Paragraph(
                "Matricule : " + safe(p.getMatricule()), font(10, true, TEXTE)
            );
            matricule.setSpacingBefore(4);
            summary.addElement(matricule);
            summary.addElement(
                new Paragraph(
                    p.isSurveillant() ? "Personnel surveillant" : "Personnel non surveillant",
                    font(10, false, TEXTE)
                )
            );
            Paragraph situation = new Paragraph(
                p.getSituation().libelle(),
                font(11, true, p.getSituation() == SituationAgent.ACTIF ? VERT : BORDEAUX_CLAIR)
            );
            situation.setSpacingBefore(5);
            summary.addElement(situation);
            identity.addCell(summary);
            doc.add(identity);
            addInformation(doc, "Identité et coordonnées", new String[][] {
                { "Date de naissance", date(p.getDateNaissance()) },
                { "Lieu de naissance", p.getLieuNaissance() },
                {
                    "Sexe",
                    p.getSexe() == null ? null : p.getSexe() == Sexe.M ? "Masculin" : "Féminin",
                },
                { "Groupe sanguin", p.getGroupeSanguin() },
                { "Téléphone", p.getTelephone() },
                { "Adresse e-mail", p.getEmail() },
                { "Adresse", p.getAdresse() },
                { "Contact d’urgence", p.getContactUrgence() },
            });
            var corps =
                p.getCorpsActuel() != null
                    ? p.getCorpsActuel()
                    : p.getGradeActuel() != null
                      ? p.getGradeActuel().getCorps()
                      : null;
            addInformation(doc, "Situation administrative actuelle", new String[][] {
                { "Corps", corps == null ? null : corps.getNom() },
                {
                    "Grade",
                    !p.isSurveillant()
                        ? "Sans grade"
                        : p.getGradeActuel() == null
                          ? null
                          : p.getGradeActuel().getNom(),
                },
                {
                    "Service actuel",
                    p.getServiceCourant() == null ? null : p.getServiceCourant().getNom(),
                },
                { "Fonction actuelle", p.getFonction() },
                { "Date de recrutement", date(p.getDateRecrutement()) },
                { "Date de titularisation", date(p.getDateTitularisation()) },
                {
                    "Arrêté d’intégration",
                    p.getArreteIntegration() == null
                        ? null
                        : p.getArreteIntegration().getReference(),
                },
                { "Formation en cours", p.isEnFormation() ? "Oui" : "Non" },
                { "Situation depuis le", date(p.getSituationDepuis()) },
                { "Motif de la situation", p.getSituationMotif() },
            });
            Paragraph source = new Paragraph(
                "Fiche établie à partir des informations enregistrées dans SIGPAP-MALI.\nÉditée par " +
                    safe(generePar) +
                    ".",
                font(8, false, Color.GRAY)
            );
            source.setSpacingBefore(12);
            doc.add(source);
            doc.newPage();

            List<String[]> gradeRows = new ArrayList<>();
            grades.forEach(x ->
                gradeRows.add(new String[] {
                    period(x),
                    x.getGrade().getNom(),
                    x.getGrade().getCorps().getNom(),
                })
            );
            if (p.isSurveillant() || !gradeRows.isEmpty()) addSection(
                doc,
                "Historique des grades",
                new String[] { "Période", "Grade", "Corps" },
                new float[] { 2.2f, 2.4f, 2.4f },
                gradeRows
            );

            List<String[]> mutationRows = new ArrayList<>();
            mutations.forEach(x ->
                mutationRows.add(new String[] {
                    period(x.dateDebut(), x.dateFin(), x.anneeDebut(), x.anneeFin()),
                    typeMutation(x.type()) + " : " + x.reference(),
                    (x.depuis() != null ? x.depuis() + " - " : "") + x.service(),
                    x.postes().isEmpty() ? "-" : String.join(", ", x.postes()),
                })
            );
            addSection(
                doc,
                "Historique des mutations",
                new String[] { "Période", "Mutation", "Service", "Postes occupés" },
                new float[] { 2f, 2.6f, 2.8f, 2f },
                mutationRows
            );

            List<String[]> formationRows = new ArrayList<>();
            formations.forEach(x ->
                formationRows.add(new String[] {
                    period(x),
                    x.getFormation().getTitre(),
                    safe(x.getLieu()) +
                        (x.getDescription() == null ? "" : "\n" + x.getDescription()),
                })
            );
            addSection(
                doc,
                "Formations suivies",
                new String[] { "Période", "Formation", "Lieu" },
                new float[] { 2.2f, 3.8f, 2f },
                formationRows
            );

            List<String[]> studyRows = new ArrayList<>();
            etudes.forEach(x ->
                studyRows.add(new String[] {
                    period(x),
                    x.getEtude().getIntitule(),
                    x.getEtude().getNiveauEtude(),
                    safe(x.getLieu()) +
                        (x.getOption() == null ? "" : "\nOption : " + x.getOption()) +
                        (x.getDescription() == null ? "" : "\n" + x.getDescription()),
                })
            );
            addSection(
                doc,
                "Études suivies",
                new String[] { "Période", "Intitulé", "Niveau", "Lieu" },
                new float[] { 2f, 3.2f, 1.5f, 1.8f },
                studyRows
            );

            List<String[]> sanctionRows = new ArrayList<>();
            sanctions.forEach(x ->
                sanctionRows.add(new String[] {
                    date(x.getDateSanction()),
                    x.getNature(),
                    x.getMotif(),
                    safe(x.getReference()),
                })
            );
            addSection(
                doc,
                "Sanctions disciplinaires",
                new String[] { "Date", "Nature", "Motif", "Référence" },
                new float[] { 1.5f, 2f, 3.4f, 2.1f },
                sanctionRows
            );

            List<String[]> congeRows = new ArrayList<>();
            conges.forEach(x ->
                congeRows.add(new String[] {
                    date(x.getDateDebut()) +
                        " au " +
                        (x.getDateFin() == null ? "en cours" : date(x.getDateFin())),
                    x.getType() == TypeCongeAbsence.CONGE ? "Congé" : "Absence",
                    x.getNature() +
                        (x.getDureeJours() == null
                            ? ""
                            : "\n" + x.getDureeJours() + " jour(s) calendaires") +
                        (x.getObservations() == null ? "" : "\n" + x.getObservations()),
                    safe(x.getReference()),
                })
            );
            addSection(
                doc,
                "Congés et absences",
                new String[] { "Période", "Type", "Nature", "Référence" },
                new float[] { 2.5f, 1.3f, 3f, 2.2f },
                congeRows
            );
            List<String[]> distinctionRows = new ArrayList<>();
            distinctions.forEach(x ->
                distinctionRows.add(new String[] {
                    date(x.getDateAttribution()),
                    x.getIntitule() + (x.getMotif() == null ? "" : "\n" + x.getMotif()),
                    safe(x.getAutorite()),
                    safe(x.getReference()),
                })
            );
            addSection(
                doc,
                "Distinctions",
                new String[] { "Date", "Intitulé", "Autorité", "Référence" },
                new float[] { 1.5f, 3f, 2.5f, 2f },
                distinctionRows
            );
            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new BusinessException(
                "La fiche de carrière ne peut pas être générée. Réessayez dans quelques instants."
            );
        }
    }
}
