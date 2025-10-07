package it.skinjobs.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.ResponseEntity;

import it.skinjobs.ConfiguratorApplicationAPI;
import it.skinjobs.config.H2TestProfileConfig;
import it.skinjobs.utils.Callable;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ConfiguratorApplicationAPI.class, H2TestProfileConfig.class})
@ActiveProfiles("test")
public class BaseApiTest {

    @MockBean
    private CredentialAPI credentialAPI;

    private TestBaseAPI api;

    // Dummy subclass to test abstract BaseAPI
    private static class TestBaseAPI extends BaseAPI<Object, Object, Object> {

        protected TestBaseAPI(CredentialAPI credentialAPI) {
            super(credentialAPI);
        }

        @Override
        protected Boolean deleteEntity(Integer index) {
            return index != null && index == 1; // only entity "1" exists
        }

        // Dummy implementations for API interface methods
        @Override
        public ResponseEntity<Object> getById(Object id) {
            return ResponseEntity.ok(null);
        }

        @Override
        public Iterable<Object> getAll() {
            return null;
        }

        @Override
        public ResponseEntity<Object> newElement(Map<String, String> headers, Object dto) {
            return ResponseEntity.ok(null);
        }

        @Override
        public ResponseEntity<Object> updateElement(Map<String, String> headers, Object dto, Object id) {
            return ResponseEntity.ok(null);
        }

        @Override
        public ResponseEntity<Boolean> deleteElement(Map<String, String> headers, Object id) {
            return ResponseEntity.ok(true);
        }
    }

    @Before
    public void setUp() {
        api = new TestBaseAPI(credentialAPI);
    }

    @Test
    public void testSessionOperation_validSession() {
        when(credentialAPI.sessionIsValid("valid-token")).thenReturn(true);

        @SuppressWarnings("unchecked")
        Callable<ResponseEntity<Object>, Object> callable = mock(Callable.class);
        when(callable.call(null)).thenReturn(ResponseEntity.ok("ok"));

        ResponseEntity<Object> response = api.sessionOperation(Map.of("token", "valid-token"), null, callable);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("ok", response.getBody());
        verify(callable, times(1)).call(null);
    }

    @Test
    public void testSessionOperation_invalidSession() {
        when(credentialAPI.sessionIsValid("invalid-token")).thenReturn(false);

        @SuppressWarnings("unchecked")
        Callable<ResponseEntity<Object>, Object> callable = mock(Callable.class);

        ResponseEntity<Object> response = api.sessionOperation(Map.of("token", "invalid-token"), null, callable);

        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(callable, never()).call(null);
    }

    @Test
    public void testSessionDeleteOperation_validSession_existingEntity() {
        when(credentialAPI.sessionIsValid("token")).thenReturn(true);

        ResponseEntity<Boolean> response = api.sessionDeleteOperation(Map.of("token", "token"), 1);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
    }

    @Test
    public void testSessionDeleteOperation_validSession_nonExistingEntity() {
        when(credentialAPI.sessionIsValid("token")).thenReturn(true);

        ResponseEntity<Boolean> response = api.sessionDeleteOperation(Map.of("token", "token"), 2);

        assertEquals(404, response.getStatusCodeValue());
        assertFalse(response.getBody());
    }

    @Test
    public void testSessionDeleteOperation_invalidSession() {
        when(credentialAPI.sessionIsValid("invalid-token")).thenReturn(false);

        ResponseEntity<Boolean> response = api.sessionDeleteOperation(Map.of("token", "invalid-token"), 1);

        assertEquals(401, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}
