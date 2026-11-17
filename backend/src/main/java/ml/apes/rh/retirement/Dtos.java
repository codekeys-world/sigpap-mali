package ml.apes.rh.retirement;

import java.time.LocalDate;

public final class Dtos {

    private Dtos() {}

    /** Une ligne du suivi des retraites. etat : RETRAITE, DEPASSEE, A_VENIR ou NON_RENSEIGNE. */
    public record RetraiteLigne(
        String slug,
        String matricule,
        String nom,
        String prenom,
        String photo,
        String grade,
        boolean surveillant,
        String corps,
        String service,
        LocalDate dateNaissance,
        Integer age,
        int ageRetraite,
        LocalDate datePrevue,
        Long moisRestants,
        String etat,
        String situation,
        LocalDate dateRetraite,
        String motifRetraite
    ) {}
}
