package ml.apes.rh.personnel;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.List;
import ml.apes.rh.domain.ArreteIntegration;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.security.CurrentUserService;
import ml.apes.rh.situation.ChangementSituationRepository;
import org.junit.jupiter.api.Test;

class PersonnelFileAccessTest {

    @SuppressWarnings("unchecked")
    private <T> T emptyRepository(Class<T> repository) {
        return (T) Proxy.newProxyInstance(
            repository.getClassLoader(),
            new Class<?>[] { repository },
            (proxy, method, args) -> {
                if (List.class.isAssignableFrom(method.getReturnType())) return List.of();
                throw new AssertionError("Unexpected repository call: " + method.getName());
            }
        );
    }

    @Test
    void userCanDownloadOwnPhotoAndIntegrationDecreeButNotUnrelatedFiles() {
        Personnel agent = new Personnel();
        agent.setPhoto("photos/own-photo.png");
        ArreteIntegration arrete = new ArreteIntegration();
        arrete.setFichier("documents/own-decree.pdf");
        agent.setArreteIntegration(arrete);
        CurrentUserService current = new CurrentUserService(null) {
            @Override
            public Personnel get() {
                return agent;
            }
        };
        PersonnelService service = new PersonnelService(
            null,
            null,
            null,
            null,
            null,
            null,
            emptyRepository(HistoriqueGradeRepository.class),
            emptyRepository(FaireFormationRepository.class),
            emptyRepository(FaireEtudeRepository.class),
            emptyRepository(AffectationPersonnelRepository.class),
            emptyRepository(PosteAffectationRepository.class),
            null,
            current,
            null,
            null,
            emptyRepository(ChangementSituationRepository.class),
            emptyRepository(SanctionPersonnelRepository.class),
            emptyRepository(PieceJustificativeRepository.class),
            null,
            emptyRepository(CongeAbsencePersonnelRepository.class),
            emptyRepository(DistinctionPersonnelRepository.class)
        ) {
            @Override
            public Personnel get(Long id) {
                return agent;
            }
        };
        assertTrue(service.fichierAccessibleParUtilisateur("photos/own-photo.png"));
        assertTrue(service.fichierAccessibleParUtilisateur("documents/own-decree.pdf"));
        assertFalse(service.fichierAccessibleParUtilisateur("documents/another-agent.pdf"));
        agent.setArreteIntegration(null);
        assertFalse(service.fichierAccessibleParUtilisateur("documents/own-decree.pdf"));
    }
}
