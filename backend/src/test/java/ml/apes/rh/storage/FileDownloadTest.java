package ml.apes.rh.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import ml.apes.rh.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class FileDownloadTest {

    @TempDir
    Path directory;

    @Test
    void everyAcceptedDocumentTypeAndPhotoCanBeDownloadedWithoutChangingItsBytes()
        throws Exception {
        FileStorageService storage = new FileStorageService(directory.toString());
        CurrentUserService current = new CurrentUserService(null) {
            @Override
            public String role() {
                return "ADMINISTRATEUR";
            }
        };
        FichierController controller = new FichierController(storage, null, current);
        for (String extension : new String[] {
            "pdf",
            "doc",
            "docx",
            "xls",
            "xlsx",
            "png",
            "jpg",
            "jpeg",
        }) {
            byte[] bytes = ("original-file-" + extension).getBytes(StandardCharsets.UTF_8);
            var uploaded = storage.stocker(
                new MockMultipartFile(
                    "file",
                    "document." + extension,
                    "application/octet-stream",
                    bytes
                ),
                "documents"
            );
            var download = controller.telecharger(uploaded.chemin());
            assertEquals("attachment", download.getHeaders().getContentDisposition().getType());
            assertEquals(
                "document." + extension,
                download.getHeaders().getContentDisposition().getFilename()
            );
            try (var stream = download.getBody().getInputStream()) {
                assertArrayEquals(bytes, stream.readAllBytes(), extension);
            }
        }
        byte[] photo = "photo-bytes".getBytes(StandardCharsets.UTF_8);
        String photoPath = storage.stockerImage(
            new MockMultipartFile("file", "portrait.png", "image/png", photo)
        );
        var download = controller.telecharger(photoPath);
        assertEquals("attachment", download.getHeaders().getContentDisposition().getType());
        try (var stream = download.getBody().getInputStream()) {
            assertArrayEquals(photo, stream.readAllBytes());
        }
    }
}
