package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

/** Un personnel hérite de toutes les informations d'une personne. Il peut se connecter (matricule + mot de passe). */
@Entity
@Table(name = "personnel")
@Getter
@Setter
public class Personnel extends Personne {

    @NotBlank(message = "Le matricule est obligatoire")
    @Column(nullable = false, unique = true, length = 50)
    @Size(max = 50)
    private String matricule;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String motDePasse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    /** Grade courant (dérivé de l'historique de port de grade). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grade_actuel_id")
    private Grade gradeActuel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "corps_actuel_id")
    private Corps corpsActuel;

    /** Service de l'affectation courante : sert au périmètre des directeurs régionaux / chefs d'établissement. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_courant_id")
    private ServiceAp serviceCourant;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "arrete_integration_id")
    private ArreteIntegration arreteIntegration;

    private boolean surveillant = true;
    private boolean enFormation = false;
    private boolean decede = false;
    private LocalDate dateDeces;
    private LocalDate dateRecrutement;
    private LocalDate dateTitularisation;
    private boolean actif = true;

    /** Nom du fichier de la photo d'identité (stockée via FileStorageService). */
    @Column(length = 255)
    @Size(max = 255)
    private String photo;

    @Transient
    private String fonction;

    @Transient
    public String getNumeroCarte() {
        return getSlug() == null ? null : "AP-" + getSlug().toUpperCase(Locale.ROOT);
    }

    /** Situation administrative courante (null = ancien enregistrement : déduite de l'indicateur « décédé »). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SituationAgent situation = SituationAgent.ACTIF;

    /** Date de début de la situation courante et son motif (décès, cause de maladie, abandon, retraite). */
    private LocalDate situationDepuis;

    @Column(length = 500)
    @Size(max = 500)
    private String situationMotif;

    public SituationAgent getSituation() {
        return situation != null
            ? situation
            : decede
              ? SituationAgent.DECEDE
              : SituationAgent.ACTIF;
    }

    /** Vrai tant que l'agent n'a pas remplacé le mot de passe défini par un gestionnaire. */
    private boolean motDePasseTemporaire = false;
}
