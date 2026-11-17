package ml.apes.rh.importation;

import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.ArreteIntegrationRepository;
import ml.apes.rh.catalog.CorpsRepository;
import ml.apes.rh.catalog.GradeRepository;
import ml.apes.rh.catalog.ServiceApRepository;
import ml.apes.rh.importation.Dtos.ImportResult;
import ml.apes.rh.importation.ImportService;
import ml.apes.rh.security.Acces;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Import Excel des agents : réservé à l'administrateur et au gestionnaire RH. */
@RestController
@RequestMapping("/api/personnels/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService service;
    private final GradeRepository gradeRepo;
    private final ServiceApRepository serviceRepo;
    private final CorpsRepository corpsRepo;
    private final ArreteIntegrationRepository arreteIntegrationRepo;

    @GetMapping("/modele")
    @PreAuthorize(Acces.ADMIN_RH)
    public ResponseEntity<byte[]> modele() {
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("modele-import-agents.xlsx")
                    .build()
                    .toString()
            )
            .contentType(
                MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
            .body(
                service.modele(
                    gradeRepo.findAll(),
                    serviceRepo.findAll(),
                    corpsRepo.findAll(),
                    arreteIntegrationRepo.findAll()
                )
            );
    }

    /** simulation=true : vérifie le fichier sans rien enregistrer. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(Acces.ADMIN_RH)
    public ImportResult importer(
        @RequestParam("file") MultipartFile file,
        @RequestParam(defaultValue = "false") boolean simulation
    ) {
        return service.importer(file, simulation);
    }
}
