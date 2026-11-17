package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "poste")
@Getter
@Setter
public class Poste extends BaseEntity {

    @NotBlank(message = "Le nom du poste est obligatoire")
    @Column(nullable = false, unique = true, length = 150)
    @Size(max = 150)
    private String nom;
}
