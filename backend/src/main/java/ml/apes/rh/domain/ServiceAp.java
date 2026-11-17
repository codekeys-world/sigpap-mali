package ml.apes.rh.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Service : Direction générale, direction régionale, service central, maison d'arrêt, autre service. */
@Entity
@Table(name = "service_ap")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceAp extends BaseEntity {

    @NotBlank(message = "Le nom du service est obligatoire")
    @Column(nullable = false, length = 200)
    @Size(max = 200)
    private String nom;

    @NotNull(message = "Le type de service est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeService type;

    /** Une maison d'arrêt relève d'une direction régionale. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private ServiceAp parent;
}
