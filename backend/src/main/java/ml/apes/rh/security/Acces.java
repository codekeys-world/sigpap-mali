package ml.apes.rh.security;

/** Expressions utilisées dans les @PreAuthorize (une seule source de vérité pour les droits). */
public final class Acces {

    public static final String TOUS = "isAuthenticated()";
    public static final String LECTURE_AUDIT = "isAuthenticated() and !hasRole('UTILISATEUR')";
    public static final String CARTES_SERVICE = "isAuthenticated() and !hasRole('UTILISATEUR')";
    public static final String MODIFIER_PROFIL = "isAuthenticated() and !hasRole('UTILISATEUR')";
    public static final String ADMIN = "hasRole('ADMINISTRATEUR')";
    public static final String ADMIN_RH = "hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_RH')";
    public static final String GESTIONNAIRES =
        "hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_RH','DIRECTEUR_REGIONAL','CHEF_ETABLISSEMENT')";
    public static final String LECTEURS_RH =
        "hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_RH','DIRECTEUR_REGIONAL','CHEF_ETABLISSEMENT'," +
        "'DIRECTEUR_GENERAL','DIRECTEUR_GENERAL_ADJOINT','CONSULTATION')";

    private Acces() {}
}
