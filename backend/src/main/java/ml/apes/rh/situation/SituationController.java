package ml.apes.rh.situation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.ChangementSituation;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.security.Acces;
import ml.apes.rh.situation.Dtos.DemandeSituation;
import ml.apes.rh.situation.SituationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/personnels/{slug}")
@RequiredArgsConstructor
public class SituationController {

    private final SituationService service;

    @GetMapping("/situations")
    @PreAuthorize(Acces.TOUS)
    public List<ChangementSituation> historique(@PathVariable String slug) {
        return service.historique(service.personnelId(slug));
    }

    /** Décès / retraite : administrateur et RH ; maladie, abandon de poste, reprise : tous les gestionnaires (contrôlé dans le service). */
    @PostMapping("/situation")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public Personnel changer(@PathVariable String slug, @Valid @RequestBody DemandeSituation req) {
        return service.changer(service.personnelId(slug), req);
    }
}
