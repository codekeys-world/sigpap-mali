package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** Pièce justificative du dossier d'un agent : le fichier est sur le serveur, la base conserve son chemin. */
@Entity
@Table(name = "piece_justificative")
@Getter
@Setter
public class PieceJustificative extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @NotBlank(message = "La catégorie est obligatoire")
    @Column(nullable = false, length = 60)
    private String categorie;

    @NotBlank(message = "L'intitulé est obligatoire")
    @Column(nullable = false, length = 200)
    private String intitule;

    private LocalDate dateDocument;

    /** Chemin relatif du fichier dans le dossier uploads. */
    @NotBlank(message = "Le fichier est obligatoire")
    @Column(nullable = false, length = 255)
    private String fichier;

    @Column(length = 1000)
    private String description;
}
