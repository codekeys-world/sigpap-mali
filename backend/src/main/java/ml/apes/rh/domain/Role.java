package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role extends BaseEntity {

    @NotBlank(message = "Le nom du rôle est obligatoire")
    @Column(nullable = false, unique = true, length = 60)
    @Size(max = 60)
    @Pattern(
        regexp = "^[A-Za-z0-9_]+$",
        message = "Utilisez uniquement des lettres, des chiffres et le caractère _"
    )
    private String nom;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_autorisation",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "autorisation_id")
    )
    private Set<Autorisation> autorisations = new HashSet<>();
}
