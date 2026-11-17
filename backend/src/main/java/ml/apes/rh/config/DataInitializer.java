package ml.apes.rh.config;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.EtudeRepository;
import ml.apes.rh.catalog.NiveauEtudeRepository;
import ml.apes.rh.domain.Etude;
import ml.apes.rh.domain.NiveauEtude;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Reconstitue le catalogue des niveaux à partir des études déjà enregistrées. */
@Component
@ConditionalOnProperty(name = "app.seed-enabled", havingValue = "true")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EtudeRepository etudeRepo;
    private final NiveauEtudeRepository niveauEtudeRepo;

    @Override
    @Transactional
    public void run(String... args) {
        etudeRepo
            .findAll()
            .stream()
            .map(Etude::getNiveauEtude)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(niveau -> !niveau.isEmpty())
            .distinct()
            .filter(niveau -> niveauEtudeRepo.findByNomIgnoreCase(niveau).isEmpty())
            .forEach(niveau -> {
                NiveauEtude nouveau = new NiveauEtude();
                nouveau.setNom(niveau);
                niveauEtudeRepo.save(nouveau);
            });
    }
}
