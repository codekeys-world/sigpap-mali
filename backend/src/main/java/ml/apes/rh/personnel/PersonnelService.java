package ml.apes.rh.personnel;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.auth.CompteEmailService;
import ml.apes.rh.catalog.ArreteIntegrationRepository;
import ml.apes.rh.catalog.CorpsRepository;
import ml.apes.rh.catalog.GradeRepository;
import ml.apes.rh.catalog.RoleRepository;
import ml.apes.rh.catalog.ServiceApRepository;
import ml.apes.rh.domain.*;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.exception.ResourceNotFoundException;
import ml.apes.rh.security.AccessScopeService;
import ml.apes.rh.security.CurrentUserService;
import ml.apes.rh.security.Roles;
import ml.apes.rh.situation.ChangementSituationRepository;
import ml.apes.rh.storage.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonnelService {

    private final PersonnelRepository repo;
    private final ArreteIntegrationRepository arreteIntegrationRepo;
    private final GradeRepository gradeRepo;
    private final CorpsRepository corpsRepo;
    private final ServiceApRepository serviceRepo;
    private final RoleRepository roleRepo;
    private final HistoriqueGradeRepository hgRepo;
    private final FaireFormationRepository ffRepo;
    private final FaireEtudeRepository feRepo;
    private final AffectationPersonnelRepository apRepo;
    private final PosteAffectationRepository posteAffectationRepo;
    private final AccessScopeService scope;
    private final CurrentUserService current;
    private final PasswordEncoder encoder;
    private final FileStorageService storage;
    private final ChangementSituationRepository situationRepo;
    private final SanctionPersonnelRepository sanctionRepo;
    private final PieceJustificativeRepository pieceRepo;
    private final CompteEmailService compteEmail;
    private final CongeAbsencePersonnelRepository congeRepo;
    private final DistinctionPersonnelRepository distinctionRepo;

    public Page<Personnel> rechercher(PersonnelFilter f, Pageable pageable) {
        return repo.findAll(
            PersonnelSpecs.build(f, scope.idsAutorises(), scope.personnelIdSeul()),
            pageable
        );
    }

    public List<Personnel> rechercherTous(PersonnelFilter f) {
        return repo.findAll(
            PersonnelSpecs.build(f, scope.idsAutorises(), scope.personnelIdSeul()),
            Sort.by("nom", "prenom")
        );
    }

    public Personnel get(Long id) {
        Personnel p = repo
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Personnel introuvable : " + id));
        scope.verifierAcces(p);
        return p;
    }

    public Personnel getBySlug(String slug) {
        Personnel p = repo
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Personnel introuvable"));
        scope.verifierAcces(p);
        p.setFonction(fonctionCourante(p));
        return p;
    }

    private String fonctionCourante(Personnel personnel) {
        return apRepo
            .findByPersonnelId(personnel.getId())
            .stream()
            .filter(
                affectation -> affectation.getDateFin() == null && affectation.getAnneeFin() == null
            )
            .max(java.util.Comparator.comparing(AffectationPersonnel::debutEffectif))
            .flatMap(affectation ->
                posteAffectationRepo
                    .findByAffectationPersonnelId(affectation.getId())
                    .stream()
                    .filter(poste -> poste.getDateFin() == null && poste.getAnneeFin() == null)
                    .max(java.util.Comparator.comparing(PosteAffectation::debutEffectif))
                    .map(poste -> poste.getPoste().getNom())
            )
            .orElse(null);
    }

    public Long idParSlug(String slug) {
        return repo
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Personnel introuvable"))
            .getId();
    }

    public boolean fichierAccessibleParUtilisateur(String chemin) {
        Personnel p = get(current.get().getId());
        if (
            java.util.Objects.equals(p.getPhoto(), chemin) ||
            (p.getArreteIntegration() != null &&
                chemin.equals(p.getArreteIntegration().getFichier())) ||
            pieceRepo
                .findByPersonnelId(p.getId())
                .stream()
                .anyMatch(x -> chemin.equals(x.getFichier())) ||
            situationRepo
                .findByPersonnelIdOrderByDateDebutDescIdDesc(p.getId())
                .stream()
                .anyMatch(x -> chemin.equals(x.getFichier())) ||
            congeRepo
                .findByPersonnelIdOrderByDateDebutDescIdDesc(p.getId())
                .stream()
                .anyMatch(x -> chemin.equals(x.getFichier())) ||
            distinctionRepo
                .findByPersonnelIdOrderByDateAttributionDescIdDesc(p.getId())
                .stream()
                .anyMatch(x -> chemin.equals(x.getFichier())) ||
            sanctionRepo
                .findByPersonnelIdOrderByDateSanctionDescIdDesc(p.getId())
                .stream()
                .anyMatch(x -> chemin.equals(x.getFichier())) ||
            hgRepo
                .findByPersonnelId(p.getId())
                .stream()
                .anyMatch(x -> chemin.equals(x.getFichierAvancement())) ||
            ffRepo
                .findByPersonnelId(p.getId())
                .stream()
                .anyMatch(x -> chemin.equals(x.getFichierAttestation())) ||
            feRepo
                .findByPersonnelId(p.getId())
                .stream()
                .anyMatch(x -> chemin.equals(x.getFichierDiplome()))
        ) {
            return true;
        }
        for (AffectationPersonnel ap : apRepo.findByPersonnelId(p.getId())) {
            if (
                ap.getAffectation().getFichierDecision() != null &&
                ap.getAffectation().getFichierDecision().equals(chemin)
            ) return true;
            if (
                posteAffectationRepo
                    .findByAffectationPersonnelId(ap.getId())
                    .stream()
                    .anyMatch(poste -> chemin.equals(poste.getFichier()))
            ) return true;
        }
        return false;
    }

    @Transactional
    public Personnel creer(Personnel in) {
        if (!StringUtils.hasText(in.getEmail())) {
            throw new BusinessException(
                "Une adresse e-mail est obligatoire pour envoyer le mot de passe temporaire."
            );
        }
        compteEmail.verifierConfiguration();
        String motDePasse = compteEmail.genererMotDePasse();
        Personnel saved = creerAvecMotDePasse(in, motDePasse);
        compteEmail.envoyerMotDePasseTemporaire(
            saved.getEmail(),
            saved.getMatricule(),
            saved.getPrenom() + " " + saved.getNom(),
            motDePasse
        );
        return saved;
    }

    /** Création depuis l'import Excel, qui garde son flux de remise des mots de passe temporaires. */
    @Transactional
    public Personnel creerImporte(Personnel in) {
        verifierMotDePasse(in.getMotDePasse(), true);
        return creerAvecMotDePasse(in, in.getMotDePasse());
    }

    private Personnel creerAvecMotDePasse(Personnel in, String motDePasse) {
        String matricule = in.getMatricule().trim();
        if (repo.existsByMatricule(matricule)) {
            throw BusinessException.conflict("Le matricule « " + matricule + " » existe déjà.");
        }
        Personnel p = new Personnel();
        copier(in, p);
        if (in.getArreteIntegration() != null) {
            p.setArreteIntegration(resoudreArreteIntegration(in.getArreteIntegration(), false));
        }
        p.setMatricule(matricule);
        p.setMotDePasse(encoder.encode(motDePasse));
        p.setMotDePasseTemporaire(true); // l'agent devra le changer à sa première connexion
        Grade grade = resoudreGrade(in);
        p.setGradeActuel(grade);
        p.setCorpsActuel(resoudreCorps(in, grade));
        p.setServiceCourant(resoudreService(in));
        p.setRole(resoudreRole(in, null));
        p.setArreteIntegration(resoudreArreteIntegration(in.getArreteIntegration(), true));
        Personnel saved = repo.saveAndFlush(p);
        if (saved.getGradeActuel() != null) historiser(saved, saved.getGradeActuel());
        return saved;
    }

    @Transactional
    public Personnel modifier(Long id, Personnel in) {
        Personnel p = get(id);
        Personnel moi = current.get();
        if (
            moi.getId().equals(id) && !in.isActif() && !Roles.ADMINISTRATEUR.equals(current.role())
        ) {
            throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        String matricule = in.getMatricule().trim();
        if (repo.existsByMatriculeAndIdNot(matricule, id)) {
            throw BusinessException.conflict("Le matricule « " + matricule + " » existe déjà.");
        }
        copier(in, p);
        if (in.getArreteIntegration() != null) {
            p.setArreteIntegration(resoudreArreteIntegration(in.getArreteIntegration(), false));
        }
        p.setMatricule(matricule);
        if (StringUtils.hasText(in.getMotDePasse())) {
            verifierMotDePasse(in.getMotDePasse(), false);
            p.setMotDePasse(encoder.encode(in.getMotDePasse()));
            p.setMotDePasseTemporaire(!moi.getId().equals(id)); // réinitialisé par un tiers => temporaire
        }
        Grade ancien = p.getGradeActuel();
        Grade nouveau = resoudreGrade(in);
        p.setGradeActuel(nouveau);
        p.setCorpsActuel(resoudreCorps(in, nouveau));
        p.setServiceCourant(resoudreService(in));
        p.setRole(resoudreRole(in, p));
        if (nouveau != null && (ancien == null || !ancien.getId().equals(nouveau.getId()))) {
            historiser(p, nouveau);
        }
        return repo.save(p);
    }

    @Transactional
    public Personnel modifierParSlug(String slug, Personnel in) {
        return modifier(idParSlug(slug), in);
    }

    @Transactional
    public Personnel modifierMonProfil(Dtos.ProfilPersonnel req) {
        Personnel p = repo
            .findById(current.get().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        p.setNom(req.nom().trim());
        p.setPrenom(req.prenom().trim());
        p.setSexe(req.sexe());
        p.setDateNaissance(req.dateNaissance());
        p.setLieuNaissance(
            StringUtils.hasText(req.lieuNaissance()) ? req.lieuNaissance().trim() : null
        );
        p.setTelephone(StringUtils.hasText(req.telephone()) ? req.telephone().trim() : null);
        p.setEmail(StringUtils.hasText(req.email()) ? req.email().trim() : null);
        p.setAdresse(StringUtils.hasText(req.adresse()) ? req.adresse().trim() : null);
        return repo.save(p);
    }

    @Transactional
    public void supprimer(Long id) {
        Personnel p = get(id);
        if (current.get().getId().equals(id)) {
            throw new BusinessException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        List<String> fichiers = new java.util.ArrayList<>();
        fichiers.add(p.getPhoto());
        pieceRepo.findByPersonnelId(id).forEach(x -> fichiers.add(x.getFichier()));
        situationRepo
            .findByPersonnelIdOrderByDateDebutDescIdDesc(id)
            .forEach(x -> fichiers.add(x.getFichier()));
        sanctionRepo
            .findByPersonnelIdOrderByDateSanctionDescIdDesc(id)
            .forEach(x -> fichiers.add(x.getFichier()));
        congeRepo
            .findByPersonnelIdOrderByDateDebutDescIdDesc(id)
            .forEach(x -> fichiers.add(x.getFichier()));
        distinctionRepo
            .findByPersonnelIdOrderByDateAttributionDescIdDesc(id)
            .forEach(x -> fichiers.add(x.getFichier()));
        hgRepo.findByPersonnelId(id).forEach(x -> fichiers.add(x.getFichierAvancement()));
        ffRepo.findByPersonnelId(id).forEach(x -> fichiers.add(x.getFichierAttestation()));
        feRepo.findByPersonnelId(id).forEach(x -> fichiers.add(x.getFichierDiplome()));
        List<AffectationPersonnel> affectations = apRepo.findByPersonnelId(id);
        affectations.forEach(affectation ->
            posteAffectationRepo
                .findByAffectationPersonnelId(affectation.getId())
                .forEach(poste -> fichiers.add(poste.getFichier()))
        );
        affectations.forEach(affectation ->
            posteAffectationRepo.deleteAll(
                posteAffectationRepo.findByAffectationPersonnelId(affectation.getId())
            )
        );
        posteAffectationRepo.flush();
        pieceRepo.deleteByPersonnelId(id);
        situationRepo.deleteByPersonnelId(id);
        sanctionRepo.deleteByPersonnelId(id);
        congeRepo.deleteByPersonnelId(id);
        distinctionRepo.deleteByPersonnelId(id);
        hgRepo.deleteByPersonnelId(id);
        ffRepo.deleteByPersonnelId(id);
        feRepo.deleteByPersonnelId(id);
        apRepo.deleteAll(affectations);
        repo.delete(p);
        repo.flush();
        fichiers.forEach(storage::supprimer); // les fichiers sont effacés du serveur une fois la base à jour
    }

    @Transactional
    public void supprimerParSlug(String slug) {
        supprimer(idParSlug(slug));
    }

    @Transactional
    public Personnel changerPhoto(Long id, MultipartFile file) {
        Personnel p = get(id);
        String nouvelle = storage.stockerImage(file);
        String ancienne = p.getPhoto();
        p.setPhoto(nouvelle);
        Personnel saved = repo.save(p);
        storage.supprimer(ancienne);
        return saved;
    }

    @Transactional
    public void supprimerPhoto(Long id) {
        Personnel p = get(id);
        String ancienne = p.getPhoto();
        p.setPhoto(null);
        repo.save(p);
        storage.supprimer(ancienne);
    }

    /** Changement de mot de passe par l'utilisateur connecté. */
    @Transactional
    public void changerMotDePasse(Dtos.ChangerMotDePasse req) {
        Personnel p = repo
            .findById(current.get().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        if (!encoder.matches(req.ancienMotDePasse(), p.getMotDePasse())) {
            throw new BusinessException("L'ancien mot de passe est incorrect.");
        }
        if (!req.nouveauMotDePasse().equals(req.confirmation())) {
            throw new BusinessException(
                "La confirmation ne correspond pas au nouveau mot de passe."
            );
        }
        if (req.nouveauMotDePasse().equals(req.ancienMotDePasse())) {
            throw new BusinessException("Le nouveau mot de passe doit être différent de l'ancien.");
        }
        p.setMotDePasse(encoder.encode(req.nouveauMotDePasse()));
        p.setMotDePasseTemporaire(false);
        repo.save(p);
    }

    // ---------- helpers ----------

    private static final Set<String> GROUPES_SANGUINS = Set.of(
        "A+",
        "A-",
        "B+",
        "B-",
        "AB+",
        "AB-",
        "O+",
        "O-"
    );

    private static String normaliserGroupeSanguin(String valeur) {
        if (!StringUtils.hasText(valeur)) return null;
        String n = valeur.trim().toUpperCase().replace(" ", "");
        if (!GROUPES_SANGUINS.contains(n)) {
            throw new BusinessException(
                "Groupe sanguin invalide (valeurs : A+, A-, B+, B-, AB+, AB-, O+, O-)."
            );
        }
        return n;
    }

    private void verifierMotDePasse(String mdp, boolean obligatoire) {
        if (!StringUtils.hasText(mdp)) {
            if (obligatoire) throw new BusinessException("Le mot de passe est obligatoire.");
            return;
        }
        if (mdp.length() < 8) throw new BusinessException(
            "Le mot de passe doit contenir au moins 8 caractères."
        );
    }

    private void copier(Personnel in, Personnel p) {
        p.setNom(in.getNom().trim());
        p.setPrenom(in.getPrenom().trim());
        p.setSexe(in.getSexe());
        p.setDateNaissance(in.getDateNaissance());
        p.setLieuNaissance(in.getLieuNaissance());
        p.setTelephone(in.getTelephone());
        p.setGroupeSanguin(normaliserGroupeSanguin(in.getGroupeSanguin()));
        p.setContactUrgence(
            StringUtils.hasText(in.getContactUrgence()) ? in.getContactUrgence().trim() : null
        );
        p.setEmail(StringUtils.hasText(in.getEmail()) ? in.getEmail().trim() : null);
        p.setAdresse(in.getAdresse());
        p.setSurveillant(in.isSurveillant());
        p.setEnFormation(in.isEnFormation());
        p.setDateRecrutement(in.getDateRecrutement());
        if (
            in.getDateTitularisation() != null &&
            in.getDateTitularisation().isAfter(LocalDate.now())
        ) {
            throw new BusinessException(
                "La date de titularisation ne peut pas etre dans le futur."
            );
        }
        if (
            in.getDateTitularisation() != null &&
            in.getDateRecrutement() != null &&
            in.getDateTitularisation().isBefore(in.getDateRecrutement())
        ) {
            throw new BusinessException("La titularisation ne peut pas preceder le recrutement.");
        }
        if (
            in.getDateTitularisation() != null &&
            in.getDateNaissance() != null &&
            in.getDateTitularisation().isBefore(in.getDateNaissance())
        ) {
            throw new BusinessException("La titularisation ne peut pas preceder la naissance.");
        }
        p.setDateTitularisation(in.getDateTitularisation());
        p.setActif(in.isActif());
        // la situation (décès, retraite) est gérée par SituationService et prime sur l'interrupteur du compte
        if (
            p.getSituation() == SituationAgent.DECEDE || p.getSituation() == SituationAgent.RETRAITE
        ) {
            p.setActif(false);
            if (p.getSituation() == SituationAgent.DECEDE) p.setEnFormation(false);
        }
    }

    private Grade resoudreGrade(Personnel in) {
        if (!in.isSurveillant()) return null;
        if (in.getGradeActuel() == null || in.getGradeActuel().getSlug() == null) return null;
        return gradeRepo
            .findBySlug(in.getGradeActuel().getSlug())
            .orElseThrow(() -> new ResourceNotFoundException("Grade introuvable"));
    }

    private Corps resoudreCorps(Personnel in, Grade grade) {
        Corps corps = null;
        if (in.getCorpsActuel() != null && in.getCorpsActuel().getSlug() != null) {
            corps = corpsRepo
                .findBySlug(in.getCorpsActuel().getSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Corps introuvable"));
        }
        if (grade != null) {
            if (corps != null && !corps.getId().equals(grade.getCorps().getId())) {
                throw new BusinessException("Le corps choisi ne correspond pas au grade.");
            }
            return grade.getCorps();
        }
        if (!in.isSurveillant() && corps == null) {
            throw new BusinessException("Le corps est obligatoire pour un agent non surveillant.");
        }
        return corps;
    }

    private ServiceAp resoudreService(Personnel in) {
        ServiceAp s = null;
        if (in.getServiceCourant() != null && in.getServiceCourant().getSlug() != null) {
            s = serviceRepo
                .findBySlug(in.getServiceCourant().getSlug())
                .orElseThrow(() -> new ResourceNotFoundException("Service introuvable"));
        }
        if (!scope.estDansPerimetre(s != null ? s.getId() : null)) {
            throw new AccessDeniedException(
                "Vous ne pouvez gérer que les agents de votre périmètre"
            );
        }
        return s;
    }

    private ArreteIntegration resoudreArreteIntegration(
        ArreteIntegration arrete,
        boolean obligatoire
    ) {
        if (arrete == null) {
            if (obligatoire) throw new BusinessException("L’arrêté d’intégration est obligatoire.");
            return null;
        }
        if (arrete.getSlug() == null) throw new ResourceNotFoundException(
            "Arrêté d’intégration introuvable"
        );
        return arreteIntegrationRepo
            .findBySlug(arrete.getSlug())
            .orElseThrow(() -> new ResourceNotFoundException("Arrêté d’intégration introuvable"));
    }

    private Role resoudreRole(Personnel in, Personnel existant) {
        String monRole = current.role();
        boolean admin = Roles.ADMINISTRATEUR.equals(monRole);
        Role defaut =
            existant != null
                ? existant.getRole()
                : roleRepo
                      .findByNom(Roles.UTILISATEUR)
                      .orElseThrow(() ->
                          new BusinessException("Le rôle UTILISATEUR est introuvable")
                      );
        if (!admin || in.getRole() == null || in.getRole().getSlug() == null) return defaut;
        Role r = roleRepo
            .findBySlug(in.getRole().getSlug())
            .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable"));
        return r;
    }

    private void historiser(Personnel p, Grade grade) {
        hgRepo
            .findByPersonnelId(p.getId())
            .stream()
            .filter(h -> h.getDateFin() == null)
            .forEach(h -> h.setDateFin(LocalDate.now()));
        HistoriqueGrade h = new HistoriqueGrade();
        h.setPersonnel(p);
        h.setGrade(grade);
        h.setDateDebut(LocalDate.now());
        hgRepo.save(h);
    }
}
