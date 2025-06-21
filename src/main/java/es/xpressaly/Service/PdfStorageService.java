package es.xpressaly.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PdfStorageService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String UPLOAD_DIR = "pdfs";
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("[a-zA-Z0-9-_\\. ]+");
    private final Path fileStorageLocation;

    public PdfStorageService() {
        this.fileStorageLocation = Paths.get(UPLOAD_DIR)
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            // Set directory permissions to be readable/writable only by the application
            try {
                // Try POSIX permissions first (Linux/Unix)
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwx------");
                Files.setPosixFilePermissions(fileStorageLocation, permissions);
            } catch (UnsupportedOperationException e) {
                // Fallback for Windows
                fileStorageLocation.toFile().setReadable(true, true);
                fileStorageLocation.toFile().setWritable(true, true);
                fileStorageLocation.toFile().setExecutable(true, true);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, Long userId) throws IOException {
        if (userId == null) {
            throw new IOException("User ID is required for file storage");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 10MB");
        }

        // Normalize and validate file name
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        validateFileName(fileName);

        // Validate file type by magic number and content type
        validateFileType(file);

        // Create user-specific directory with secure permissions
        Path userDir = createAndGetUserDirectory(userId);

        // Generate a unique filename to prevent overwriting and guessing
        String uniqueFileName = UUID.randomUUID().toString() + "_" + sanitizeFileName(fileName);

        // Resolve the destination path and validate it's within our upload directory
        Path targetLocation = userDir.resolve(uniqueFileName).normalize();
        validatePath(targetLocation, userDir);

        // Copy file to the target location (Replacing existing file with the same name)
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Set secure file permissions
        try {
            // Try POSIX permissions first (Linux/Unix)
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rw-------");
            Files.setPosixFilePermissions(targetLocation, permissions);
        } catch (UnsupportedOperationException e) {
            // Fallback for Windows
            targetLocation.toFile().setReadable(true, true);
            targetLocation.toFile().setWritable(true, true);
            targetLocation.toFile().setExecutable(false, true);
        }

        return "/pdfs/user_" + userId + "/" + uniqueFileName;
    }

    private Path createAndGetUserDirectory(Long userId) throws IOException {
        Path userDir = fileStorageLocation.resolve("user_" + userId);
        Files.createDirectories(userDir);
        
        try {
            // Try POSIX permissions first (Linux/Unix)
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwx------");
            Files.setPosixFilePermissions(userDir, permissions);
        } catch (UnsupportedOperationException e) {
            // Fallback for Windows
            userDir.toFile().setReadable(true, true);
            userDir.toFile().setWritable(true, true);
            userDir.toFile().setExecutable(true, true);
        }
        
        return userDir;
    }

    private String sanitizeFileName(String fileName) {
        // Get just the filename without path
        fileName = Paths.get(fileName).getFileName().toString();
        
        // Replace spaces with underscores and remove any other special characters
        return fileName.trim().replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9\\-\\._]", "");
    }

    private void validatePath(Path path, Path baseDir) throws IOException {
        if (!path.normalize().startsWith(baseDir.normalize())) {
            throw new IOException("Invalid file location");
        }
        
        // Check for Windows reserved names
        String normalizedPath = path.normalize().toString().toLowerCase();
        String[] reservedNames = {"con", "prn", "aux", "nul", "com1", "com2", "com3", "com4", 
                                "com5", "com6", "com7", "com8", "com9", "lpt1", "lpt2", "lpt3", 
                                "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"};
        
        for (String reserved : reservedNames) {
            if (normalizedPath.contains("\\" + reserved + "\\") || 
                normalizedPath.contains("\\" + reserved + ".")) {
                throw new IOException("Invalid file name: reserved Windows name detected");
            }
        }
    }

    private void validateFileName(String fileName) throws IOException {
        // Basic security checks
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IOException("Filename cannot be empty");
        }

        // Check for basic path traversal attempts
        if (fileName.contains("..")) {
            throw new IOException("Invalid file name");
        }

        // Check file extension
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            throw new IOException("Only PDF files are allowed");
        }

        // Allow more characters but still maintain basic security
        if (!SAFE_FILENAME_PATTERN.matcher(fileName).matches()) {
            fileName = sanitizeFileName(fileName);
            if (!SAFE_FILENAME_PATTERN.matcher(fileName).matches()) {
                throw new IOException("Filename contains invalid characters");
            }
        }
    }

    private void validateFileType(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds maximum limit of 10MB");
        }

        // Check content type - be more permissive with MIME types
        String contentType = file.getContentType();
        if (contentType == null || 
            !(contentType.equals("application/pdf") || 
              contentType.equals("binary/octet-stream") || // Some systems use this for PDFs
              contentType.equals("application/x-pdf") ||
              contentType.equals("application/acrobat") ||
              contentType.equals("applications/vnd.pdf") ||
              contentType.equals("text/pdf") ||
              contentType.equals("text/x-pdf"))) {
            throw new IOException("File must be a PDF document");
        }

        // Validate PDF header
        byte[] header = new byte[5];
        if (file.getInputStream().read(header) >= 4) {  // Changed from exact 5 to >= 4
            String magic = new String(header);
            if (magic.startsWith("%PDF-")) {
                return; // Valid PDF header found
            }
        }
        throw new IOException("Invalid PDF file format");
    }

    public void deleteFile(String filePath, Long userId) throws IOException {
        if (filePath == null || filePath.isEmpty() || userId == null) {
            throw new IOException("Invalid file path or user ID");
        }

        // Validate the file belongs to the user
        if (!filePath.contains("/user_" + userId + "/")) {
            throw new IOException("Unauthorized access to file");
        }
        
        // Extract only the filename from the full path
        String fileName = Paths.get(filePath).getFileName().toString();
        
        // Validate filename
        validateFileName(fileName);
        
        // Get user directory
        Path userDir = fileStorageLocation.resolve("user_" + userId);
        
        // Resolve and validate the path is within user's directory
        Path targetLocation = userDir.resolve(fileName).normalize();
        validatePath(targetLocation, userDir);
        
        // Delete the file if it exists
        Files.deleteIfExists(targetLocation);
    }

    public Path loadFileAsResource(String fileName, Long userId) throws IOException {
        if (userId == null) {
            throw new IOException("User ID is required to access file");
        }

        // Validate filename
        validateFileName(fileName);
        
        // Get user directory
        Path userDir = fileStorageLocation.resolve("user_" + userId);
        
        // Resolve the path and ensure it's within user's directory
        Path filePath = userDir.resolve(fileName).normalize();
        validatePath(filePath, userDir);
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + fileName);
        }
        
        return filePath;
    }
} 