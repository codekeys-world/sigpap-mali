package ml.apes.rh.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "distinction_personnel")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DistinctionPersonnel extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false)
    private Personnel personnel;

    @NotBlank(message = "L’intitulé de la distinction est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String intitule;

    @NotNull(message = "La date d’attribution est obligatoire")
    @Column(nullable = false)
    private LocalDate dateAttribution;

    @Size(max = 200)
    @Column(length = 200)
    private String autorite;

    @Size(max = 200)
    @Column(length = 200)
    private String reference;

    @Size(max = 1000)
    @Column(length = 1000)
    private String motif;

    @Size(max = 255)
    @Column(length = 255)
    private String fichier;
}
