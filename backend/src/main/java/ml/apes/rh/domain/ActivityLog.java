package ml.apes.rh.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Journal d'activité : écritures, connexions, exports et documents, avec l'origine technique (IP, navigateur…). */
@Entity
@Table(name = "journal_activite")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String utilisateur;

    @Column(length = 40)
    private String role;

    /** CREATION, MODIFICATION, SUPPRESSION, CONNEXION, ECHEC_CONNEXION, EXPORT, DOCUMENT, TELEVERSEMENT, CHANGEMENT_MOT_DE_PASSE */
    @Column(length = 40)
    private String action;

    @Column(length = 10)
    private String methode;

    @Column(length = 400)
    private String uri;

    @Column(length = 400)
    private String parametres;

    @Column(length = 200)
    private String ressource;

    @Column(length = 40)
    private String ressourceId;

    private int statut;
    private Long dureeMs;
    private LocalDateTime dateAction;

    // ---- origine de la requête ----
    @Column(length = 64)
    private String ip;

    @Column(length = 500)
    private String client; // User-Agent complet

    @Column(length = 60)
    private String navigateur;

    @Column(length = 60)
    private String systeme;

    @Column(length = 20)
    private String appareil;

    @Column(length = 200)
    private String origine;
}
