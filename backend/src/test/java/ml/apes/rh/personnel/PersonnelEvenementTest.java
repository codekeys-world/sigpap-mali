package ml.apes.rh.personnel;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import ml.apes.rh.domain.CongeAbsencePersonnel;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.security.Acces;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class PersonnelEvenementTest {

    @Test
    void durationIncludesBothDatesAndSupportsAnUnknownEnd() {
        CongeAbsencePersonnel conge = new CongeAbsencePersonnel();
        conge.setDateDebut(LocalDate.of(2026, 12, 30));
        assertDoesNotThrow(conge::verifierDates);
        assertNull(conge.getDureeJours());
        conge.setDateFin(LocalDate.of(2027, 1, 2));
        assertEquals(4L, conge.getDureeJours());
        conge.setDateFin(conge.getDateDebut());
        assertEquals(1L, conge.getDureeJours());
    }

    @Test
    void missingStartAndReversedDatesAreRejected() {
        CongeAbsencePersonnel conge = new CongeAbsencePersonnel();
        assertThrows(BusinessException.class, conge::verifierDates);
        conge.setDateDebut(LocalDate.of(2026, 11, 14));
        conge.setDateFin(LocalDate.of(2026, 11, 13));
        assertThrows(BusinessException.class, conge::verifierDates);
    }

    @Test
    void bothHistoriesAreReadableButEveryWriteRequiresAManagementRole() {
        for (var method : PersonnelEvenementController.class.getDeclaredMethods()) {
            String permission = method.getAnnotation(PreAuthorize.class).value();
            assertEquals(
                List.of("conges", "distinctions").contains(method.getName())
                    ? Acces.TOUS
                    : Acces.GESTIONNAIRES,
                permission,
                method.getName()
            );
        }
    }
}
