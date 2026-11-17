package ml.apes.rh.situation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import ml.apes.rh.domain.SituationAgent;

public final class Dtos {

    private Dtos() {}

    /** Demande de changement de situation d'un agent (décès, maladie, abandon de poste, retraite, reprise). */
    public record DemandeSituation(
        @NotNull(message = "La situation est obligatoire") SituationAgent situation,
        @NotNull(message = "La date est obligatoire") LocalDate date,
        @Size(max = 500, message = "Le motif ne doit pas dépasser 500 caractères") String motif,
        @Size(max = 200, message = "La référence ne doit pas dépasser 200 caractères")
        String reference,
        @Size(max = 255) String fichier
    ) {}
}
