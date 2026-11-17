package ml.apes.rh.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "niveau_etude")
@Getter
@Setter
public class NiveauEtude extends BaseEntity {

    @NotBlank(message = "Le nom du niveau est obligatoire")
    @Column(nullable = false, unique = true, length = 100)
    @Size(max = 100)
    private String nom;
}
