package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "grade", uniqueConstraints = @UniqueConstraint(columnNames = { "nom", "corps_id" }))
@Getter
@Setter
public class Grade extends BaseEntity {

    @NotBlank(message = "Le nom du grade est obligatoire")
    @Column(nullable = false, length = 150)
    @Size(max = 150)
    private String nom;

    @NotNull(message = "Le corps du grade est obligatoire")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "corps_id", nullable = false)
    private Corps corps;
}
