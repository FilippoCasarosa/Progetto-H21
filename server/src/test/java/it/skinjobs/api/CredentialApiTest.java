package it.skinjobs.api;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import it.skinjobs.ConfiguratorApplicationAPI;
import it.skinjobs.config.H2TestProfileConfig;
import it.skinjobs.dto.CredentialDTO;
import it.skinjobs.model.Credential;
import it.skinjobs.model.Session;
import it.skinjobs.repository.Credentials;
import it.skinjobs.repository.Sessions;
import it.skinjobs.utils.CredentialsProperties;

@SpringBootTest(classes = { ConfiguratorApplicationAPI.class, H2TestProfileConfig.class })
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class CredentialApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private Credentials credentials;

    @MockBean
    private Sessions sessions;

    @Autowired
    private CredentialAPI credentialAPI;

    private Credential admin;
    private List<Session> activeSessionList;

    @Before
    public void setup() {
        // Admin credential
        admin = new Credential();
        admin.setName("admin");
        admin.setPassword("admin");

        // Mock finding admin
        Mockito.when(credentials.findByName("admin")).thenReturn(List.of(admin));

        // Mock session save to return same object
        Mockito.when(sessions.save(Mockito.any(Session.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Active session
        Session session = new Session();
        session.setToken("123");
        session.setExpireDate(Date.from(Instant.now().plus(5, ChronoUnit.MINUTES)));
        activeSessionList = List.of(session);

        Mockito.when(sessions.findByToken("123")).thenReturn(activeSessionList);
        Mockito.when(sessions.findByToken("expired")).thenReturn(new ArrayList<>());
    }

@Test
public void testGetAdminCredential() {
    // Create mocks
    Credentials mockCredentials = Mockito.mock(Credentials.class);
    Sessions mockSessions = Mockito.mock(Sessions.class);
    CredentialsProperties props = new CredentialsProperties();
    props.setName("admin");
    props.setPassword("admin");

    // Mock findByName to return empty, so admin is created
    Mockito.when(mockCredentials.findByName("admin")).thenReturn(new ArrayList<>());
    Mockito.when(mockCredentials.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

    // Instantiate local CredentialAPI with dummy objects
    CredentialAPI dummyAPI = new CredentialAPI(mockCredentials, mockSessions, props);

    Credential dummyAdmin = dummyAPI.getAdminCredential();
    assertNotNull(dummyAdmin);                   // ensure it’s created
    assertEquals("admin", dummyAdmin.getName()); // validate name
    assertEquals("admin", dummyAdmin.getPassword());
}



    @Test
    public void testLoginSuccess() throws Exception {
        CredentialDTO dto = new CredentialDTO();
        dto.setName("admin");
        dto.setPassword("admin");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential.name", is("admin")));
    }

    @Test
    public void testLoginWrongPassword() throws Exception {
        CredentialDTO dto = new CredentialDTO();
        dto.setName("admin");
        dto.setPassword("wrong");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLoginUserNotFound() throws Exception {
        CredentialDTO dto = new CredentialDTO();
        dto.setName("unknown");
        dto.setPassword("any");

        Mockito.when(credentials.findByName("unknown")).thenReturn(new ArrayList<>());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSessionIsValid() {
        // Verify that the session with token "123" is valid
        boolean valid = credentialAPI.sessionIsValid("123");
        assertTrue("Session with token '123' should be valid", valid);
    }

    @Test
    public void testSessionIsNotValid() {
        // Verify that the session with token "expired" is not valid
        boolean valid = credentialAPI.sessionIsValid("expired");
        assertFalse("Session with token 'expired' should NOT be valid", valid);
    }

    @Test
    public void testSessionSaveReturnsSameObject() {
        Session s = new Session();
        s.setToken("999");

        Session saved = sessions.save(s);

        // Verify that the saved session is the same object as the original
        assertSame("Saved session should be the same instance as the original", s, saved);
    }

}