package ml.apes.rh.personnel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.imageio.ImageIO;
import ml.apes.rh.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Persistent transparent signature image used on service cards. */
@Service
public class SignatureDirecteurService {

    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private final Path file;
    private final Path nomFile;

    public SignatureDirecteurService(@Value("${app.upload-dir}") String uploadDir) {
        this.file = Paths.get(uploadDir)
            .toAbsolutePath()
            .normalize()
            .resolve("parametres")
            .resolve("signature-directeur.png");
        this.nomFile = this.file.resolveSibling("nom-directeur.txt");
    }

    public synchronized void enregistrer(MultipartFile upload) {
        if (upload == null || upload.isEmpty()) throw new BusinessException(
            "Selectionnez une image de signature."
        );
        if (upload.getSize() > MAX_BYTES) throw new BusinessException(
            "La signature ne doit pas depasser 2 Mo."
        );
        try (var input = upload.getInputStream()) {
            BufferedImage image = ImageIO.read(input);
            if (
                image == null || image.getWidth() > 3000 || image.getHeight() > 1500
            ) throw new BusinessException("Le fichier doit etre une image PNG ou JPEG valide.");
            Files.createDirectories(file.getParent());
            Path temp = Files.createTempFile(file.getParent(), "signature-", ".png");
            try {
                if (!ImageIO.write(image, "png", temp.toFile())) throw new BusinessException(
                    "Impossible de convertir la signature en PNG."
                );
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException ex) {
            throw new BusinessException("Impossible d'enregistrer la signature du Directeur.");
        }
    }

    public synchronized byte[] lire() {
        try {
            return Files.isRegularFile(file) ? Files.readAllBytes(file) : null;
        } catch (IOException ex) {
            throw new BusinessException("Impossible de lire la signature du Directeur.");
        }
    }

    public synchronized String lireNom() {
        try {
            return Files.isRegularFile(nomFile)
                ? Files.readString(nomFile, StandardCharsets.UTF_8)
                : "Le Directeur Général";
        } catch (IOException ex) {
            throw new BusinessException("Impossible de lire le nom du Directeur.");
        }
    }

    public synchronized void enregistrerNom(String nom) {
        if (nom == null || nom.isBlank()) throw new BusinessException(
            "Le nom du Directeur est obligatoire."
        );
        String nomNettoye = nom.trim();
        if (nomNettoye.length() > 120) throw new BusinessException(
            "Le nom du Directeur ne peut pas dépasser 120 caractères."
        );
        try {
            Files.createDirectories(nomFile.getParent());
            Files.writeString(nomFile, nomNettoye, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BusinessException("Impossible d'enregistrer le nom du Directeur.");
        }
    }
}
