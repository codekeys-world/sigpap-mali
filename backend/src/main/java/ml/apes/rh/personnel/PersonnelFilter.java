package ml.apes.rh.personnel;

import lombok.Getter;
import lombok.Setter;

/** Filtres de recherche / d'export : texte, grade, corps, service, surveillant, en formation, décès. */
@Getter
@Setter
public class PersonnelFilter {

    private String q;
    private String gradeSlug;
    private String corpsSlug;
    private String serviceSlug;
    private Boolean surveillant;
    private Boolean enFormation;
    private Boolean decede;
    /** ACTIF (en activité), MALADE, ABANDON_POSTE, DECEDE ou RETRAITE. */
    private String statut;
    /** Agents ayant une étude en cours (sans date ni année de fin). */
    private Boolean enEtude;
}
