package it.skinjobs.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.ResponseEntity;

import it.skinjobs.ConfiguratorApplicationAPI;
import it.skinjobs.config.H2TestProfileConfig;
import it.skinjobs.utils.Callable;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ConfiguratorApplicationAPI.class, H2TestProfileConfig.class })
@ActiveProfiles("test")
public class DeleteAllApiTest {

    @MockBean
    private CredentialAPI credentialAPI;

    private TestDeleteAllAPI api;

    // Dummy subclass to test the abstract DeleteAllAPI class
    private static class TestDeleteAllAPI extends DeleteAllAPI<Object, Object, Object> {

        protected TestDeleteAllAPI(CredentialAPI credentialAPI) {
            super(credentialAPI);
        }

        @Override
        protected Boolean deleteEntity(Integer index) {
            return index != null && index == 1; // only "1" exists
        }

        // Implement API interface abstract methods as no-op / dummy
        @Override
        public ResponseEntity<Object> getById(Object id) {
            return null;
        }

        @Override
        public Iterable<Object> getAll() {
            return null;
        }

        @Override
        public ResponseEntity<Object> newElement(Map<String, String> headers, Object dto) {
            return null;
        }

        @Override
        public ResponseEntity<Boolean> deleteElement(Map<String, String> headers, Object id) {
            return null; // or ResponseEntity.ok(true/false) if needed
        }

        @Override
        public ResponseEntity<Object> updateElement(Map<String, String> headers, Object dto, Object id) {
            return null;
        }
    }

    @Before
    public void setUp() {
        api = new TestDeleteAllAPI(credentialAPI);
    }

    @Test
    public void testSessionDeleteAllOperationValidSession() {
        // Arrange
        when(credentialAPI.sessionIsValid("valid-token")).thenReturn(true);

        @SuppressWarnings("unchecked")
        Callable<ResponseEntity<Boolean>, Object> callable = (Callable<ResponseEntity<Boolean>, Object>) mock(
                Callable.class);
        when(callable.call(null)).thenReturn(ResponseEntity.ok(true));

        // Act
        ResponseEntity<Boolean> response = api.sessionDeleteAllOperation(Map.of("token", "valid-token"), callable);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
        verify(callable, times(1)).call(null);
    }

    @Test
    public void testSessionDeleteAllOperationInvalidSession() {
        // Arrange
        when(credentialAPI.sessionIsValid("invalid-token")).thenReturn(false);

        @SuppressWarnings("unchecked")
        Callable<ResponseEntity<Boolean>, Object> callable = (Callable<ResponseEntity<Boolean>, Object>) mock(
                Callable.class);

        // Act
        ResponseEntity<Boolean> response = api.sessionDeleteAllOperation(Map.of("token", "invalid-token"), callable);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(callable, never()).call(null);
    }
}
