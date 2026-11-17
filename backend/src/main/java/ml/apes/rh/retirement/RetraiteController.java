package ml.apes.rh.retirement;

import lombok.RequiredArgsConstructor;
import ml.apes.rh.personnel.ExportService;
import ml.apes.rh.retirement.Dtos.RetraiteLigne;
import ml.apes.rh.retirement.RetraiteService;
import ml.apes.rh.security.Acces;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/retraites")
@RequiredArgsConstructor
public class RetraiteController {

    private final RetraiteService service;
    private final ExportService exportService;

    @GetMapping
    @PreAuthorize(Acces.TOUS)
    public Page<RetraiteLigne> lister(
        @RequestParam(defaultValue = "TOUS") String etat,
        @RequestParam(defaultValue = "24") int horizon,
        @RequestParam(required = false) String q,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        return service.lister(etat, horizon, q, pageable);
    }

    @GetMapping("/export")
    @PreAuthorize(Acces.TOUS)
    public ResponseEntity<byte[]> exporter(
        @RequestParam(defaultValue = "TOUS") String etat,
        @RequestParam(defaultValue = "24") int horizon,
        @RequestParam(required = false) String q
    ) {
        byte[] data = exportService.retraites(service.tous(etat, horizon, q));
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("suivi-retraites-apes.xlsx")
                    .build()
                    .toString()
            )
            .contentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
            .body(data);
    }
}
