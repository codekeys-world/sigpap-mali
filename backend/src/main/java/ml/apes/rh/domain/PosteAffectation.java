package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Poste occupé pendant une affectation. */
@Entity
@Table(name = "poste_affectation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PosteAffectation extends Periode {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "affectation_personnel_id", nullable = false)
    private AffectationPersonnel affectationPersonnel;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "poste_id", nullable = false)
    private Poste poste;

    @Column(length = 1000)
    @Size(max = 1000)
    private String description;

    @Column(length = 255)
    @Size(max = 255)
    private String fichier;
}
