package it.skinjobs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Filippo Casarosa
 * @author Filippo Maria Rognoni
 * @author Andrei Blindu
 * 
 * DTO--> DATA TRANSFER OBJECT (POJO class in Maven Framework). This object transfers data from one layer to another
 * From the client's point of view this is a read-only object having just getters and setters and no behaviour
 * This is aimed at API stability making the business logic independent from the client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompatibilityConstraintDTO {
    
    private Integer componentFamilyId1;
    private Integer componentFamilyId2;
}