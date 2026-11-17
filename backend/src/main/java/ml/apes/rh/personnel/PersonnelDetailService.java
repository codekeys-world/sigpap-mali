package ml.apes.rh.personnel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.AffectationRepository;
import ml.apes.rh.catalog.EtudeRepository;
import ml.apes.rh.catalog.FormationRepository;
import ml.apes.rh.catalog.GradeRepository;
import ml.apes.rh.catalog.PosteRepository;
import ml.apes.rh.catalog.ServiceApRepository;
import ml.apes.rh.domain.*;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.exception.ResourceNotFoundException;
import ml.apes.rh.personnel.Dtos.MutationEvent;
import ml.apes.rh.security.AccessScopeService;
import ml.apes.rh.security.CurrentUserService;
import ml.apes.rh.security.Roles;
import ml.apes.rh.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Gestion des grades, formations, études, affectations et postes d'un agent. */
@Service
@RequiredArgsConstructor
@Transactional
public class PersonnelDetailService {

    private final PersonnelService personnelService;
    private final GradeRepository gradeRepo;
    private final HistoriqueGradeRepository hgRepo;
    private final FormationRepository formationRepo;
    private final FaireFormationRepository ffRepo;
    private final EtudeRepository etudeRepo;
    private final FaireEtudeRepository feRepo;
    private final AffectationPersonnelRepository apRepo;
    private final AffectationRepository affectationRepo;
    private final ServiceApRepository serviceRepo;
    private final CurrentUserService currentUser;
    private final AccessScopeService accessScope;
    private final PosteRepository posteRepo;
    private final PosteAffectationRepository paRepo;
    private final SanctionPersonnelRepository sanctionRepo;
    private final FileStorageService storage;

    public Long personnelId(String slug) {
        return personnelService.idParSlug(slug);
    }

    public Long idRessource(String type, String slug) {
        return switch (type) {
            case "grade" -> hgRepo
                .findBySlug(slug)
                .orElseThrow(() -> introuvable("Grade"))
                .getId();
            case "formation" -> ffRepo
                .findBySlug(slug)
                .orElseThrow(() -> introuvable("Formation"))
                .getId();
            case "etude" -> feRepo
                .findBySlug(slug)
                .orElseThrow(() -> introuvable("Étude"))
                .getId();
            case "affectation" -> apRepo
                .findBySlug(slug)
                .orElseThrow(() -> introuvable("Affectation"))
                .getId();
            case "poste" -> paRepo
                .findBySlug(slug)
                .orElseThrow(() -> introuvable("Poste"))
                .getId();
            case "sanction" -> sanctionRepo
                .findBySlug(slug)
                .orElseThrow(() -> introuvable("Sanction"))
                .getId();
            default -> throw new IllegalArgumentException("Type de ressource inconnu");
        };
    }

    private static <T extends Periode> List<T> trier(List<T> l) {
        l.sort(Comparator.comparing((T x) -> x.debutEffectif()).reversed());
        return l;
    }

    private void verifierSurveillant(Personnel p) {
        if (!p.isSurveillant()) throw new BusinessException(
            "Les agents non surveillants ne portent pas de grade."
        );
    }

    private static ResourceNotFoundException introuvable(String what) {
        return new ResourceNotFoundException(what + " introuvable");
    }

    // ===================== Historique de grades =====================

    @Transactional(readOnly = true)
    public List<HistoriqueGrade> grades(Long pid) {
        personnelService.get(pid);
        return trier(hgRepo.findByPersonnelId(pid));
    }

    public HistoriqueGrade ajouterGrade(Long pid, HistoriqueGrade in) {
        Personnel p = personnelService.get(pid);
        verifierSurveillant(p);
        HistoriqueGrade h = new HistoriqueGrade();
        h.setPersonnel(p);
        remplirGrade(in, h);
        HistoriqueGrade saved = hgRepo.saveAndFlush(h);
        rafraichirGrade(p);
        return saved;
    }

    public HistoriqueGrade modifierGrade(Long pid, Long id, HistoriqueGrade in) {
        Personnel p = personnelService.get(pid);
        verifierSurveillant(p);
        HistoriqueGrade h = hgRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Historique de grade"));
        String ancienFichier = h.getFichierAvancement();
        remplirGrade(in, h);
        HistoriqueGrade saved = hgRepo.saveAndFlush(h);
        if (
            StringUtils.hasText(ancienFichier) &&
            !ancienFichier.equals(saved.getFichierAvancement())
        ) {
            storage.supprimer(ancienFichier);
        }
        rafraichirGrade(p);
        return saved;
    }

    public void supprimerGrade(Long pid, Long id) {
        Personnel p = personnelService.get(pid);
        verifierSurveillant(p);
        HistoriqueGrade h = hgRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Historique de grade"));
        String fichier = h.getFichierAvancement();
        hgRepo.delete(h);
        hgRepo.flush();
        storage.supprimer(fichier);
        rafraichirGrade(p);
    }

    private void remplirGrade(HistoriqueGrade in, HistoriqueGrade h) {
        if (in.getGrade() == null || in.getGrade().getSlug() == null) throw new BusinessException(
            "Le grade est obligatoire."
        );
        h.setGrade(
            gradeRepo.findBySlug(in.getGrade().getSlug()).orElseThrow(() -> introuvable("Grade"))
        );
        if (in.getDateDebut() == null && in.getAnneeDebut() == null) {
            throw new BusinessException("Renseignez la date de début ou l'année de début.");
        }
        if (!StringUtils.hasText(in.getFichierAvancement()) && h.getId() == null) {
            throw new BusinessException("La décision d'avancement du grade doit être jointe.");
        }
        if (
            StringUtils.hasText(in.getFichierAvancement()) &&
            !storage.existe(in.getFichierAvancement())
        ) {
            throw new BusinessException("Le fichier d'avancement est introuvable : renvoyez-le.");
        }
        h.setFichierAvancement(in.getFichierAvancement());
        h.copierPeriode(in);
    }

    private void rafraichirGrade(Personnel p) {
        p.setGradeActuel(
            hgRepo
                .findByPersonnelId(p.getId())
                .stream()
                .max(Comparator.comparing(HistoriqueGrade::debutEffectif))
                .map(HistoriqueGrade::getGrade)
                .orElse(null)
        );
    }

    // ===================== Formations =====================

    @Transactional(readOnly = true)
    public List<FaireFormation> formations(Long pid) {
        personnelService.get(pid);
        return trier(ffRepo.findByPersonnelId(pid));
    }

    public FaireFormation ajouterFormation(Long pid, FaireFormation in) {
        Personnel p = personnelService.get(pid);
        FaireFormation f = new FaireFormation();
        f.setPersonnel(p);
        remplirFormation(in, f);
        return ffRepo.saveAndFlush(f);
    }

    public FaireFormation modifierFormation(Long pid, Long id, FaireFormation in) {
        personnelService.get(pid);
        FaireFormation f = ffRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Formation du personnel"));
        String ancienneAttestation = f.getFichierAttestation();
        remplirFormation(in, f);
        FaireFormation saved = ffRepo.saveAndFlush(f);
        if (
            StringUtils.hasText(ancienneAttestation) &&
            !ancienneAttestation.equals(saved.getFichierAttestation())
        ) {
            storage.supprimer(ancienneAttestation);
        }
        return saved;
    }

    public void supprimerFormation(Long pid, Long id) {
        personnelService.get(pid);
        FaireFormation f = ffRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Formation du personnel"));
        String attestation = f.getFichierAttestation();
        ffRepo.delete(f);
        ffRepo.flush();
        storage.supprimer(attestation);
    }

    private void remplirFormation(FaireFormation in, FaireFormation f) {
        Formation fin = in.getFormation();
        if (fin == null || fin.getSlug() == null) throw new BusinessException(
            "Sélectionnez une formation."
        );
        f.setFormation(
            formationRepo.findBySlug(fin.getSlug()).orElseThrow(() -> introuvable("Formation"))
        );
        f.setLieu(in.getLieu());
        f.setDescription(in.getDescription());
        if (
            StringUtils.hasText(in.getFichierAttestation()) &&
            !storage.existe(in.getFichierAttestation())
        ) {
            throw new BusinessException(
                "L'attestation de formation est introuvable : renvoyez-la."
            );
        }
        f.setFichierAttestation(in.getFichierAttestation());
        f.copierPeriode(in);
    }

    // ===================== Études =====================

    @Transactional(readOnly = true)
    public List<FaireEtude> etudes(Long pid) {
        personnelService.get(pid);
        return trier(feRepo.findByPersonnelId(pid));
    }

    public FaireEtude ajouterEtude(Long pid, FaireEtude in) {
        Personnel p = personnelService.get(pid);
        FaireEtude e = new FaireEtude();
        e.setPersonnel(p);
        remplirEtude(in, e);
        return feRepo.saveAndFlush(e);
    }

    public FaireEtude modifierEtude(Long pid, Long id, FaireEtude in) {
        personnelService.get(pid);
        FaireEtude e = feRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Étude du personnel"));
        String ancienDiplome = e.getFichierDiplome();
        remplirEtude(in, e);
        FaireEtude saved = feRepo.saveAndFlush(e);
        if (
            StringUtils.hasText(ancienDiplome) && !ancienDiplome.equals(saved.getFichierDiplome())
        ) {
            storage.supprimer(ancienDiplome);
        }
        return saved;
    }

    public void supprimerEtude(Long pid, Long id) {
        personnelService.get(pid);
        FaireEtude e = feRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Étude du personnel"));
        String diplome = e.getFichierDiplome();
        feRepo.delete(e);
        feRepo.flush();
        storage.supprimer(diplome);
    }

    private void remplirEtude(FaireEtude in, FaireEtude e) {
        Etude ein = in.getEtude();
        if (ein == null || ein.getSlug() == null) throw new BusinessException(
            "Sélectionnez une étude."
        );
        e.setEtude(etudeRepo.findBySlug(ein.getSlug()).orElseThrow(() -> introuvable("Étude")));
        if (!StringUtils.hasText(in.getFichierDiplome()) && e.getId() == null) {
            throw new BusinessException("Le diplôme doit être joint à l'étude.");
        }
        e.setFichierDiplome(in.getFichierDiplome());
        e.setLieu(in.getLieu());
        e.setOption(in.getOption());
        e.setDescription(in.getDescription());
        e.copierPeriode(in);
    }

    // ===================== Affectations =====================

    @Transactional(readOnly = true)
    public List<AffectationPersonnel> affectations(Long pid) {
        personnelService.get(pid);
        return trier(apRepo.findByPersonnelId(pid));
    }

    public AffectationPersonnel ajouterAffectation(Long pid, AffectationPersonnel in) {
        Personnel p = personnelService.get(pid);
        AffectationPersonnel ap = new AffectationPersonnel();
        ap.setPersonnel(p);
        remplirAffectation(in, ap);
        AffectationPersonnel saved = apRepo.saveAndFlush(ap);
        rafraichirService(p);
        return saved;
    }

    public AffectationPersonnel modifierAffectation(Long pid, Long id, AffectationPersonnel in) {
        Personnel p = personnelService.get(pid);
        AffectationPersonnel ap = getAp(pid, id);
        remplirAffectation(in, ap);
        AffectationPersonnel saved = apRepo.saveAndFlush(ap);
        rafraichirService(p);
        return saved;
    }

    public void supprimerAffectation(Long pid, Long id) {
        Personnel p = personnelService.get(pid);
        AffectationPersonnel ap = getAp(pid, id);
        apRepo.delete(ap);
        apRepo.flush();
        rafraichirService(p);
    }

    private AffectationPersonnel getAp(Long pid, Long id) {
        return apRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Affectation"));
    }

    private void remplirAffectation(AffectationPersonnel in, AffectationPersonnel ap) {
        Affectation ain = in.getAffectation();
        if (ain == null || ain.getSlug() == null) throw new BusinessException(
            "Sélectionnez une mutation."
        );
        ap.setAffectation(
            affectationRepo.findBySlug(ain.getSlug()).orElseThrow(() -> introuvable("Mutation"))
        );
        if (in.getService() == null || in.getService().getSlug() == null) {
            throw new BusinessException("Sélectionnez le service d'affectation de l'agent.");
        }
        ServiceAp service = serviceRepo
            .findBySlug(in.getService().getSlug())
            .orElseThrow(() -> introuvable("Service"));
        if (!accessScope.estDansPerimetre(service.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Vous ne pouvez pas affecter un agent hors de votre périmètre."
            );
        }
        ap.setService(service);
        if (in.getDateDebut() == null && in.getAnneeDebut() == null) {
            throw new BusinessException("Renseignez la date de début ou l'année de début.");
        }
        ap.copierPeriode(in);
    }

    private void rafraichirService(Personnel p) {
        apRepo
            .findByPersonnelId(p.getId())
            .stream()
            .max(Comparator.comparing(AffectationPersonnel::debutEffectif))
            .ifPresent(ap -> {
                ServiceAp service =
                    ap.getService() != null ? ap.getService() : ap.getAffectation().getService();
                if (service != null) p.setServiceCourant(service);
            });
    }

    /**
     * Historique des mutations d'un agent, du plus récent au plus ancien : chaque événement indique le service
     * d'origine (« depuis »), le service d'arrivée, la décision, les fichiers et les postes occupés.
     */
    @Transactional(readOnly = true)
    public List<MutationEvent> historiqueMutations(Long pid) {
        personnelService.get(pid);
        List<AffectationPersonnel> asc = new ArrayList<>(apRepo.findByPersonnelId(pid));
        asc.sort(Comparator.comparing(AffectationPersonnel::debutEffectif));
        List<MutationEvent> res = new ArrayList<>();
        String precedent = null;
        for (int i = 0; i < asc.size(); i++) {
            AffectationPersonnel ap = asc.get(i);
            Affectation a = ap.getAffectation();
            ServiceAp s = ap.getService() != null ? ap.getService() : a.getService();
            if (s == null) continue;
            List<String> postes = paRepo
                .findByAffectationPersonnelId(ap.getId())
                .stream()
                .sorted(Comparator.comparing(PosteAffectation::debutEffectif))
                .map(x -> x.getPoste().getNom())
                .toList();
            boolean enCours =
                i == asc.size() - 1 && ap.getDateFin() == null && ap.getAnneeFin() == null;
            res.add(
                new MutationEvent(
                    ap.getId(),
                    a.getType().name(),
                    a.getReference(),
                    s.getNom(),
                    s.getType().name(),
                    s.getParent() != null ? s.getParent().getNom() : null,
                    precedent,
                    ap.getDateDebut(),
                    ap.getDateFin(),
                    ap.getAnneeDebut(),
                    ap.getAnneeFin(),
                    ap.getDureeMois(),
                    a.getFichierDecision(),
                    postes,
                    enCours,
                    Roles.ADMINISTRATEUR.equals(currentUser.role()) ? ap.getCreatedBy() : null,
                    Roles.ADMINISTRATEUR.equals(currentUser.role()) ? ap.getCreatedDate() : null
                )
            );
            precedent = s.getNom();
        }

        Collections.reverse(res);
        return res;
    }

    // ===================== Postes d'une affectation =====================

    @Transactional(readOnly = true)
    public List<PosteAffectation> postes(Long pid, Long apid) {
        personnelService.get(pid);
        getAp(pid, apid);
        return trier(paRepo.findByAffectationPersonnelId(apid));
    }

    public PosteAffectation ajouterPoste(Long pid, Long apid, PosteAffectation in) {
        personnelService.get(pid);
        PosteAffectation pa = new PosteAffectation();
        pa.setAffectationPersonnel(getAp(pid, apid));
        remplirPoste(in, pa);
        return paRepo.saveAndFlush(pa);
    }

    public PosteAffectation modifierPoste(Long pid, Long apid, Long id, PosteAffectation in) {
        personnelService.get(pid);
        getAp(pid, apid);
        PosteAffectation pa = paRepo
            .findById(id)
            .filter(x -> x.getAffectationPersonnel().getId().equals(apid))
            .orElseThrow(() -> introuvable("Poste"));
        String ancienFichier = pa.getFichier();
        remplirPoste(in, pa);
        PosteAffectation saved = paRepo.saveAndFlush(pa);
        if (StringUtils.hasText(ancienFichier) && !ancienFichier.equals(saved.getFichier())) {
            storage.supprimer(ancienFichier);
        }
        return saved;
    }

    public void supprimerPoste(Long pid, Long apid, Long id) {
        personnelService.get(pid);
        getAp(pid, apid);
        PosteAffectation pa = paRepo
            .findById(id)
            .filter(x -> x.getAffectationPersonnel().getId().equals(apid))
            .orElseThrow(() -> introuvable("Poste"));
        String fichier = pa.getFichier();
        paRepo.delete(pa);
        paRepo.flush();
        storage.supprimer(fichier);
    }

    // ===================== Sanctions =====================

    @Transactional(readOnly = true)
    public List<SanctionPersonnel> sanctions(Long pid) {
        personnelService.get(pid);
        return sanctionRepo.findByPersonnelIdOrderByDateSanctionDescIdDesc(pid);
    }

    public SanctionPersonnel ajouterSanction(Long pid, SanctionPersonnel in) {
        SanctionPersonnel sanction = new SanctionPersonnel();
        sanction.setPersonnel(personnelService.get(pid));
        remplirSanction(in, sanction);
        return sanctionRepo.saveAndFlush(sanction);
    }

    public SanctionPersonnel modifierSanction(Long pid, Long id, SanctionPersonnel in) {
        personnelService.get(pid);
        SanctionPersonnel sanction = sanctionRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Sanction"));
        String ancienFichier = sanction.getFichier();
        remplirSanction(in, sanction);
        SanctionPersonnel saved = sanctionRepo.saveAndFlush(sanction);
        if (StringUtils.hasText(ancienFichier) && !ancienFichier.equals(saved.getFichier())) {
            storage.supprimer(ancienFichier);
        }
        return saved;
    }

    public void supprimerSanction(Long pid, Long id) {
        personnelService.get(pid);
        SanctionPersonnel sanction = sanctionRepo
            .findById(id)
            .filter(x -> x.getPersonnel().getId().equals(pid))
            .orElseThrow(() -> introuvable("Sanction"));
        String fichier = sanction.getFichier();
        sanctionRepo.delete(sanction);
        sanctionRepo.flush();
        if (StringUtils.hasText(fichier)) storage.supprimer(fichier);
    }

    private void remplirSanction(SanctionPersonnel in, SanctionPersonnel sanction) {
        sanction.setNature(in.getNature().trim());
        sanction.setDateSanction(in.getDateSanction());
        sanction.setMotif(in.getMotif().trim());
        sanction.setReference(
            StringUtils.hasText(in.getReference()) ? in.getReference().trim() : null
        );
        if (StringUtils.hasText(in.getFichier()) && !storage.existe(in.getFichier())) {
            throw new BusinessException("Le fichier de la sanction est introuvable : renvoyez-le.");
        }
        sanction.setFichier(in.getFichier());
    }

    private void remplirPoste(PosteAffectation in, PosteAffectation pa) {
        Poste pin = in.getPoste();
        if (pin == null) throw new BusinessException("Le poste est obligatoire.");
        if (StringUtils.hasText(pin.getNom())) {
            String nom = pin.getNom().trim();
            pa.setPoste(
                posteRepo.findByNomIgnoreCase(nom).orElseGet(() -> {
                    Poste n = new Poste();
                    n.setNom(nom);
                    return posteRepo.save(n);
                })
            );
        } else if (pin.getSlug() != null) {
            pa.setPoste(
                posteRepo.findBySlug(pin.getSlug()).orElseThrow(() -> introuvable("Poste"))
            );
        } else {
            throw new BusinessException("Le nom du poste est obligatoire.");
        }
        pa.setDescription(in.getDescription());
        if (!StringUtils.hasText(in.getFichier())) {
            throw new BusinessException("La pièce justificative du poste est obligatoire.");
        }
        if (!storage.existe(in.getFichier())) {
            throw new BusinessException("La pièce justificative est introuvable : renvoyez-la.");
        }
        pa.setFichier(in.getFichier());
        if (in.getDateDebut() == null) throw new BusinessException(
            "La date de début du poste est obligatoire."
        );
        pa.copierPeriode(in);
    }
}
