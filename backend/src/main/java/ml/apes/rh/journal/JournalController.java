package ml.apes.rh.journal;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.ActivityLog;
import ml.apes.rh.security.Acces;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final ActivityLogRepository repository;

    /** Consultation de toutes les actions du système : administrateur uniquement. */
    @GetMapping
    @PreAuthorize(Acces.ADMIN)
    public Page<ActivityLog> lister(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String resultat,
        @RequestParam(required = false) String utilisateur,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate debut,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate fin,
        @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable p
    ) {
        return repository.findAll(
            JournalSpecs.build(q, action, resultat, utilisateur, debut, fin),
            p
        );
    }
}
