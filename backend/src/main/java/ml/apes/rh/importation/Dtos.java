package ml.apes.rh.importation;

import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record ErreurImport(int ligne, String matricule, String message) {}

    public record CompteGenere(String matricule, String nom, String motDePasse) {}

    public record ImportResult(
        boolean simulation,
        int total,
        int valides,
        int crees,
        List<ErreurImport> erreurs,
        List<CompteGenere> comptes
    ) {}
}
