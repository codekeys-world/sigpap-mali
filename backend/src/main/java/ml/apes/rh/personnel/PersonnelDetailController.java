package ml.apes.rh.personnel;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.*;
import ml.apes.rh.personnel.Dtos.MutationEvent;
import ml.apes.rh.personnel.PersonnelDetailService;
import ml.apes.rh.security.Acces;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Toutes ces actions se font depuis l'affichage des informations d'un surveillant. */
@RestController
@RequestMapping("/api/personnels/{pid}")
@RequiredArgsConstructor
public class PersonnelDetailController {

    private final PersonnelDetailService s;

    // ---- historique des grades ----
    @GetMapping("/historiques-grades")
    @PreAuthorize(Acces.TOUS)
    public List<HistoriqueGrade> grades(@PathVariable String pid) {
        return s.grades(s.personnelId(pid));
    }

    @PostMapping("/historiques-grades")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public HistoriqueGrade ajouterGrade(
        @PathVariable String pid,
        @Valid @RequestBody HistoriqueGrade b
    ) {
        return s.ajouterGrade(s.personnelId(pid), b);
    }

    @PutMapping("/historiques-grades/{id}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public HistoriqueGrade modifierGrade(
        @PathVariable String pid,
        @PathVariable String id,
        @Valid @RequestBody HistoriqueGrade b
    ) {
        return s.modifierGrade(s.personnelId(pid), s.idRessource("grade", id), b);
    }

    @DeleteMapping("/historiques-grades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerGrade(@PathVariable String pid, @PathVariable String id) {
        s.supprimerGrade(s.personnelId(pid), s.idRessource("grade", id));
    }

    // ---- formations ----
    @GetMapping("/formations")
    @PreAuthorize(Acces.TOUS)
    public List<FaireFormation> formations(@PathVariable String pid) {
        return s.formations(s.personnelId(pid));
    }

    @PostMapping("/formations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public FaireFormation ajouterFormation(
        @PathVariable String pid,
        @Valid @RequestBody FaireFormation b
    ) {
        return s.ajouterFormation(s.personnelId(pid), b);
    }

    @PutMapping("/formations/{id}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public FaireFormation modifierFormation(
        @PathVariable String pid,
        @PathVariable String id,
        @Valid @RequestBody FaireFormation b
    ) {
        return s.modifierFormation(s.personnelId(pid), s.idRessource("formation", id), b);
    }

    @DeleteMapping("/formations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerFormation(@PathVariable String pid, @PathVariable String id) {
        s.supprimerFormation(s.personnelId(pid), s.idRessource("formation", id));
    }

    // ---- études ----
    @GetMapping("/etudes")
    @PreAuthorize(Acces.TOUS)
    public List<FaireEtude> etudes(@PathVariable String pid) {
        return s.etudes(s.personnelId(pid));
    }

    @PostMapping("/etudes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public FaireEtude ajouterEtude(@PathVariable String pid, @Valid @RequestBody FaireEtude b) {
        return s.ajouterEtude(s.personnelId(pid), b);
    }

    @PutMapping("/etudes/{id}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public FaireEtude modifierEtude(
        @PathVariable String pid,
        @PathVariable String id,
        @Valid @RequestBody FaireEtude b
    ) {
        return s.modifierEtude(s.personnelId(pid), s.idRessource("etude", id), b);
    }

    @DeleteMapping("/etudes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerEtude(@PathVariable String pid, @PathVariable String id) {
        s.supprimerEtude(s.personnelId(pid), s.idRessource("etude", id));
    }

    // ---- affectations ----
    @GetMapping("/affectations")
    @PreAuthorize(Acces.TOUS)
    public List<AffectationPersonnel> affectations(@PathVariable String pid) {
        return s.affectations(s.personnelId(pid));
    }

    @PostMapping("/affectations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public AffectationPersonnel ajouterAffectation(
        @PathVariable String pid,
        @Valid @RequestBody AffectationPersonnel b
    ) {
        return s.ajouterAffectation(s.personnelId(pid), b);
    }

    @PutMapping("/affectations/{id}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public AffectationPersonnel modifierAffectation(
        @PathVariable String pid,
        @PathVariable String id,
        @Valid @RequestBody AffectationPersonnel b
    ) {
        return s.modifierAffectation(s.personnelId(pid), s.idRessource("affectation", id), b);
    }

    @DeleteMapping("/affectations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerAffectation(@PathVariable String pid, @PathVariable String id) {
        s.supprimerAffectation(s.personnelId(pid), s.idRessource("affectation", id));
    }

    // ---- historique des mutations (timeline) ----
    @GetMapping("/historique-mutations")
    @PreAuthorize(Acces.TOUS)
    public List<MutationEvent> historiqueMutations(@PathVariable String pid) {
        return s.historiqueMutations(s.personnelId(pid));
    }

    // ---- postes occupés pendant une affectation ----
    @GetMapping("/affectations/{apid}/postes")
    @PreAuthorize(Acces.TOUS)
    public List<PosteAffectation> postes(@PathVariable String pid, @PathVariable String apid) {
        return s.postes(s.personnelId(pid), s.idRessource("affectation", apid));
    }

    @PostMapping("/affectations/{apid}/postes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public PosteAffectation ajouterPoste(
        @PathVariable String pid,
        @PathVariable String apid,
        @Valid @RequestBody PosteAffectation b
    ) {
        return s.ajouterPoste(s.personnelId(pid), s.idRessource("affectation", apid), b);
    }

    @PutMapping("/affectations/{apid}/postes/{id}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public PosteAffectation modifierPoste(
        @PathVariable String pid,
        @PathVariable String apid,
        @PathVariable String id,
        @Valid @RequestBody PosteAffectation b
    ) {
        return s.modifierPoste(
            s.personnelId(pid),
            s.idRessource("affectation", apid),
            s.idRessource("poste", id),
            b
        );
    }

    @DeleteMapping("/affectations/{apid}/postes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerPoste(
        @PathVariable String pid,
        @PathVariable String apid,
        @PathVariable String id
    ) {
        s.supprimerPoste(
            s.personnelId(pid),
            s.idRessource("affectation", apid),
            s.idRessource("poste", id)
        );
    }

    // ---- sanctions ----
    @GetMapping("/sanctions")
    @PreAuthorize(Acces.TOUS)
    public List<SanctionPersonnel> sanctions(@PathVariable String pid) {
        return s.sanctions(s.personnelId(pid));
    }

    @PostMapping("/sanctions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public SanctionPersonnel ajouterSanction(
        @PathVariable String pid,
        @Valid @RequestBody SanctionPersonnel b
    ) {
        return s.ajouterSanction(s.personnelId(pid), b);
    }

    @PutMapping("/sanctions/{id}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public SanctionPersonnel modifierSanction(
        @PathVariable String pid,
        @PathVariable String id,
        @Valid @RequestBody SanctionPersonnel b
    ) {
        return s.modifierSanction(s.personnelId(pid), s.idRessource("sanction", id), b);
    }

    @DeleteMapping("/sanctions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerSanction(@PathVariable String pid, @PathVariable String id) {
        s.supprimerSanction(s.personnelId(pid), s.idRessource("sanction", id));
    }
}
