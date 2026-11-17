package ml.apes.rh.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.AffectationRepository;
import ml.apes.rh.catalog.CorpsRepository;
import ml.apes.rh.catalog.EtudeRepository;
import ml.apes.rh.catalog.FormationRepository;
import ml.apes.rh.catalog.GradeRepository;
import ml.apes.rh.catalog.NiveauEtudeRepository;
import ml.apes.rh.catalog.PosteRepository;
import ml.apes.rh.catalog.ServiceApRepository;
import ml.apes.rh.dashboard.Dtos.*;
import ml.apes.rh.domain.*;
import ml.apes.rh.journal.ActivityLogRepository;
import ml.apes.rh.personnel.AffectationPersonnelRepository;
import ml.apes.rh.personnel.FaireEtudeRepository;
import ml.apes.rh.personnel.FaireFormationRepository;
import ml.apes.rh.personnel.PersonnelFilter;
import ml.apes.rh.personnel.PersonnelRepository;
import ml.apes.rh.personnel.PersonnelSpecs;
import ml.apes.rh.retirement.RetraiteService;
import ml.apes.rh.security.AccessScopeService;
import ml.apes.rh.security.CurrentUserService;
import ml.apes.rh.security.Roles;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final PersonnelRepository personnelRepo;
    private final AffectationPersonnelRepository apRepo;
    private final FaireFormationRepository ffRepo;
    private final FaireEtudeRepository feRepo;
    private final CorpsRepository corpsRepo;
    private final GradeRepository gradeRepo;
    private final ServiceApRepository serviceRepo;
    private final FormationRepository formationRepo;
    private final EtudeRepository etudeRepo;
    private final NiveauEtudeRepository niveauEtudeRepo;
    private final AffectationRepository affectationRepo;
    private final PosteRepository posteRepo;
    private final ActivityLogRepository logRepo;
    private final AccessScopeService scope;
    private final CurrentUserService current;
    private final RetraiteService retraiteService;

    private static List<Stat> stats(Map<String, Long> m, int limit, boolean tri) {
        var s = m.entrySet().stream();
        if (tri) s = s.sorted(Map.Entry.<String, Long>comparingByValue().reversed());
        return s
            .limit(limit)
            .map(e -> new Stat(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    /** Classe les agents par tranche selon une valeur numérique (null => « Non renseigné »). */
    private static Map<String, Long> tranches(
        List<Personnel> all,
        Function<Personnel, Integer> valeur,
        int[] bornes,
        String[] libelles
    ) {
        Map<String, Long> res = new LinkedHashMap<>();
        for (String l : libelles) res.put(l, 0L);
        res.put("Non renseigné", 0L);
        for (Personnel p : all) {
            Integer v = valeur.apply(p);
            String l = "Non renseigné";
            if (v != null) {
                int i = 0;
                while (i < bornes.length && v >= bornes[i]) i++;
                l = libelles[i];
            }
            res.merge(l, 1L, Long::sum);
        }
        return res;
    }

    private static List<Stat> parAnnee(Map<Integer, Long> m) {
        return m
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .skip(Math.max(0, m.size() - 10))
            .map(e -> new Stat(String.valueOf(e.getKey()), e.getValue()))
            .collect(Collectors.toList());
    }

    public Dashboard construire() {
        Set<Long> ids = scope.idsAutorises();
        List<Personnel> all = personnelRepo.findAll(
            PersonnelSpecs.build(new PersonnelFilter(), ids, scope.personnelIdSeul())
        );
        Set<Long> pids = all.stream().map(Personnel::getId).collect(Collectors.toSet());
        LocalDate today = LocalDate.now();

        // ---- répartitions de base ----
        Map<String, long[]> corps = new TreeMap<>();
        Map<String, String> corpsSlugs = new HashMap<>();
        Map<String, Long> grades = new HashMap<>();
        Map<String, Long> types = new HashMap<>();
        Map<String, Long> services = new HashMap<>();
        Map<Integer, Long> recrutements = new HashMap<>();
        for (Personnel p : all) {
            Corps corpsActuel =
                p.getCorpsActuel() != null
                    ? p.getCorpsActuel()
                    : p.getGradeActuel() != null
                      ? p.getGradeActuel().getCorps()
                      : null;
            String c = corpsActuel != null ? corpsActuel.getNom() : "Non renseigné";
            if (corpsActuel != null) corpsSlugs.put(c, corpsActuel.getSlug());
            long[] a = corps.computeIfAbsent(c, k -> new long[2]);
            if (p.getSexe() == Sexe.M) a[0]++;
            else a[1]++;
            grades.merge(
                p.isSurveillant()
                    ? p.getGradeActuel() != null
                        ? p.getGradeActuel().getNom()
                        : "Grade non renseigné"
                    : "Sans grade",
                1L,
                Long::sum
            );
            types.merge(
                p.getServiceCourant() != null
                    ? p.getServiceCourant().getType().name()
                    : "NON_RENSEIGNE",
                1L,
                Long::sum
            );
            services.merge(
                p.getServiceCourant() != null ? p.getServiceCourant().getNom() : "Non renseigné",
                1L,
                Long::sum
            );
            if (p.getDateRecrutement() != null) recrutements.merge(
                p.getDateRecrutement().getYear(),
                1L,
                Long::sum
            );
        }
        List<CorpsStat> parCorps = corps
            .entrySet()
            .stream()
            .map(e ->
                new CorpsStat(
                    corpsSlugs.get(e.getKey()),
                    e.getKey(),
                    e.getValue()[0],
                    e.getValue()[1]
                )
            )
            .collect(Collectors.toList());

        // ---- âge et ancienneté ----
        Map<String, Long> age = tranches(
            all,
            p ->
                p.getDateNaissance() == null
                    ? null
                    : Period.between(p.getDateNaissance(), today).getYears(),
            new int[] { 30, 40, 50, 60 },
            new String[] { "< 30 ans", "30–39 ans", "40–49 ans", "50–59 ans", "60 ans et +" }
        );
        Map<String, Long> anc = tranches(
            all,
            p ->
                p.getDateRecrutement() == null
                    ? null
                    : Period.between(p.getDateRecrutement(), today).getYears(),
            new int[] { 5, 10, 20 },
            new String[] { "< 5 ans", "5–9 ans", "10–19 ans", "20 ans et +" }
        );
        age.values().removeIf(v -> v == 0); // on n'affiche pas les tranches vides
        anc.values().removeIf(v -> v == 0);

        // ---- affectations, formations, études (dans le périmètre) ----
        List<AffectationPersonnel> aps = pids.isEmpty()
            ? List.of()
            : apRepo.findByPersonnelIdIn(pids);
        List<FaireFormation> ffs = pids.isEmpty() ? List.of() : ffRepo.findByPersonnelIdIn(pids);
        List<FaireEtude> fes = pids.isEmpty() ? List.of() : feRepo.findByPersonnelIdIn(pids);

        Map<Long, AffectationPersonnel> derniere = new HashMap<>();
        Map<Integer, Long> mutations = new HashMap<>();
        for (AffectationPersonnel ap : aps) {
            derniere.merge(ap.getPersonnel().getId(), ap, (x, y) ->
                y.debutEffectif().isAfter(x.debutEffectif()) ? y : x
            );
            if (ap.debutEffectif().getYear() > 1900) mutations.merge(
                ap.debutEffectif().getYear(),
                1L,
                Long::sum
            );
        }
        long enCours = derniere
            .values()
            .stream()
            .filter(ap -> ap.getDateFin() == null && ap.getAnneeFin() == null)
            .count();
        Map<String, Long> formations = ffs
            .stream()
            .collect(
                Collectors.groupingBy(f -> f.getFormation().getTitre(), Collectors.counting())
            );
        Map<String, Long> etudes = fes
            .stream()
            .collect(
                Collectors.groupingBy(e -> e.getEtude().getNiveauEtude(), Collectors.counting())
            );

        // ---- situations et suivi des retraites ----
        long retraites = all
            .stream()
            .filter(p -> p.getSituation() == SituationAgent.RETRAITE)
            .count();
        long depassees = 0,
            dans12 = 0,
            dans24 = 0;
        Map<String, Long> departs = new LinkedHashMap<>();
        for (int y = today.getYear(); y <= today.getYear() + 5; y++) departs.put(
            String.valueOf(y),
            0L
        );
        for (Personnel p : all) {
            if (p.isDecede() || p.getSituation() == SituationAgent.RETRAITE) continue;
            LocalDate prevue = retraiteService.datePrevue(p);
            if (prevue == null) continue;
            if (prevue.isBefore(today)) {
                depassees++;
                continue;
            }
            if (!prevue.isAfter(today.plusMonths(12))) dans12++;
            if (!prevue.isAfter(today.plusMonths(24))) dans24++;
            departs.computeIfPresent(String.valueOf(prevue.getYear()), (k, v) -> v + 1);
        }

        // ---- qualité des données ----
        Qualite qualite = new Qualite(
            all
                .stream()
                .filter(p -> p.getPhoto() == null)
                .count(),
            all
                .stream()
                .filter(p ->
                    p.isSurveillant() ? p.getGradeActuel() == null : p.getCorpsActuel() == null
                )
                .count(),
            all
                .stream()
                .filter(p -> p.getServiceCourant() == null)
                .count(),
            all
                .stream()
                .filter(
                    p ->
                        (p.getTelephone() == null || p.getTelephone().isBlank()) &&
                        (p.getEmail() == null || p.getEmail().isBlank())
                )
                .count(),
            all
                .stream()
                .filter(p -> !p.isActif() && !p.isDecede())
                .count(),
            all.stream().filter(Personnel::isMotDePasseTemporaire).count()
        );

        // ---- activité du jour (administrateurs uniquement) ----
        String role = current.role();
        Activite activite = null;
        if (Roles.ADMINISTRATEUR.equals(role)) {
            LocalDateTime debut = today.atStartOfDay();
            activite = new Activite(
                logRepo.countByDateActionGreaterThanEqual(debut),
                logRepo.countByActionAndDateActionGreaterThanEqual("CONNEXION", debut),
                logRepo.countByActionAndDateActionGreaterThanEqual("ECHEC_CONNEXION", debut)
            );
        }

        // ---- listes récentes ----
        Comparator<Personnel> parDateCreation = Comparator.comparing(
            Personnel::getCreatedDate,
            Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder())
        );
        List<AgentRecent> recents = all
            .stream()
            .sorted(parDateCreation.reversed())
            .limit(6)
            .map(p ->
                new AgentRecent(
                    p.getSlug(),
                    p.getPrenom() + " " + p.getNom(),
                    p.getMatricule(),
                    p.getGradeActuel() != null ? p.getGradeActuel().getNom() : null,
                    p.getPhoto(),
                    Roles.ADMINISTRATEUR.equals(current.role()) ? p.getCreatedDate() : null,
                    Roles.ADMINISTRATEUR.equals(current.role()) ? p.getCreatedBy() : null
                )
            )
            .collect(Collectors.toList());
        Long personnelId = scope.personnelIdSeul();
        boolean dossierPersonnel = personnelId != null;
        Specification<AffectationPersonnel> sp = (r, q, cb) ->
            personnelId != null
                ? cb.equal(r.get("personnel").get("id"), personnelId)
                : ids == null
                  ? cb.conjunction()
                  : ids.isEmpty()
                    ? cb.disjunction()
                    : r.get("personnel").get("serviceCourant").get("id").in(ids);
        List<AffectationRecente> affs = apRepo
            .findAll(sp, PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "id")))
            .getContent()
            .stream()
            .map(ap ->
                new AffectationRecente(
                    ap.getSlug(),
                    ap.getPersonnel().getSlug(),
                    ap.getPersonnel().getPrenom() + " " + ap.getPersonnel().getNom(),
                    ap.getService() != null
                        ? ap.getService().getNom()
                        : ap.getAffectation().getService() != null
                          ? ap.getAffectation().getService().getNom()
                          : null,
                    ap.getAffectation().getType().name(),
                    ap.getDateDebut(),
                    Roles.ADMINISTRATEUR.equals(current.role()) ? ap.getCreatedBy() : null
                )
            )
            .collect(Collectors.toList());

        return new Dashboard(
            all.size(),
            all.stream().filter(Personnel::isSurveillant).count(),
            all.stream().filter(Personnel::isEnFormation).count(),
            all.stream().filter(Personnel::isDecede).count(),
            all.stream().filter(Personnel::isActif).count(),
            all
                .stream()
                .filter(p -> p.getSexe() == Sexe.M)
                .count(),
            all
                .stream()
                .filter(p -> p.getSexe() == Sexe.F)
                .count(),
            parCorps,
            stats(grades, 8, true),
            stats(types, 10, true),
            recents,
            affs,
            dossierPersonnel
                ? new Referentiels(0, 0, 0, 0, 0, 0, 0, 0)
                : new Referentiels(
                      corpsRepo.count(),
                      gradeRepo.count(),
                      serviceRepo.count(),
                      formationRepo.count(),
                      etudeRepo.count(),
                      niveauEtudeRepo.count(),
                      posteRepo.count(),
                      affectationRepo.count()
                  ),
            qualite,
            activite,
            aps.size(),
            enCours,
            stats(age, 10, false),
            stats(anc, 10, false),
            parAnnee(recrutements),
            parAnnee(mutations),
            stats(formations, 6, true),
            stats(etudes, 6, true),
            stats(services, 8, true),
            all
                .stream()
                .filter(p -> p.getSituation() == SituationAgent.MALADE)
                .count(),
            all
                .stream()
                .filter(p -> p.getSituation() == SituationAgent.ABANDON_POSTE)
                .count(),
            new Retraites(depassees, dans12, dans24, retraites, stats(departs, 10, false))
        );
    }
}
