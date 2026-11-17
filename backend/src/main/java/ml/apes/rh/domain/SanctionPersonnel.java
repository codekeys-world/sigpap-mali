package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** Décision disciplinaire enregistrée dans le dossier d'un agent. */
@Entity
@Table(name = "sanction_personnel")
@Getter
@Setter
public class SanctionPersonnel extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @NotBlank(message = "La nature de la sanction est obligatoire")
    @Column(nullable = false, length = 120)
    private String nature;

    @NotNull(message = "La date de la sanction est obligatoire")
    @Column(name = "date_sanction", nullable = false)
    private LocalDate dateSanction;

    @NotBlank(message = "Le motif de la sanction est obligatoire")
    @Column(nullable = false, length = 1000)
    private String motif;

    @Column(length = 200)
    private String reference;

    @Column(length = 255)
    private String fichier;
}
