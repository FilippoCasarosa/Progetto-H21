package it.skinjobs.api;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.skinjobs.dto.UploadFileResponse;
import it.skinjobs.utils.FileStorageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.mock.web.MockMultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class FileApiTest {

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private CredentialAPI credentialAPI;

    private FileAPI api;

    @Before
    public void setUp() {
        api = new FileAPI(fileStorageService, credentialAPI);
        when(credentialAPI.sessionIsValid(anyString())).thenReturn(true);
    }

    @Test
    public void testUploadFile_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello World".getBytes());
        when(fileStorageService.storeFile(file)).thenReturn("test.txt");

        ResponseEntity<UploadFileResponse> response = api.uploadFile(Map.of("token", "123"), file);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("test.txt", response.getBody().getFileName());
        assertEquals("text/plain", response.getBody().getFileType());
        assertTrue(response.getBody().getFileDownloadUri().contains("test.txt"));
    }

    @Test
    public void testUploadFile_Unauthorized() {
        when(credentialAPI.sessionIsValid("123")).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "Hello World".getBytes());

        ResponseEntity<UploadFileResponse> response = api.uploadFile(Map.of("token", "123"), file);

        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    public void testDownloadFile_Success() throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("test.txt");
        File fakeFile = new File("src/test/resources/test.txt");
        when(resource.getFile()).thenReturn(fakeFile);
        when(fileStorageService.loadFileAsResource("test.txt")).thenReturn(resource);

        HttpServletRequest request = mock(HttpServletRequest.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(request.getServletContext()).thenReturn(servletContext);
        when(servletContext.getMimeType(any(String.class))).thenReturn("text/plain");

        ResponseEntity<?> response = api.downloadFile("test.txt", request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("attachment; filename=\"test.txt\"",
                response.getHeaders().getFirst("Content-Disposition"));
    }

    @Test
    public void testDownloadFile_DefaultMimeType() throws IOException {
        Resource resource = mock(Resource.class);
        when(resource.getFilename()).thenReturn("test.txt");
        when(resource.getFile()).thenThrow(new IOException("Cannot read file"));
        when(fileStorageService.loadFileAsResource("test.txt")).thenReturn(resource);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletContext()).thenReturn(mock(ServletContext.class));

        ResponseEntity<?> response = api.downloadFile("test.txt", request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("attachment; filename=\"test.txt\"",
                response.getHeaders().getFirst("Content-Disposition"));
    }
}
