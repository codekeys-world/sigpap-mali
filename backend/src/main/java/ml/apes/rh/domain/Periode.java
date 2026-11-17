package ml.apes.rh.domain;

import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ml.apes.rh.exception.BusinessException;

/** Période avec dates exactes OU, à défaut, années de début et de fin (cf. cahier des charges). */
@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Periode extends BaseEntity {

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Min(1950)
    @Max(2100)
    private Integer anneeDebut;

    @Min(1950)
    @Max(2100)
    private Integer anneeFin;

    public void verifierPeriode() {
        if (dateDebut == null && anneeDebut == null) {
            throw new BusinessException("Renseignez une date ou une année de début.");
        }
        if (dateDebut != null && dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new BusinessException("La date de fin ne peut pas précéder la date de début.");
        }
        if (anneeDebut != null && anneeFin != null && anneeFin < anneeDebut) {
            throw new BusinessException("L'année de fin ne peut pas précéder l'année de début.");
        }
        int debut = dateDebut != null ? dateDebut.getYear() : anneeDebut;
        Integer fin = dateFin != null ? Integer.valueOf(dateFin.getYear()) : anneeFin;
        if (fin != null && fin < debut) {
            throw new BusinessException("La fin de la période ne peut pas précéder son début.");
        }
    }

    public void copierPeriode(Periode autre) {
        this.dateDebut = autre.dateDebut;
        this.dateFin = autre.dateFin;
        this.anneeDebut = autre.anneeDebut;
        this.anneeFin = autre.anneeFin;
        verifierPeriode();
    }

    public LocalDate debutEffectif() {
        if (dateDebut != null) return dateDebut;
        if (anneeDebut != null) return LocalDate.of(anneeDebut, 1, 1);
        return LocalDate.MIN;
    }
}
