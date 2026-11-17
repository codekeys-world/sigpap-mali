package ml.apes.rh.domain;

/** Situation administrative d'un agent. L'état « en formation » reste un indicateur indépendant. */
public enum SituationAgent {
    ACTIF,
    MALADE,
    ABANDON_POSTE,
    DECEDE,
    RETRAITE;

    public String libelle() {
        return switch (this) {
            case ACTIF -> "En activité";
            case MALADE -> "Malade";
            case ABANDON_POSTE -> "Abandon de poste";
            case DECEDE -> "Décédé";
            case RETRAITE -> "Retraité";
        };
    }
}
