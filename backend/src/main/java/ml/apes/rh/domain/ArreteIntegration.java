package ml.apes.rh.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "arrete_integration")
@Getter
@Setter
public class ArreteIntegration extends BaseEntity {

    @NotBlank(message = "La référence de l’arrêté est obligatoire")
    @Column(nullable = false, unique = true, length = 150)
    @Size(max = 150)
    private String reference;

    @NotBlank(message = "Le fichier de l’arrêté est obligatoire")
    @Column(nullable = false, length = 255)
    @Size(max = 255)
    private String fichier;
}
