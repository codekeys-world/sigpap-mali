package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Référentiel d'une mutation partagée par plusieurs agents. */
@Entity
@Table(name = "affectation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Affectation extends BaseEntity {

    @NotNull(message = "Le type d'affectation est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeAffectation type;

    /** Référence de la mutation, partagée par tous les agents qui y sont rattachés. */
    @Column(name = "decision", length = 255)
    private String reference;

    @JsonIgnore
    @Column(name = "fichier_mutation", length = 255)
    private String fichierMutationLegacy;

    @JsonIgnore
    @Column(name = "nom_mutation", nullable = false, length = 200)
    private String nomMutationLegacy;

    @Column(length = 255)
    private String fichierDecision;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private ServiceAp service;
}
