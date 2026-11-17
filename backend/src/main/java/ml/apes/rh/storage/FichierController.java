package ml.apes.rh.storage;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.personnel.PersonnelService;
import ml.apes.rh.security.Acces;
import ml.apes.rh.security.CurrentUserService;
import ml.apes.rh.security.Roles;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/fichiers")
@RequiredArgsConstructor
public class FichierController {

    private final FileStorageService storage;
    private final PersonnelService personnelService;
    private final CurrentUserService currentUser;

    /** Envoi d'un fichier (pièce justificative, mutation, décision…). Retourne le chemin à enregistrer en base. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(Acces.GESTIONNAIRES)
    public Map<String, Object> envoyer(
        @RequestParam("file") MultipartFile file,
        @RequestParam(defaultValue = "documents") String dossier
    ) {
        FileStorageService.Stocke s = storage.stocker(file, dossier);
        return Map.of("chemin", s.chemin(), "nom", s.nom(), "taille", s.taille());
    }

    /** Affichage dans le navigateur. */
    @GetMapping("/voir")
    @PreAuthorize(Acces.TOUS)
    public ResponseEntity<Resource> voir(@RequestParam String chemin) {
        return reponse(chemin, false);
    }

    /** Téléchargement (en-tête Content-Disposition: attachment) sous le nom d'origine du fichier. */
    @GetMapping("/telecharger")
    @PreAuthorize(Acces.TOUS)
    public ResponseEntity<Resource> telecharger(@RequestParam String chemin) {
        return reponse(chemin, true);
    }

    private ResponseEntity<Resource> reponse(String chemin, boolean attachment) {
        if (
            Roles.UTILISATEUR.equals(currentUser.role()) &&
            !personnelService.fichierAccessibleParUtilisateur(chemin)
        ) {
            throw new AccessDeniedException(
                "Vous ne pouvez consulter que les fichiers de votre propre dossier."
            );
        }
        Resource r = storage.charger(chemin);
        ContentDisposition cd = (
            attachment ? ContentDisposition.attachment() : ContentDisposition.inline()
        )
            .filename(storage.nomAffichage(chemin), java.nio.charset.StandardCharsets.UTF_8)
            .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
            .contentType(
                org.springframework.http.MediaTypeFactory.getMediaType(r).orElse(
                    MediaType.APPLICATION_OCTET_STREAM
                )
            )
            .body(r);
    }
}
