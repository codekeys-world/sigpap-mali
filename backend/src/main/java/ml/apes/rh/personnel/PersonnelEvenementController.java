package ml.apes.rh.personnel;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.CongeAbsencePersonnel;
import ml.apes.rh.domain.DistinctionPersonnel;
import ml.apes.rh.security.Acces;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/personnels/{personnelSlug}")
@RequiredArgsConstructor
public class PersonnelEvenementController {

    private final PersonnelEvenementService service;

    @GetMapping("/conges-absences")
    @PreAuthorize(Acces.TOUS)
    public List<CongeAbsencePersonnel> conges(@PathVariable String personnelSlug) {
        return service.conges(personnelSlug);
    }

    @PostMapping("/conges-absences")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public CongeAbsencePersonnel ajouterConge(
        @PathVariable String personnelSlug,
        @Valid @RequestBody CongeAbsencePersonnel demande
    ) {
        return service.ajouterConge(personnelSlug, demande);
    }

    @PutMapping("/conges-absences/{slug}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public CongeAbsencePersonnel modifierConge(
        @PathVariable String personnelSlug,
        @PathVariable String slug,
        @Valid @RequestBody CongeAbsencePersonnel demande
    ) {
        return service.modifierConge(personnelSlug, slug, demande);
    }

    @DeleteMapping("/conges-absences/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerConge(@PathVariable String personnelSlug, @PathVariable String slug) {
        service.supprimerConge(personnelSlug, slug);
    }

    @GetMapping("/distinctions")
    @PreAuthorize(Acces.TOUS)
    public List<DistinctionPersonnel> distinctions(@PathVariable String personnelSlug) {
        return service.distinctions(personnelSlug);
    }

    @PostMapping("/distinctions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public DistinctionPersonnel ajouterDistinction(
        @PathVariable String personnelSlug,
        @Valid @RequestBody DistinctionPersonnel demande
    ) {
        return service.ajouterDistinction(personnelSlug, demande);
    }

    @PutMapping("/distinctions/{slug}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public DistinctionPersonnel modifierDistinction(
        @PathVariable String personnelSlug,
        @PathVariable String slug,
        @Valid @RequestBody DistinctionPersonnel demande
    ) {
        return service.modifierDistinction(personnelSlug, slug, demande);
    }

    @DeleteMapping("/distinctions/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerDistinction(
        @PathVariable String personnelSlug,
        @PathVariable String slug
    ) {
        service.supprimerDistinction(personnelSlug, slug);
    }
}
