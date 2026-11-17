package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "formation")
@Getter
@Setter
public class Formation extends BaseEntity {

    @NotBlank(message = "Le titre de la formation est obligatoire")
    @Column(nullable = false, unique = true, length = 200)
    @Size(max = 200)
    private String titre;
}
