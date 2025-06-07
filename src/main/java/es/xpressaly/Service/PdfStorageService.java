package es.xpressaly.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class PdfStorageService {

    // Define the upload directory for PDFs

    private final Path fileStorageLocation;

    public PdfStorageService() {
        this.fileStorageLocation = Paths.get("src/main/resources/static/uploads/pdfs")
                                        .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        // Normalize file name
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // Check if the file's name contains invalid characters
        if (fileName.contains("..")) {
            throw new IOException("Sorry! Filename contains invalid path sequence " + fileName);
        }

        // Validate file type by magic number (more secure than content type)
        byte[] header = new byte[5]; // %PDF-
        file.getInputStream().read(header);
        if (!new String(header).startsWith("%PDF-")) {
            throw new IOException("Only PDF files are allowed based on file content.");
        }

        // Generate a unique filename to prevent overwriting and guessing
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

        // Copy file to the target location (Replacing existing file with the same name)
        Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/pdfs/" + uniqueFileName; // Return the relative path for web access
    }

    public Path loadFileAsResource(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }
} 