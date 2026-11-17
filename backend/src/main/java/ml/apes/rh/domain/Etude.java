package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "etude")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Etude extends BaseEntity {

    @NotBlank(message = "L'intitulé de l'étude est obligatoire")
    @Column(nullable = false, length = 200)
    @Size(max = 200)
    private String intitule;

    @NotBlank(message = "Le niveau d'étude est obligatoire")
    @Column(nullable = false, length = 100)
    @Size(max = 100)
    private String niveauEtude;
}
