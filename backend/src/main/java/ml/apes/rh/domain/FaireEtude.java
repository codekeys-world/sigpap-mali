package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Sous-entité « faire étude » : étude diplômante suivie par un personnel. */
@Entity
@Table(name = "faire_etude")
@Getter
@Setter
public class FaireEtude extends Periode {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "etude_id", nullable = false)
    private Etude etude;

    @Column(length = 200)
    @Size(max = 200)
    private String lieu;

    @Column(name = "option_etude", length = 200)
    @Size(max = 200)
    private String option;

    @Column(length = 1000)
    @Size(max = 1000)
    private String description;

    @Column(length = 255)
    @Size(max = 255)
    private String fichierDiplome;
}
