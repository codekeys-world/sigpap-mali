package ml.apes.rh.personnel;

import static org.junit.jupiter.api.Assertions.*;

import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ml.apes.rh.domain.*;
import org.junit.jupiter.api.Test;

class FicheServiceTest {

    private Personnel agent() {
        Personnel p = new Personnel();
        p.setPrenom("Aminata");
        p.setNom("Traoré");
        p.setMatricule("AP-TEST-001");
        p.setSexe(Sexe.F);
        p.setSurveillant(false);
        p.setSituation(SituationAgent.MALADE);
        p.setTelephone("+223 76 12 34 56");
        p.setEmail("aminata@example.test");
        p.setDateNaissance(LocalDate.of(1986, 3, 14));
        p.setLieuNaissance("Bamako");
        p.setAdresse("Bamako, quartier du centre administratif");
        p.setContactUrgence("Moussa Traoré — +223 70 11 22 33");
        p.setDateRecrutement(LocalDate.of(2012, 1, 2));
        p.setSituationDepuis(LocalDate.of(2026, 11, 1));
        p.setSituationMotif("Repos médical prescrit");
        ServiceAp service = new ServiceAp();
        service.setNom("Direction des ressources humaines et de la formation");
        p.setServiceCourant(service);
        Corps corps = new Corps();
        corps.setNom("Administration générale");
        p.setCorpsActuel(corps);
        p.setFonction("Responsable du suivi administratif des agents");
        return p;
    }

    private String allText(PdfReader reader) throws Exception {
        StringBuilder text = new StringBuilder();
        var extractor = new PdfTextExtractor(reader);
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            String page = extractor.getTextFromPage(i);
            assertTrue(page.contains("Page " + i), page);
            text.append(page).append(' ');
        }
        return text.toString().replaceAll("\\s+", " ");
    }

    @Test
    void recordIncludesLogoIdentityAndCompleteHistoriesWithPageNumbers() throws Exception {
        CongeAbsencePersonnel conge = new CongeAbsencePersonnel();
        conge.setType(TypeCongeAbsence.CONGE);
        conge.setNature("Congé annuel");
        conge.setDateDebut(LocalDate.of(2026, 10, 1));
        conge.setDateFin(LocalDate.of(2026, 10, 5));
        conge.setObservations("Reprise du service le 6 octobre");
        DistinctionPersonnel distinction = new DistinctionPersonnel();
        distinction.setIntitule("Médaille du mérite");
        distinction.setDateAttribution(LocalDate.of(2025, 6, 1));
        distinction.setMotif("Qualité du service rendu");
        distinction.setAutorite("Ministère de la Justice");
        byte[] pdf = new FicheService().generer(
            agent(),
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(conge),
            List.of(distinction),
            "Gestionnaire RH (RH-001)"
        );
        Files.createDirectories(Path.of("target/fiche-preview"));
        Files.write(Path.of("target/fiche-preview/fiche-agent.pdf"), pdf);
        PdfReader reader = new PdfReader(pdf);
        try {
            assertEquals(2, reader.getNumberOfPages());
            String firstPage = new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(firstPage.contains("MINISTÈRE DE LA JUSTICE"), firstPage);
            assertTrue(firstPage.contains("RÉPUBLIQUE DU MALI"), firstPage);
            String text = allText(reader);
            assertTrue(text.contains("Aminata TRAORÉ"), text);
            assertTrue(text.contains("Personnel non surveillant"), text);
            assertTrue(text.contains("Sans grade"), text);
            assertTrue(text.contains("5 jour(s) calendaires"), text);
            assertTrue(text.contains("Reprise du service le 6 octobre"), text);
            assertTrue(text.contains("Qualité du service rendu"), text);
            assertTrue(text.contains("Gestionnaire RH (RH-001)"), text);
            assertFalse(text.contains("HISTORIQUE DES GRADES"), text);
            var images = reader.getPageN(1).getAsDict(PdfName.RESOURCES).getAsDict(PdfName.XOBJECT);
            assertNotNull(images, "Le logo doit être incorporé au PDF");
            assertFalse(images.getKeys().isEmpty());
        } finally {
            reader.close();
        }
    }

    @Test
    void longHistoryKeepsItsLastEntryAndRepeatsTheTableHeaders() throws Exception {
        List<DistinctionPersonnel> distinctions = new ArrayList<>();
        for (int i = 1; i <= 65; i++) {
            DistinctionPersonnel d = new DistinctionPersonnel();
            d.setIntitule("Distinction numéro " + i);
            d.setDateAttribution(LocalDate.of(2025, 1, 1));
            d.setMotif(
                "Reconnaissance pour la qualité du travail et le suivi des dossiers administratifs des agents."
            );
            distinctions.add(d);
        }
        byte[] pdf = new FicheService().generer(
            agent(),
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            distinctions,
            "RH-001"
        );
        Files.createDirectories(Path.of("target/fiche-preview"));
        Files.write(Path.of("target/fiche-preview/fiche-longue.pdf"), pdf);
        PdfReader reader = new PdfReader(pdf);
        try {
            assertTrue(reader.getNumberOfPages() > 3);
            String text = allText(reader);
            assertTrue(text.contains("Distinction numéro 65"), text);
            assertTrue(
                new PdfTextExtractor(reader)
                    .getTextFromPage(reader.getNumberOfPages())
                    .contains("DISTINCTIONS")
            );
        } finally {
            reader.close();
        }
    }
}
