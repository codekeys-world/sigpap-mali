package ml.apes.rh.personnel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.exception.ResourceNotFoundException;
import ml.apes.rh.security.Acces;
import ml.apes.rh.security.CurrentUserService;
import ml.apes.rh.storage.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/personnels")
@RequiredArgsConstructor
public class PersonnelController {

    private final PersonnelService service;
    private final ExportService exportService;
    private final CarteService carteService;
    private final FileStorageService storage;
    private final FicheService ficheService;
    private final PersonnelEvenementService evenements;
    private final PersonnelDetailService detail;
    private final CurrentUserService currentUser;
    private final SignatureDirecteurService signatureDirecteur;

    @GetMapping
    @PreAuthorize(Acces.TOUS)
    public Page<Personnel> lister(
        PersonnelFilter filtre,
        @PageableDefault(size = 10, sort = { "nom", "prenom" }) Pageable pageable
    ) {
        return service.rechercher(filtre, pageable);
    }

    @GetMapping("/export")
    @PreAuthorize(Acces.TOUS)
    public ResponseEntity<byte[]> exporter(
        @RequestParam(defaultValue = "xlsx") String format,
        PersonnelFilter filtre
    ) {
        var liste = service.rechercherTous(filtre);
        byte[] data;
        String nom;
        MediaType type;
        switch (format.toLowerCase()) {
            case "xlsx" -> {
                data = exportService.excel(liste, exportService.criteres(filtre));
                nom = "personnel-ap.xlsx";
                type = MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );
            }
            case "docx" -> {
                data = exportService.word(liste);
                nom = "personnel-ap.docx";
                type = MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                );
            }
            default -> throw new BusinessException(
                "Format d'export non supporté : utilisez xlsx ou docx."
            );
        }
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(nom).build().toString()
            )
            .contentType(type)
            .body(data);
    }

    @GetMapping("/{slug}")
    @PreAuthorize(Acces.TOUS)
    public Personnel get(@PathVariable String slug) {
        return service.getBySlug(slug);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public Personnel creer(@Valid @RequestBody Personnel body) {
        return service.creer(body);
    }

    @PutMapping("/{slug}")
    @PreAuthorize(Acces.GESTIONNAIRES)
    public Personnel modifier(@PathVariable String slug, @Valid @RequestBody Personnel body) {
        return service.modifierParSlug(slug, body);
    }

    @DeleteMapping("/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimer(@PathVariable String slug) {
        service.supprimerParSlug(slug);
    }

    @GetMapping("/{slug}/carte")
    @PreAuthorize(Acces.CARTES_SERVICE)
    public ResponseEntity<byte[]> carte(@PathVariable String slug) {
        Personnel p = service.getBySlug(slug);
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.inline()
                    .filename("carte-" + p.getMatricule() + ".pdf")
                    .build()
                    .toString()
            )
            .contentType(MediaType.APPLICATION_PDF)
            .body(
                carteService.generer(
                    p,
                    p.getPhoto() != null ? storage.lireOctets(p.getPhoto()) : null,
                    signatureDirecteur.lire(),
                    signatureDirecteur.lireNom()
                )
            );
    }

    @GetMapping("/{slug}/carte/qr")
    @PreAuthorize(Acces.CARTES_SERVICE)
    public ResponseEntity<byte[]> qrCarte(@PathVariable String slug) {
        Personnel p = service.getBySlug(slug);
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.IMAGE_PNG)
            .body(carteService.genererQr(p.getNumeroCarte()));
    }

    @GetMapping("/signature-directeur")
    @PreAuthorize(Acces.TOUS)
    public ResponseEntity<byte[]> signatureDirecteur() {
        byte[] signature = signatureDirecteur.lire();
        if (signature == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.IMAGE_PNG)
            .body(signature);
    }

    @GetMapping("/signature-directeur/nom")
    @PreAuthorize(Acces.TOUS)
    public ResponseEntity<String> nomDirecteur() {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .contentType(MediaType.TEXT_PLAIN)
            .body(signatureDirecteur.lireNom());
    }

    @PutMapping("/signature-directeur/nom")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.ADMIN_RH)
    public void enregistrerNomDirecteur(@RequestParam String nom) {
        signatureDirecteur.enregistrerNom(nom);
    }

    @PostMapping(value = "/signature-directeur", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.ADMIN_RH)
    public void enregistrerSignatureDirecteur(@RequestParam("file") MultipartFile file) {
        signatureDirecteur.enregistrer(file);
    }

    /** Fiche individuelle de carrière (PDF) : consultable par tous les rôles, dans leur périmètre. */
    @GetMapping("/{slug}/fiche")
    @PreAuthorize(Acces.TOUS)
    public ResponseEntity<byte[]> fiche(@PathVariable String slug) {
        Personnel p = service.getBySlug(slug);
        byte[] photo = p.getPhoto() != null ? storage.lireOctets(p.getPhoto()) : null;
        Personnel lecteur = currentUser.get();
        byte[] pdf = ficheService.generer(
            p,
            photo,
            detail.grades(p.getId()),
            detail.historiqueMutations(p.getId()),
            detail.formations(p.getId()),
            detail.etudes(p.getId()),
            detail.sanctions(p.getId()),
            evenements.conges(slug),
            evenements.distinctions(slug),
            lecteur.getPrenom() + " " + lecteur.getNom() + " (" + lecteur.getMatricule() + ")"
        );
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.inline()
                    .filename("fiche-" + p.getMatricule() + ".pdf")
                    .build()
                    .toString()
            )
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    // ---------- photo de l'agent ----------

    @PostMapping(value = "/{slug}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public Personnel changerPhoto(
        @PathVariable String slug,
        @RequestParam("file") MultipartFile file
    ) {
        return service.changerPhoto(service.idParSlug(slug), file);
    }

    @GetMapping("/{slug}/photo")
    @PreAuthorize(Acces.TOUS)
    public ResponseEntity<byte[]> photo(
        @PathVariable String slug,
        @RequestParam(defaultValue = "false") boolean telecharger
    ) {
        Personnel p = service.getBySlug(slug);
        byte[] data = p.getPhoto() != null ? storage.lireOctets(p.getPhoto()) : null;
        if (data == null) throw new ResourceNotFoundException("Cet agent n'a pas de photo");
        MediaType type = p.getPhoto().toLowerCase().endsWith(".png")
            ? MediaType.IMAGE_PNG
            : MediaType.IMAGE_JPEG;
        ResponseEntity.BodyBuilder b = ResponseEntity.ok().contentType(type);
        if (telecharger) {
            // bouton « Télécharger la photo »
            String ext = p.getPhoto().substring(p.getPhoto().lastIndexOf('.') + 1);
            b.header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("photo-" + p.getMatricule() + "." + ext)
                    .build()
                    .toString()
            );
        }
        return b.body(data);
    }

    @DeleteMapping("/{slug}/photo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public void supprimerPhoto(@PathVariable String slug) {
        service.supprimerPhoto(service.idParSlug(slug));
    }
}
