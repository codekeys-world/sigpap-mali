package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Période pendant laquelle un personnel occupe une affectation. */
@Entity
@Table(name = "affectation_personnel")
@Getter
@Setter
public class AffectationPersonnel extends Periode {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "affectation_id", nullable = false)
    private Affectation affectation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private ServiceAp service;

    @Transient
    public Integer getDureeMois() {
        LocalDate debut =
            getDateDebut() != null
                ? getDateDebut()
                : getAnneeDebut() != null
                  ? LocalDate.of(getAnneeDebut(), 1, 1)
                  : null;
        if (debut == null) return null;
        LocalDate fin =
            getDateFin() != null
                ? getDateFin()
                : getAnneeFin() != null
                  ? LocalDate.of(getAnneeFin(), 12, 1)
                  : LocalDate.now();
        return Math.toIntExact(
            ChronoUnit.MONTHS.between(YearMonth.from(debut), YearMonth.from(fin))
        );
    }

    @JsonIgnore
    @OneToMany(mappedBy = "affectationPersonnel", cascade = CascadeType.REMOVE)
    private List<PosteAffectation> postes = new ArrayList<>();
}
