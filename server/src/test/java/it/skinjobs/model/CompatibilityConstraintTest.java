package it.skinjobs.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Filippo Casarosa
 * Test coverage for CompatibilityConstraint model with Lombok
 */
@SpringBootTest
class CompatibilityConstraintTest {
    
    @Test
    void testGettersAndSetters() {
        // Setup
        CompatibilityConstraint constraint = new CompatibilityConstraint();
        
        ComponentFamily family1 = new ComponentFamily();
        family1.setId(1);
        family1.setName("CPU");
        
        ComponentFamily family2 = new ComponentFamily();
        family2.setId(2);
        family2.setName("Motherboard");
        
        // Test setters and getters
        constraint.setId(100);
        constraint.setComponentFamily1(family1);
        constraint.setComponentFamily2(family2);
        
        // Assertions
        assertEquals(100, constraint.getId());
        assertEquals(family1, constraint.getComponentFamily1());
        assertEquals(family2, constraint.getComponentFamily2());
        assertEquals(1, constraint.getComponentFamily1().getId());
        assertEquals(2, constraint.getComponentFamily2().getId());
        assertEquals("CPU", constraint.getComponentFamily1().getName());
        assertEquals("Motherboard", constraint.getComponentFamily2().getName());
    }
    
    @Test
    void testNullValues() {
        CompatibilityConstraint constraint = new CompatibilityConstraint();
        
        // Test initial null values
        assertNull(constraint.getId());
        assertNull(constraint.getComponentFamily1());
        assertNull(constraint.getComponentFamily2());
    }
    
    @Test
    void testSetNullValues() {
        CompatibilityConstraint constraint = new CompatibilityConstraint();
        
        // Set values first
        ComponentFamily family = new ComponentFamily();
        constraint.setComponentFamily1(family);
        constraint.setComponentFamily2(family);
        
        // Now set to null
        constraint.setComponentFamily1(null);
        constraint.setComponentFamily2(null);
        
        // Verify null
        assertNull(constraint.getComponentFamily1());
        assertNull(constraint.getComponentFamily2());
    }
    
    @Test
    void testSameFamilyForBothConstraints() {
        CompatibilityConstraint constraint = new CompatibilityConstraint();
        
        ComponentFamily family = new ComponentFamily();
        family.setId(1);
        family.setName("Universal");
        
        // Set same family for both
        constraint.setComponentFamily1(family);
        constraint.setComponentFamily2(family);
        
        assertEquals(constraint.getComponentFamily1(), constraint.getComponentFamily2());
        assertSame(constraint.getComponentFamily1(), constraint.getComponentFamily2());
    }
}
