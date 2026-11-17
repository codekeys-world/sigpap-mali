package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "autorisation")
@Getter
@Setter
public class Autorisation extends BaseEntity {

    @NotBlank(message = "Le nom de l'autorisation est obligatoire")
    @Column(nullable = false, unique = true, length = 100)
    @Size(max = 100)
    @Pattern(
        regexp = "^[A-Za-z0-9_]+$",
        message = "Utilisez uniquement des lettres, des chiffres et le caractère _"
    )
    private String nom;
}
