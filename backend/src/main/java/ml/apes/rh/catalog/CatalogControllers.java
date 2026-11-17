package ml.apes.rh.catalog;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.CatalogService;
import ml.apes.rh.catalog.CatalogServices.*;
import ml.apes.rh.domain.*;
import ml.apes.rh.security.Acces;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

public final class CatalogControllers {

    private CatalogControllers() {}

    @RestController
    @RequestMapping("/api/corps")
    @RequiredArgsConstructor
    public static class CorpsController extends CatalogController<Corps> {

        private final CorpsService service;

        protected CatalogService<Corps> service() {
            return service;
        }

        /** Affichage des grades d'un corps. */
        @GetMapping("/{slug}/grades")
        @PreAuthorize(Acces.TOUS)
        public List<Grade> grades(@PathVariable String slug) {
            return service.grades(slug);
        }
    }

    @RestController
    @RequestMapping("/api/grades")
    @RequiredArgsConstructor
    public static class GradeController extends CatalogController<Grade> {

        private final GradeService service;

        protected CatalogService<Grade> service() {
            return service;
        }
    }

    @RestController
    @RequestMapping("/api/formations")
    @RequiredArgsConstructor
    public static class FormationController extends CatalogController<Formation> {

        private final FormationService service;

        protected CatalogService<Formation> service() {
            return service;
        }
    }

    @RestController
    @RequestMapping("/api/etudes")
    @RequiredArgsConstructor
    public static class EtudeController extends CatalogController<Etude> {

        private final EtudeService service;

        protected CatalogService<Etude> service() {
            return service;
        }
    }

    @RestController
    @RequestMapping("/api/niveaux-etude")
    @RequiredArgsConstructor
    public static class NiveauEtudeController extends CatalogController<NiveauEtude> {

        private final NiveauEtudeService service;

        protected CatalogService<NiveauEtude> service() {
            return service;
        }
    }

    @RestController
    @RequestMapping("/api/affectations")
    @RequiredArgsConstructor
    public static class AffectationController extends CatalogController<Affectation> {

        private final AffectationService service;

        protected CatalogService<Affectation> service() {
            return service;
        }
    }

    @RestController
    @RequestMapping("/api/postes")
    @RequiredArgsConstructor
    public static class PosteController extends CatalogController<Poste> {

        private final PosteService service;

        protected CatalogService<Poste> service() {
            return service;
        }
    }

    @RestController
    @RequestMapping("/api/arretes-integration")
    @RequiredArgsConstructor
    public static class ArreteIntegrationController extends CatalogController<ArreteIntegration> {

        private final ArreteIntegrationService service;

        protected CatalogService<ArreteIntegration> service() {
            return service;
        }
    }

    @RestController
    @RequestMapping("/api/services")
    @RequiredArgsConstructor
    public static class ServiceApController extends CatalogController<ServiceAp> {

        private final ServiceApService service;

        protected CatalogService<ServiceAp> service() {
            return service;
        }
    }
}
