package it.skinjobs.api;

import it.skinjobs.dto.UploadFileResponse;
import it.skinjobs.utils.Environment;
import it.skinjobs.utils.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * REST API for file upload and download operations.
 * 
 * Provides endpoints for uploading files to the server and downloading them later.
 * Uses session validation via CredentialAPI to restrict access.
 * 
 * @author 
 */
@RestController
public class FileAPI {

   private static final Logger logger = LoggerFactory.getLogger(FileAPI.class);

   private final FileStorageService fileStorageService;
   private final CredentialAPI credentialAPI;

   /**
    * Constructor-based dependency injection (SonarQube compliant)
    *
    * @param fileStorageService service for file storage operations
    * @param credentialAPI      authentication and session validation API
    */
   public FileAPI(FileStorageService fileStorageService, CredentialAPI credentialAPI) {
      this.fileStorageService = fileStorageService;
      this.credentialAPI = credentialAPI;
   }

   /**
    * Uploads a file and returns its metadata and download URL.
    *
    * @param headers request headers containing authentication token
    * @param file    multipart file to upload
    * @return response with file details or unauthorized status
    */
   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @PostMapping("/uploadFile")
   public ResponseEntity<UploadFileResponse> uploadFile(@RequestHeader Map<String, String> headers,
                                                        @RequestParam("file") MultipartFile file) {
      String token = headers.get("token");
      if (!credentialAPI.sessionIsValid(token)) {
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      String fileName = fileStorageService.storeFile(file);
      String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/downloadFile/")
            .path(fileName)
            .toUriString();

      UploadFileResponse response = new UploadFileResponse(
            fileName, fileDownloadUri, file.getContentType(), file.getSize());

      return ResponseEntity.ok(response);
   }

   /**
    * Downloads a file by its name.
    *
    * @param fileName the name of the file to download
    * @param request  HTTP servlet request (used to detect MIME type)
    * @return response containing the file resource
    */
   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @GetMapping("/downloadFile/{fileName:.+}")
   public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
      Resource resource = fileStorageService.loadFileAsResource(fileName);

      String contentType;
      try {
         contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
      } catch (IOException ex) {
         logger.warn("Could not determine file type for '{}'. Using default.", fileName);
         contentType = null;
      }

      if (contentType == null) {
         contentType = "application/octet-stream";
      }

      return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                  String.format("attachment; filename=\"%s\"", resource.getFilename()))
            .body(resource);
   }
}
