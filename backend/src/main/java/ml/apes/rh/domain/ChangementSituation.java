package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** Historique des situations d'un agent (décès, maladie, abandon de poste, retraite, reprise). */
@Entity
@Table(name = "changement_situation")
@Getter
@Setter
public class ChangementSituation extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituationAgent situation;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    /** Motif du décès, cause de la maladie, observation de l'abandon, motif de la retraite… */
    @Column(length = 500)
    private String motif;

    /** Référence du justificatif : acte de décès, certificat médical, décision… */
    @Column(length = 200)
    private String reference;

    /** Pièce justificative (acte de décès, certificat médical, décision…) : chemin relatif dans uploads. */
    @Column(length = 255)
    private String fichier;
}
