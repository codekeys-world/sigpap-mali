package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.Setter;
import ml.apes.rh.exception.BusinessException;

@Entity
@Table(name = "conge_absence_personnel")
@Getter
@Setter
public class CongeAbsencePersonnel extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @NotNull(message = "Le type est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeCongeAbsence type;

    @NotBlank(message = "La nature du congé ou de l’absence est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nature;

    @NotNull(message = "La date de début est obligatoire")
    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Size(max = 200)
    @Column(length = 200)
    private String reference;

    @Size(max = 1000)
    @Column(length = 1000)
    private String observations;

    @Size(max = 255)
    @Column(length = 255)
    private String fichier;

    public void verifierDates() {
        if (dateDebut == null) throw new BusinessException("La date de début est obligatoire.");
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new BusinessException("La date de fin ne peut pas précéder la date de début.");
        }
    }

    /** Durée calendaire indicative, bornes incluses ; aucun calcul de solde de congés. */
    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getDureeJours() {
        return dateDebut != null && dateFin != null
            ? ChronoUnit.DAYS.between(dateDebut, dateFin) + 1
            : null;
    }
}
