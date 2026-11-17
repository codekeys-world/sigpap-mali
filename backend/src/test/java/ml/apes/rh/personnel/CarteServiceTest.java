package ml.apes.rh.personnel;

import static org.junit.jupiter.api.Assertions.*;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import ml.apes.rh.domain.Corps;
import ml.apes.rh.domain.Grade;
import ml.apes.rh.domain.Personnel;
import org.junit.jupiter.api.Test;

class CarteServiceTest {

    private Personnel agent(boolean surveillant) {
        Personnel agent = new Personnel();
        agent.setSlug("agent-test");
        agent.setMatricule("AGENT-TEST");
        agent.setPrenom("Aminata");
        agent.setNom("Traore");
        agent.setSurveillant(surveillant);
        Corps corps = new Corps();
        corps.setNom("Corps administratif");
        agent.setCorpsActuel(corps);
        Grade grade = new Grade();
        grade.setNom("Inspecteur");
        grade.setCorps(corps);
        agent.setGradeActuel(grade);
        return agent;
    }

    private String recto(Personnel agent) throws Exception {
        byte[] pdf = new CarteService().generer(agent, null, null, "Le Directeur");
        PdfReader reader = new PdfReader(pdf);
        try {
            assertEquals(2, reader.getNumberOfPages());
            return new PdfTextExtractor(reader).getTextFromPage(1).replaceAll("\\s+", " ");
        } finally {
            reader.close();
        }
    }

    @Test
    void surveillanceCardShowsGradeWithoutCorpsAndFirstNameBeforeLastName() throws Exception {
        String text = recto(agent(true));
        assertTrue(text.contains("Aminata TRAORE"), text);
        assertTrue(text.contains("GRADE"), text);
        assertTrue(text.contains("Inspecteur"), text);
        assertFalse(text.contains("CORPS"), text);
        assertFalse(text.contains("Corps administratif"), text);
    }

    @Test
    void backIncludesTheFullPassageNoticeWithoutTruncation() throws Exception {
        byte[] pdf = new CarteService().generer(agent(true), null, null, "Le Directeur");
        PdfReader reader = new PdfReader(pdf);
        try {
            String text = new PdfTextExtractor(reader).getTextFromPage(2).replaceAll("\\s+", " ");
            assertTrue(text.contains("laisser librement circuler"), text);
            assertTrue(text.contains("protections nécessaires"), text);
            assertTrue(text.contains("présente carte en cas de besoin."), text);
            assertTrue(text.contains("service émetteur."), text);
        } finally {
            reader.close();
        }
    }

    @Test
    void nonSurveillanceCardKeepsCorpsWithoutGrade() throws Exception {
        String text = recto(agent(false));
        assertTrue(text.contains("CORPS"), text);
        assertTrue(text.contains("Corps administratif"), text);
        assertFalse(text.contains("GRADE"), text);
    }
}
