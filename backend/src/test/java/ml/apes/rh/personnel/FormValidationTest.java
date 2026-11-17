package ml.apes.rh.personnel;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import java.time.LocalDate;
import ml.apes.rh.auth.Dtos.DemandeReinitialisation;
import ml.apes.rh.domain.*;
import ml.apes.rh.exception.BusinessException;
import org.junit.jupiter.api.Test;

class FormValidationTest {

    @Test
    void invalidIdentityAndEmailAreRejectedBeforePersistence() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var profile = new Dtos.ProfilPersonnel(
                " ",
                "Aminata",
                Sexe.F,
                LocalDate.now().plusDays(1),
                null,
                "not-a-phone",
                "bad-email",
                null
            );
            var fields = validator
                .validate(profile)
                .stream()
                .map(v -> v.getPropertyPath().toString())
                .toList();
            assertTrue(
                fields.containsAll(java.util.List.of("nom", "dateNaissance", "telephone", "email"))
            );
            assertFalse(validator.validate(new DemandeReinitialisation("not-an-email")).isEmpty());
            assertTrue(
                validator.validate(new DemandeReinitialisation("agent@example.ml")).isEmpty()
            );
            Corps corps = new Corps();
            corps.setNom("x".repeat(151));
            assertFalse(validator.validate(corps).isEmpty());
        }
    }

    @Test
    void historyRequiresAStartAndAcceptsOnlyCoherentMixedDateAndYearRanges() {
        FaireFormation history = new FaireFormation();
        assertThrows(BusinessException.class, history::verifierPeriode);
        history.setAnneeDebut(2026);
        history.setDateFin(LocalDate.of(2025, 12, 1));
        assertThrows(BusinessException.class, history::verifierPeriode);
        history.setDateFin(LocalDate.of(2026, 12, 1));
        assertDoesNotThrow(history::verifierPeriode);
        history.setAnneeDebut(2024);
        history.setDateFin(null);
        assertDoesNotThrow(history::verifierPeriode);
    }
}
