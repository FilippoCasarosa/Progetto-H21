
    package it.skinjobs.api;
    
    import static org.junit.Assert.*;
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
    
    import java.util.*;
    
    import org.junit.Before;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.mockito.AdditionalAnswers;
    import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.boot.test.mock.mockito.MockBean;
    import org.springframework.http.ResponseEntity;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.context.junit4.SpringRunner;
    
    import it.skinjobs.ConfiguratorApplicationAPI;
    import it.skinjobs.config.H2TestProfileConfig;
    import it.skinjobs.dto.ReadySetupDTO;
    import it.skinjobs.model.Component;
    import it.skinjobs.model.ReadySetup;
import it.skinjobs.repository.Components;
import it.skinjobs.repository.ReadySetups;
    
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = {ConfiguratorApplicationAPI.class, H2TestProfileConfig.class})
    @ActiveProfiles("test")
    @AutoConfigureMockMvc
    public class ReadySetupApiTest {
    
        @MockBean
        private ReadySetups readySetups;
    
        @MockBean
        private Components components;
    
        @MockBean
        private CredentialAPI credentialAPI;
    
        private ReadySetupAPI api;
    
        @Before
        public void setUp() {
            api = new ReadySetupAPI(credentialAPI, readySetups, components);
            when(credentialAPI.sessionIsValid(anyString())).thenReturn(true);
        }
    
        @Test
        public void testGetAll() {
            ReadySetup rs = new ReadySetup();
            List<ReadySetup> list = List.of(rs);
            when(readySetups.findAll()).thenReturn(list);
    
            Iterable<ReadySetup> result = api.getAll();
            assertNotNull(result);
            assertTrue(result.iterator().hasNext());
        }
    
        @Test
        public void testGetByIdFound() {
            ReadySetup rs = new ReadySetup();
            when(readySetups.findById(1)).thenReturn(Optional.of(rs));
    
            ResponseEntity<ReadySetup> response = api.getById(1);
            assertEquals(200, response.getStatusCodeValue());
            assertEquals(rs, response.getBody());
        }
    
        @Test
        public void testGetByIdNotFound() {
            when(readySetups.findById(2)).thenReturn(Optional.empty());
    
            ResponseEntity<ReadySetup> response = api.getById(2);
            assertEquals(404, response.getStatusCodeValue());
            assertNull(response.getBody());
        }
    
        @Test
        public void testNewElement() {
            ReadySetupDTO dto = new ReadySetupDTO();
            dto.setName("Setup1");
            dto.setComponentList(new HashSet<>(List.of(1, 2)));
            dto.setUsage("gaming");
            dto.setImagePath("img.png");
            dto.setTotalPrice(100.0f);
    
            Component c1 = new Component();
            c1.setId(1);
            Component c2 = new Component();
            c2.setId(2);
    
            when(components.findById(1)).thenReturn(Optional.of(c1));
            when(components.findById(2)).thenReturn(Optional.of(c2));
            when(readySetups.save(any(ReadySetup.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
    
            ResponseEntity<ReadySetup> response = api.newElement(Map.of("token", "123"), dto);
            assertEquals(200, response.getStatusCodeValue());
            assertEquals("Setup1", response.getBody().getName());
            assertEquals(2, response.getBody().getComponentList().size());
        }
    
        @Test
        public void testUpdateElementFound() {
            ReadySetup existing = new ReadySetup();
            existing.setId(1);
            when(readySetups.findById(1)).thenReturn(Optional.of(existing));
            when(components.findById(anyInt())).thenReturn(Optional.of(new Component()));
            when(readySetups.save(any(ReadySetup.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
    
            ReadySetupDTO dto = new ReadySetupDTO();
            dto.setName("Updated");
            dto.setComponentList(new HashSet<>(List.of(1)));
            dto.setUsage("office");
            dto.setImagePath("img2.png");
            dto.setTotalPrice(200.0f);
    
            ResponseEntity<ReadySetup> response = api.updateElement(Map.of("token", "123"), dto, 1);
            assertEquals(200, response.getStatusCodeValue());
            assertEquals("Updated", response.getBody().getName());
        }
    
        @Test
        public void testUpdateElementNotFound() {
            when(readySetups.findById(2)).thenReturn(Optional.empty());
    
            ReadySetupDTO dto = new ReadySetupDTO();
            ResponseEntity<ReadySetup> response = api.updateElement(Map.of("token", "123"), dto, 2);
            assertEquals(400, response.getStatusCodeValue());
        }
    
        @Test
        public void testDeleteElement() {
            when(readySetups.findById(1)).thenReturn(Optional.of(new ReadySetup()));
            ResponseEntity<Boolean> response = api.deleteElement(Map.of("token", "123"), 1);
            assertEquals(true, response.getBody());
        }
    
        @Test
        public void testDeleteCascade() {
            Component c = new Component();
            c.setId(1);
            ReadySetup rs = new ReadySetup();
            rs.setId(10);
            rs.setComponentList(Set.of(c));
    
            when(readySetups.findAll()).thenReturn(List.of(rs));
            ReadySetupAPI spyApi = spy(api);
            doReturn(true).when(spyApi).deleteEntity(10);
    
            spyApi.deleteCascade(1);
            verify(spyApi).deleteEntity(10);
        }
    
        @Test
        public void testDeleteEntity() {
            when(readySetups.findById(1)).thenReturn(Optional.of(new ReadySetup()));
            Boolean result = api.deleteEntity(1);
            assertTrue(result);
    
            when(readySetups.findById(2)).thenReturn(Optional.empty());
            result = api.deleteEntity(2);
            assertFalse(result);
        }
    }

