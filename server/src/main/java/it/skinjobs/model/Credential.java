package it.skinjobs.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Filippo Casarosa
 * this entity class represent the DB Schema in which credential id, name and password of the
 * website admin are saved.
 */
@Entity
@Getter
@Setter
public class Credential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    private String name;
    
    private String password;
}