package ml.apes.rh.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record AuteurAudit(String matricule, String prenom, String nom) {}

    public record LoginRequest(
        @NotBlank(message = "Le matricule est obligatoire") String matricule,
        @NotBlank(message = "Le mot de passe est obligatoire") String motDePasse,
        boolean seSouvenirDeMoi
    ) {}

    public record LoginResponse(
        String slug,
        String matricule,
        String nom,
        String prenom,
        String photo,
        String role,
        List<String> autorisations,
        boolean motDePasseTemporaire
    ) {}

    public record DemandeReinitialisation(
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 200) String siteWeb
    ) {
        public DemandeReinitialisation(String email) {
            this(email, null);
        }
    }

    public record ReinitialiserMotDePasse(
        @NotBlank String jeton,
        @NotBlank
        @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
        String nouveauMotDePasse,
        @NotBlank String confirmation
    ) {}
}
