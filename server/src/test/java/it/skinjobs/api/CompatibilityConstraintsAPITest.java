package it.skinjobs.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.Mockito;

import it.skinjobs.ConfiguratorApplicationAPI;
import it.skinjobs.config.H2TestProfileConfig;
import it.skinjobs.model.CompatibilityConstraint;
import it.skinjobs.model.Component;
import it.skinjobs.model.ComponentFamily;
import it.skinjobs.repository.CompatibilityConstraints;
import it.skinjobs.repository.Components;

/**
 * @author Filippo Casarosa
 * Updated with better coverage for Lombok models
 */
@SpringBootTest(classes = { ConfiguratorApplicationAPI.class, H2TestProfileConfig.class })
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class CompatibilityConstraintsAPITest {
    
    @Autowired
    MockMvc mockMvc;
    
    @Autowired
    ObjectMapper mapper;
    
    @Autowired
    CompatibilityConstraintsAPI compatibilityConstraintsAPI;
    
    @MockBean
    private Components components;
    
    @MockBean
    private CompatibilityConstraints compatibilityConstraints;
    
    @Test
    public void getCompatibleComponentsTest() throws Exception {
        // Setup input component with family
        ComponentFamily inputFamily = new ComponentFamily();
        inputFamily.setId(0);
        
        Component inputComponent = new Component();
        inputComponent.setId(1);
        inputComponent.setComponentFamily(inputFamily);
        
        Mockito.when(components.findById(anyInt())).thenReturn(Optional.of(inputComponent));
        
        // Setup constraint 1
        CompatibilityConstraint constraint1 = new CompatibilityConstraint();
        constraint1.setId(1);
        
        ComponentFamily family2 = new ComponentFamily();
        family2.setId(2);
        family2.setName("Family2");
        
        Component componentFamily2 = new Component();
        componentFamily2.setId(2);
        componentFamily2.setName("Family2Component");
        componentFamily2.setComponentFamily(family2);
        
        constraint1.setComponentFamily1(inputFamily);
        constraint1.setComponentFamily2(family2);
        
        // Setup constraint 2
        CompatibilityConstraint constraint2 = new CompatibilityConstraint();
        constraint2.setId(2);
        
        ComponentFamily family3 = new ComponentFamily();
        family3.setId(3);
        family3.setName("Family3");
        
        Component componentFamily3 = new Component();
        componentFamily3.setId(3);
        componentFamily3.setName("Family3Component");
        componentFamily3.setComponentFamily(family3);
        
        constraint2.setComponentFamily1(inputFamily);
        constraint2.setComponentFamily2(family3);
        
        // Setup constraints list
        ArrayList<CompatibilityConstraint> constraints = new ArrayList<>();
        constraints.add(constraint1);
        constraints.add(constraint2);
        
        List<Component> list1 = new ArrayList<>();
        list1.add(componentFamily2);
        
        List<Component> list2 = new ArrayList<>();
        list2.add(componentFamily3);
        
        Mockito.when(compatibilityConstraints.findByFamilyId(anyInt())).thenReturn(constraints);
        Mockito.when(components.findComponentsByFamilyId(2)).thenReturn(list1);
        Mockito.when(components.findComponentsByFamilyId(3)).thenReturn(list2);
        
        mockMvc.perform(MockMvcRequestBuilders
                .get("/compatibilityConstraints/getByComponentId/1"))
                .andExpect(status().isOk());
    }
}