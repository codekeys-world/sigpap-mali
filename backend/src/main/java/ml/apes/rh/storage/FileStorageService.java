package ml.apes.rh.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stockage des fichiers sur le serveur, dans le dossier « uploads » (app.upload-dir), classés par sous-dossier :
 * photos/, documents/ (pièces justificatives, mutations, décisions) et justificatifs/ (situations).
 * La base de données ne conserve que le CHEMIN RELATIF du fichier (ex. « documents/3f2a…_diplome.pdf »).
 */
@Service
public class FileStorageService {

    public static final String PHOTOS = "photos";
    public static final String DOCUMENTS = "documents";
    public static final String JUSTIFICATIFS = "justificatifs";
    private static final Set<String> DOSSIERS = Set.of(DOCUMENTS, JUSTIFICATIFS);
    private static final Set<String> EXT_DOCUMENTS = Set.of(
        "pdf",
        "doc",
        "docx",
        "xls",
        "xlsx",
        "png",
        "jpg",
        "jpeg"
    );
    private static final Set<String> EXT_IMAGES = Set.of("png", "jpg", "jpeg");
    private static final long TAILLE_MAX_PHOTO = 3L * 1024 * 1024;

    /** Résultat d'un enregistrement : chemin relatif (à stocker en base), nom d'origine et taille. */
    public record Stocke(String chemin, String nom, long taille) {}

    private final Path racine;

    public FileStorageService(@Value("${app.upload-dir}") String dir) throws IOException {
        this.racine = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(racine);
    }

    /** Enregistre un document dans documents/ ou justificatifs/ (tout autre dossier demandé => documents/). */
    public Stocke stocker(MultipartFile file, String dossier) {
        return enregistrer(
            file,
            DOSSIERS.contains(dossier) ? dossier : DOCUMENTS,
            EXT_DOCUMENTS,
            "Format non autorisé. Formats acceptés : pdf, doc, docx, xls, xlsx, png, jpg.",
            true
        );
    }

    /** Enregistre une photo d'agent dans photos/ et retourne son chemin relatif. */
    public String stockerImage(MultipartFile file) {
        if (file != null && file.getSize() > TAILLE_MAX_PHOTO) {
            throw new BusinessException("La photo ne doit pas dépasser 3 Mo.");
        }
        return enregistrer(
            file,
            PHOTOS,
            EXT_IMAGES,
            "Format de photo non autorisé. Formats acceptés : jpg, jpeg, png.",
            false
        ).chemin();
    }

    private Stocke enregistrer(
        MultipartFile file,
        String dossier,
        Set<String> autorises,
        String messageFormat,
        boolean garderNom
    ) {
        if (file == null || file.isEmpty()) throw new BusinessException("Aucun fichier reçu.");
        String original =
            file.getOriginalFilename() == null ? "fichier" : file.getOriginalFilename();
        String propre = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        int dot = propre.lastIndexOf('.');
        String ext = dot >= 0 ? propre.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
        if (!autorises.contains(ext)) throw new BusinessException(messageFormat);
        // nom aléatoire complet (UUID) : le chemin ne peut pas être deviné
        String nom = UUID.randomUUID() + (garderNom ? "_" + propre : "." + ext);
        try (InputStream in = file.getInputStream()) {
            Path dir = racine.resolve(dossier);
            Files.createDirectories(dir);
            Files.copy(in, dir.resolve(nom), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("Impossible d'enregistrer le fichier.");
        }
        return new Stocke(dossier + "/" + nom, propre, file.getSize());
    }

    private Path resoudre(String chemin) {
        Path p = racine.resolve(chemin).normalize();
        if (!p.startsWith(racine)) throw new ResourceNotFoundException("Fichier introuvable"); // protection « ../ »
        return p;
    }

    public boolean existe(String chemin) {
        try {
            return chemin != null && !chemin.isBlank() && Files.isRegularFile(resoudre(chemin));
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    public Resource charger(String chemin) {
        try {
            Path p = resoudre(chemin);
            if (!Files.isRegularFile(p)) throw new ResourceNotFoundException("Fichier introuvable");
            return new UrlResource(p.toUri());
        } catch (IOException e) {
            throw new ResourceNotFoundException("Fichier introuvable");
        }
    }

    /** Nom lisible d'un fichier : dernier segment du chemin sans le préfixe UUID. */
    public String nomAffichage(String chemin) {
        String nom = chemin.substring(chemin.lastIndexOf('/') + 1);
        return nom.replaceFirst("^[0-9a-fA-F-]{36}_", "");
    }

    /** @return le contenu du fichier, ou null s'il n'existe plus. */
    public byte[] lireOctets(String chemin) {
        try {
            Path p = resoudre(chemin);
            return Files.isRegularFile(p) ? Files.readAllBytes(p) : null;
        } catch (IOException | ResourceNotFoundException e) {
            return null;
        }
    }

    public void supprimer(String chemin) {
        if (chemin == null || chemin.isBlank()) return;
        try {
            Files.deleteIfExists(resoudre(chemin));
        } catch (IOException | ResourceNotFoundException ignored) {
            // un fichier orphelin n'empêche pas le fonctionnement
        }
    }
}
