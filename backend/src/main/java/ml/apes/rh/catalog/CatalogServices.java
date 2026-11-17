package ml.apes.rh.catalog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.*;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.exception.ResourceNotFoundException;
import ml.apes.rh.personnel.AffectationPersonnelRepository;
import ml.apes.rh.personnel.PersonnelRepository;
import ml.apes.rh.repository.BaseEntityRepository;
import ml.apes.rh.security.Roles;
import ml.apes.rh.storage.FileStorageService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

/** Regroupe les petits services de référentiels (un bean Spring par classe interne statique). */
public final class CatalogServices {

    private CatalogServices() {}

    @Service
    @RequiredArgsConstructor
    public static class CorpsService extends CatalogService<Corps> {

        private final CorpsRepository repository;
        private final GradeRepository gradeRepository;

        protected BaseEntityRepository<Corps> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<Corps> specs() {
            return repository;
        }

        protected Corps nouveau() {
            return new Corps();
        }

        protected String libelle() {
            return "Corps";
        }

        protected void copier(Corps s, Corps c) {
            c.setNom(s.getNom().trim());
            c.setAgeRetraite(s.getAgeRetraite());
        }

        public List<Grade> grades(String corpsSlug) {
            Corps corps = getBySlug(corpsSlug);
            return gradeRepository.findByCorpsIdOrderByNom(corps.getId());
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class GradeService extends CatalogService<Grade> {

        private final GradeRepository repository;
        private final CorpsRepository corpsRepository;

        protected BaseEntityRepository<Grade> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<Grade> specs() {
            return repository;
        }

        protected String[] champsRecherche() {
            return new String[] { "nom", "corps.nom" };
        }

        protected Grade nouveau() {
            return new Grade();
        }

        protected String libelle() {
            return "Grade";
        }

        protected void copier(Grade s, Grade c) {
            c.setNom(s.getNom().trim());
            if (s.getCorps() == null || s.getCorps().getSlug() == null) {
                throw new BusinessException("Il faut sélectionner le corps du grade.");
            }
            c.setCorps(
                corpsRepository
                    .findBySlug(s.getCorps().getSlug())
                    .orElseThrow(() -> new ResourceNotFoundException("Corps introuvable"))
            );
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class FormationService extends CatalogService<Formation> {

        private final FormationRepository repository;

        protected BaseEntityRepository<Formation> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<Formation> specs() {
            return repository;
        }

        protected String[] champsRecherche() {
            return new String[] { "titre" };
        }

        protected String triParDefaut() {
            return "titre";
        }

        protected Formation nouveau() {
            return new Formation();
        }

        protected String libelle() {
            return "Formation";
        }

        protected void copier(Formation s, Formation c) {
            c.setTitre(s.getTitre().trim());
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class EtudeService extends CatalogService<Etude> {

        private final EtudeRepository repository;
        private final NiveauEtudeRepository niveaux;

        protected BaseEntityRepository<Etude> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<Etude> specs() {
            return repository;
        }

        protected String[] champsRecherche() {
            return new String[] { "intitule", "niveauEtude" };
        }

        protected String triParDefaut() {
            return "intitule";
        }

        protected Etude nouveau() {
            return new Etude();
        }

        protected String libelle() {
            return "Étude";
        }

        protected void copier(Etude s, Etude c) {
            c.setIntitule(s.getIntitule().trim());
            if (s.getNiveauEtude() == null || s.getNiveauEtude().isBlank()) {
                throw new BusinessException("Sélectionnez un niveau d'étude.");
            }
            c.setNiveauEtude(
                niveaux
                    .findByNomIgnoreCase(s.getNiveauEtude().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Niveau d'étude introuvable"))
                    .getNom()
            );
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class NiveauEtudeService extends CatalogService<NiveauEtude> {

        private final NiveauEtudeRepository repository;
        private final EtudeRepository etudes;

        protected BaseEntityRepository<NiveauEtude> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<NiveauEtude> specs() {
            return repository;
        }

        protected NiveauEtude nouveau() {
            return new NiveauEtude();
        }

        protected String libelle() {
            return "Niveau d'étude";
        }

        protected void copier(NiveauEtude source, NiveauEtude cible) {
            cible.setNom(source.getNom().trim());
        }

        @Override
        @org.springframework.transaction.annotation.Transactional
        public NiveauEtude modifier(Long id, NiveauEtude source) {
            String ancienNom = get(id).getNom();
            NiveauEtude niveau = super.modifier(id, source);
            etudes
                .findByNiveauEtudeIgnoreCase(ancienNom)
                .forEach(etude -> etude.setNiveauEtude(niveau.getNom()));
            return niveau;
        }

        @Override
        protected void verifierSuppression(NiveauEtude niveau) {
            if (etudes.existsByNiveauEtudeIgnoreCase(niveau.getNom())) {
                throw new BusinessException(
                    "Ce niveau est associé à une étude et ne peut pas être supprimé."
                );
            }
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class AffectationService extends CatalogService<Affectation> {

        private final AffectationRepository repository;
        private final AffectationPersonnelRepository affectationsPersonnels;
        private final FileStorageService storage;

        protected BaseEntityRepository<Affectation> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<Affectation> specs() {
            return repository;
        }

        protected String[] champsRecherche() {
            return new String[] { "reference" };
        }

        protected String triParDefaut() {
            return "reference";
        }

        protected Affectation nouveau() {
            return new Affectation();
        }

        protected String libelle() {
            return "Mutation";
        }

        protected void copier(Affectation source, Affectation cible) {
            if (source.getType() == null) throw new BusinessException(
                "Le type de mutation est obligatoire."
            );
            if (source.getReference() == null || source.getReference().isBlank()) {
                throw new BusinessException("La référence de la mutation est obligatoire.");
            }

            cible.setType(source.getType());
            cible.setReference(source.getReference().trim());
            cible.setNomMutationLegacy(source.getReference().trim());
            cible.setFichierDecision(source.getFichierDecision());
            cible.setService(null);
            if (source.getFichierDecision() == null || source.getFichierDecision().isBlank()) {
                throw new BusinessException("La décision jointe est obligatoire.");
            }
            if (!storage.existe(source.getFichierDecision())) {
                throw new BusinessException(
                    "La décision jointe est introuvable : renvoyez le fichier."
                );
            }
        }

        @Override
        @org.springframework.transaction.annotation.Transactional
        public Affectation modifier(Long id, Affectation source) {
            Affectation ancienne = get(id);
            String ancienFichier = ancienne.getFichierMutationLegacy();
            String ancienneDecision = ancienne.getFichierDecision();
            Affectation miseAJour = super.modifier(id, source);
            miseAJour.setFichierMutationLegacy(null);
            storage.supprimer(ancienFichier);
            if (
                !java.util.Objects.equals(ancienneDecision, miseAJour.getFichierDecision())
            ) storage.supprimer(ancienneDecision);
            return miseAJour;
        }

        @Override
        @org.springframework.transaction.annotation.Transactional
        public void supprimer(Long id) {
            Affectation mutation = get(id);
            super.supprimer(id);
            storage.supprimer(mutation.getFichierMutationLegacy());
            storage.supprimer(mutation.getFichierDecision());
        }

        @Override
        protected void verifierSuppression(Affectation affectation) {
            if (affectationsPersonnels.countByAffectationId(affectation.getId()) > 0) {
                throw new BusinessException(
                    "Cette mutation est utilisée par un ou plusieurs agents et ne peut pas être supprimée."
                );
            }
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class PosteService extends CatalogService<Poste> {

        private final PosteRepository repository;

        protected BaseEntityRepository<Poste> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<Poste> specs() {
            return repository;
        }

        protected Poste nouveau() {
            return new Poste();
        }

        protected String libelle() {
            return "Poste";
        }

        protected void copier(Poste s, Poste c) {
            c.setNom(s.getNom().trim());
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class ArreteIntegrationService extends CatalogService<ArreteIntegration> {

        private final ArreteIntegrationRepository repository;
        private final PersonnelRepository personnels;
        private final FileStorageService storage;

        protected BaseEntityRepository<ArreteIntegration> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<ArreteIntegration> specs() {
            return repository;
        }

        protected String[] champsRecherche() {
            return new String[] { "reference" };
        }

        protected String triParDefaut() {
            return "reference";
        }

        protected ArreteIntegration nouveau() {
            return new ArreteIntegration();
        }

        protected String libelle() {
            return "Arrêté d’intégration";
        }

        protected void copier(ArreteIntegration source, ArreteIntegration cible) {
            if (source.getReference() == null || source.getReference().isBlank()) {
                throw new BusinessException("La référence de l’arrêté est obligatoire.");
            }
            String reference = source.getReference().trim();
            repository.findByReferenceIgnoreCase(reference).ifPresent(existing -> {
                if (!existing.getId().equals(cible.getId())) {
                    throw BusinessException.conflict("Cette référence d’arrêté existe déjà.");
                }
            });
            if (source.getFichier() == null || source.getFichier().isBlank()) {
                throw new BusinessException("Le fichier de l’arrêté est obligatoire.");
            }
            if (!storage.existe(source.getFichier())) {
                throw new BusinessException(
                    "Le fichier de l’arrêté est introuvable : renvoyez-le."
                );
            }
            cible.setReference(reference);
            cible.setFichier(source.getFichier());
        }

        @Override
        @org.springframework.transaction.annotation.Transactional
        public ArreteIntegration modifier(Long id, ArreteIntegration source) {
            String ancienFichier = get(id).getFichier();
            ArreteIntegration miseAJour = super.modifier(id, source);
            if (!java.util.Objects.equals(ancienFichier, miseAJour.getFichier())) storage.supprimer(
                ancienFichier
            );
            return miseAJour;
        }

        @Override
        @org.springframework.transaction.annotation.Transactional
        public void supprimer(Long id) {
            ArreteIntegration arrete = get(id);
            super.supprimer(id);
            storage.supprimer(arrete.getFichier());
        }

        @Override
        protected void verifierSuppression(ArreteIntegration arrete) {
            if (personnels.existsByArreteIntegrationId(arrete.getId())) {
                throw new BusinessException(
                    "Cet arrêté est lié à un ou plusieurs agents et ne peut pas être supprimé."
                );
            }
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class ServiceApService extends CatalogService<ServiceAp> {

        private final ServiceApRepository repository;

        protected BaseEntityRepository<ServiceAp> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<ServiceAp> specs() {
            return repository;
        }

        protected String[] champsRecherche() {
            return new String[] { "nom", "parent.nom" };
        }

        protected ServiceAp nouveau() {
            return new ServiceAp();
        }

        protected String libelle() {
            return "Service";
        }

        protected void copier(ServiceAp s, ServiceAp c) {
            c.setNom(s.getNom().trim());
            if (s.getType() == null) {
                throw new BusinessException("Le type de service est obligatoire.");
            }
            c.setType(s.getType());
            ServiceAp parent = null;
            if (s.getParent() != null && s.getParent().getSlug() != null) {
                parent = repository
                    .findBySlug(s.getParent().getSlug())
                    .orElseThrow(() -> new ResourceNotFoundException("Service parent introuvable"));
                if (parent.getId().equals(c.getId())) {
                    throw new BusinessException("Un service ne peut pas relever de lui-même.");
                }
            }
            TypeService typeParentAttendu = switch (s.getType()) {
                case DIRECTION_REGIONALE, SERVICES_RATTACHE -> TypeService.DIRECTION_GENERALE;
                case
                    MAISON_ARRET_CORRECTION,
                    CENTRE_PENITENCIER_AGRICOLE -> TypeService.DIRECTION_REGIONALE;
                case DIRECTION_GENERALE, AUTRE_SERVICE -> null;
            };
            boolean doitEtreRacine =
                s.getType() == TypeService.DIRECTION_GENERALE ||
                s.getType() == TypeService.AUTRE_SERVICE;
            if (doitEtreRacine && parent != null) {
                throw new BusinessException(
                    "Ce type de service ne peut pas avoir de service parent."
                );
            }
            if (
                typeParentAttendu != null &&
                (parent == null || parent.getType() != typeParentAttendu)
            ) {
                throw new BusinessException(
                    "Le type de service parent n'est pas compatible avec le service sélectionné."
                );
            }
            c.setParent(parent);
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class AutorisationService extends CatalogService<Autorisation> {

        private final AutorisationRepository repository;

        protected BaseEntityRepository<Autorisation> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<Autorisation> specs() {
            return repository;
        }

        protected Autorisation nouveau() {
            return new Autorisation();
        }

        protected String libelle() {
            return "Autorisation";
        }

        protected void copier(Autorisation s, Autorisation c) {
            c.setNom(s.getNom().trim().toUpperCase());
        }
    }

    @Service
    @RequiredArgsConstructor
    public static class RoleService extends CatalogService<Role> {

        private static final Set<String> ROLES_SYSTEME = Set.of(
            Roles.ADMINISTRATEUR,
            Roles.GESTIONNAIRE_RH,
            Roles.DIRECTEUR_REGIONAL,
            Roles.CHEF_ETABLISSEMENT,
            Roles.UTILISATEUR,
            Roles.DIRECTEUR_GENERAL,
            Roles.DIRECTEUR_GENERAL_ADJOINT,
            Roles.CONSULTATION
        );
        private final RoleRepository repository;
        private final AutorisationRepository autorisationRepository;

        protected BaseEntityRepository<Role> repo() {
            return repository;
        }

        protected org.springframework.data.jpa.repository.JpaSpecificationExecutor<Role> specs() {
            return repository;
        }

        protected Role nouveau() {
            return new Role();
        }

        protected String libelle() {
            return "Rôle";
        }

        protected void copier(Role s, Role c) {
            String nom = s.getNom().trim().toUpperCase();
            if (
                c.getNom() != null && ROLES_SYSTEME.contains(c.getNom()) && !c.getNom().equals(nom)
            ) {
                throw new BusinessException("Les rôles système ne peuvent pas être renommés.");
            }
            c.setNom(nom);
            Set<Autorisation> auts = new HashSet<>();
            if (s.getAutorisations() != null) {
                List<String> slugs = s
                    .getAutorisations()
                    .stream()
                    .map(Autorisation::getSlug)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
                if (slugs.size() != s.getAutorisations().size()) {
                    throw new BusinessException("Une autorisation sélectionnée est invalide.");
                }
                List<Autorisation> trouvees = autorisationRepository.findAllBySlugIn(slugs);
                if (trouvees.size() != slugs.size()) {
                    throw new ResourceNotFoundException(
                        "Une autorisation sélectionnée est introuvable."
                    );
                }
                auts.addAll(trouvees);
            }
            c.setAutorisations(auts);
        }

        @Override
        protected void verifierSuppression(Role r) {
            if (ROLES_SYSTEME.contains(r.getNom())) {
                throw new BusinessException("Les rôles système ne peuvent pas être supprimés.");
            }
        }
    }
}
