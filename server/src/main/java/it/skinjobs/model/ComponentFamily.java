package it.skinjobs.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * @author Filippo Casarosa
 *         The decorator entity tells Hibernate that this object will be a
 *         database entity
 */
@Entity
@Getter
@Setter
public class ComponentFamily {

   /**
    * This decorator generates automatically the primary key
    */
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Integer id;

   private String name;

   /**
    * This decorator allows us to realize a many to one relationship between the
    * ComponentType's primary key(id) and
    * each table referencing it with a foreign key, in this case type_id is a
    * foreign key for ComponentType's id
    */
   @ManyToOne
   @JoinColumn(name = "type_id", updatable = true)
   private ComponentType type;
}