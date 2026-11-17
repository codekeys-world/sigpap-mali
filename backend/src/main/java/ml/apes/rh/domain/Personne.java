package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "personne")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Personne extends BaseEntity {

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false, length = 100)
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false, length = 100)
    @Size(max = 100)
    private String prenom;

    @NotNull(message = "Le sexe est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Sexe sexe;

    @PastOrPresent(message = "La date de naissance ne peut pas être future")
    private LocalDate dateNaissance;

    @Column(length = 150)
    @Size(max = 150)
    private String lieuNaissance;

    @Column(length = 30)
    @Pattern(regexp = "^$|^[+0-9 ().-]{6,30}$", message = "Le numéro de téléphone est invalide")
    @Size(max = 30)
    private String telephone;

    /** Groupe sanguin (A+, A-, B+, B-, AB+, AB-, O+, O-). Affiché sur la carte de service. */
    @Column(length = 5)
    @Size(max = 5)
    private String groupeSanguin;

    @Column(length = 150)
    @Size(max = 150)
    private String contactUrgence;

    @Email(message = "L'adresse e-mail est invalide")
    @Column(length = 150)
    @Size(max = 150)
    private String email;

    @Column(length = 255)
    @Size(max = 255)
    private String adresse;
}
