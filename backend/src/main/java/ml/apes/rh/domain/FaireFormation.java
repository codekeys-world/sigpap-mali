package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sous-entité « faire formation » : participation d'un personnel à une formation certifiante. */
@Entity
@Table(name = "faire_formation")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FaireFormation extends Periode {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "formation_id", nullable = false)
    private Formation formation;

    @Column(length = 200)
    @Size(max = 200)
    private String lieu;

    @Column(length = 1000)
    @Size(max = 1000)
    private String description;

    @Column(length = 255)
    @Size(max = 255)
    private String fichierAttestation;
}
