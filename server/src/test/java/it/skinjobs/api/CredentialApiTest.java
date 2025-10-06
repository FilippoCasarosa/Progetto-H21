package it.skinjobs.api;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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

    @Autowired
    private CredentialsProperties properties;

    @MockBean
    private Credentials credentials;

    @MockBean
    private Sessions sessions;

    @Autowired
    private CredentialAPI credentialAPI;

    private List<Credential> credentialList;
    private CredentialDTO credentialDTO;
    private List<Session> sessionList;

    @Before
    public void init() {
        // No setup required here; Spring handles @Autowired beans
    }

    @Test
    public void testConstructor() {
        CredentialsProperties localProperties = new CredentialsProperties();
        localProperties.setName("admin");
        localProperties.setPassword("admin");

        doAnswer(returnsFirstArg()).when(credentials).save(any(Credential.class));
        when(credentials.findByName(anyString())).thenReturn(new ArrayList<>());

        // Include sessions when constructing locally
        CredentialAPI localCredentialAPI = new CredentialAPI(credentials, sessions, localProperties);

        assertEquals("admin", localCredentialAPI.getAdminCredential().getName());
        assertEquals("admin", localCredentialAPI.getAdminCredential().getPassword());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        credentialDTO = new CredentialDTO();
        credentialDTO.setName("admin");
        credentialDTO.setPassword("admin");

        Credential c = new Credential();
        c.setName("admin");
        c.setPassword("admin");

        credentialList = new ArrayList<>();
        credentialList.add(c);

        when(credentials.findByName(anyString())).thenReturn(credentialList);
        doAnswer(returnsFirstArg()).when(sessions).save(any(Session.class));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(credentialDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.credential.name", is("admin")));
    }

    @Test
    public void testLoginNotFound() throws Exception {
        credentialDTO = new CredentialDTO();
        credentialDTO.setName("Pippo");
        credentialDTO.setPassword("admin");

        when(credentials.findByName(anyString())).thenReturn(new ArrayList<>());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(credentialDTO));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testLoginFail() throws Exception {
        credentialDTO = new CredentialDTO();
        credentialDTO.setName("admin");
        credentialDTO.setPassword("Paperino");

        Credential c = new Credential();
        c.setName("admin");
        c.setPassword("admin");

        credentialList = new ArrayList<>();
        credentialList.add(c);

        when(credentials.findByName(anyString())).thenReturn(credentialList);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(credentialDTO));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSessionIsValid() {
        Session session = new Session();
        session.setToken("123");
        Instant future = Instant.now().plus(1, ChronoUnit.MINUTES);
        Date date = Date.from(future);
        session.setExpireDate(date);

        sessionList = new ArrayList<>();
        sessionList.add(session);

        when(sessions.findByToken(anyString())).thenReturn(sessionList);

        assertTrue(credentialAPI.sessionIsValid("123"));
    }

    @Test
    public void testSessionIsNotValid() {
        when(sessions.findByToken(anyString())).thenReturn(new ArrayList<>());

        assertFalse(credentialAPI.sessionIsValid("421"));
    }
}
