package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "corps")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Corps extends BaseEntity {

    @NotBlank(message = "Le nom du corps est obligatoire")
    @Column(nullable = false, unique = true, length = 150)
    @Size(max = 150)
    private String nom;

    /** Âge de départ à la retraite pour ce corps ; vide = âge par défaut de l'application (app.retraite.age-defaut). */
    @Min(value = 40, message = "L'âge de la retraite doit être compris entre 40 et 75 ans")
    @Max(value = 75, message = "L'âge de la retraite doit être compris entre 40 et 75 ans")
    private Integer ageRetraite;
}
