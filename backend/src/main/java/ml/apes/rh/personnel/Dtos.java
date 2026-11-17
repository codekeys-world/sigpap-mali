package ml.apes.rh.personnel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record ProfilPersonnel(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @NotNull ml.apes.rh.domain.Sexe sexe,
        @PastOrPresent(message = "La date de naissance ne peut pas être future")
        LocalDate dateNaissance,
        @Size(max = 150) String lieuNaissance,
        @Size(max = 30)
        @Pattern(regexp = "^$|^[+0-9 ().-]{6,30}$", message = "Le numéro de téléphone est invalide")
        String telephone,
        @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$") @Size(max = 150) String email,
        @Size(max = 255) String adresse
    ) {}

    public record ChangerMotDePasse(
        @NotBlank(message = "L'ancien mot de passe est obligatoire") String ancienMotDePasse,
        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
        String nouveauMotDePasse,
        @NotBlank(message = "La confirmation est obligatoire") String confirmation
    ) {}

    /** Un événement de l'historique des mutations d'un agent (alimente la timeline). */
    public record MutationEvent(
        Long id,
        String type,
        String reference,
        String service,
        String typeService,
        String serviceParent,
        String depuis,
        LocalDate dateDebut,
        LocalDate dateFin,
        Integer anneeDebut,
        Integer anneeFin,
        Integer dureeMois,
        String fichierDecision,
        List<String> postes,
        boolean enCours,
        String creePar,
        LocalDateTime creeLe
    ) {}
}
