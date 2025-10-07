package it.skinjobs.api;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import it.skinjobs.ConfiguratorApplicationAPI;
import it.skinjobs.config.H2TestProfileConfig;
import it.skinjobs.dto.CredentialDTO;
import it.skinjobs.model.Credential;
import it.skinjobs.model.Session;
import it.skinjobs.repository.Credentials;
import it.skinjobs.repository.Sessions;
import it.skinjobs.utils.CredentialsProperties;

@SpringBootTest(classes = {ConfiguratorApplicationAPI.class, H2TestProfileConfig.class})
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

    private CredentialAPI credentialAPI;

    private CredentialsProperties properties;

    @Before
    public void setUp() {
        properties = new CredentialsProperties();
        properties.setName("admin");
        properties.setPassword("admin");

        // Return the same object on save
        when(credentials.save(any(Credential.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        // Initialize CredentialAPI with mocked repos
        credentialAPI = new CredentialAPI(credentials, sessions, properties);
    }

    @Test
    public void testGetAdminCredential() {
        Credential admin = credentialAPI.getAdminCredential();
        assertNotNull(admin);
        assertEquals("admin", admin.getName());
        assertEquals("admin", admin.getPassword());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        Credential c = new Credential();
        c.setName("admin");
        c.setPassword("admin");

        List<Credential> list = new ArrayList<>();
        list.add(c);

        when(credentials.findByName("admin")).thenReturn(list);
        when(sessions.save(any(Session.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        CredentialDTO dto = new CredentialDTO();
        dto.setName("admin");
        dto.setPassword("admin");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credential.name").value("admin"));
    }

    @Test
    public void testLoginWrongPassword() throws Exception {
        Credential c = new Credential();
        c.setName("admin");
        c.setPassword("admin");

        List<Credential> list = new ArrayList<>();
        list.add(c);

        when(credentials.findByName("admin")).thenReturn(list);

        CredentialDTO dto = new CredentialDTO();
        dto.setName("admin");
        dto.setPassword("wrong");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLoginUserNotFound() throws Exception {
        when(credentials.findByName("missing")).thenReturn(new ArrayList<>());

        CredentialDTO dto = new CredentialDTO();
        dto.setName("missing");
        dto.setPassword("any");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSessionIsValid() {
        Session s = new Session();
        s.setToken("123");
        s.setExpireDate(new Date(System.currentTimeMillis() + 60000)); // 1 min ahead

        when(sessions.findByToken("123")).thenReturn(List.of(s));
        when(sessions.save(any(Session.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        assertTrue(credentialAPI.sessionIsValid("123"));
    }

    @Test
    public void testSessionIsNotValid() {
        when(sessions.findByToken("expired")).thenReturn(new ArrayList<>());
        assertFalse(credentialAPI.sessionIsValid("expired"));
    }

    @Test
    public void testLogoutValidSession() {
        Session s = new Session();
        s.setToken("abc");
        s.setExpireDate(new Date(System.currentTimeMillis() + 60000));

        when(sessions.findByToken("abc")).thenReturn(List.of(s));
        when(sessions.save(any(Session.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        ResponseEntity<Boolean> response = credentialAPI.logout(Map.of("token", "abc"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

    @Test
    public void testLogoutInvalidSession() {
        when(sessions.findByToken("invalid")).thenReturn(new ArrayList<>());
        ResponseEntity<Boolean> response = credentialAPI.logout(Map.of("token", "invalid"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }
}
