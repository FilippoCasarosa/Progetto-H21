package it.skinjobs.dto;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for UploadFileResponse DTO
 * Follows the style of ReadySetupDTOtest
 */
@SpringBootTest
public class UploadFileResponseTest {

    @Test
    public void checkProperties() {
        UploadFileResponse response = new UploadFileResponse(
                "test.txt",
                "/download/test.txt",
                "text/plain",
                123L
        );

        // Test getters
        assertEquals("test.txt", response.getFileName());
        assertEquals("/download/test.txt", response.getFileDownloadUri());
        assertEquals("text/plain", response.getFileType());
        assertEquals(123L, response.getSize());

        // Test setters
        response.setFileName("newFile.txt");
        response.setFileDownloadUri("/download/newFile.txt");
        response.setFileType("application/pdf");
        response.setSize(456L);

        assertEquals("newFile.txt", response.getFileName());
        assertEquals("/download/newFile.txt", response.getFileDownloadUri());
        assertEquals("application/pdf", response.getFileType());
        assertEquals(456L, response.getSize());
    }
}
