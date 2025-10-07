package it.skinjobs.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Filippo Casarosa
 *         The decorator entity tells Hibernate that this object will be a
 *         database entity
 */
@Entity
@Getter
@Setter
public class Component {

   /**
    * This decorator generates automatically the primary key
    */
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Integer id;

   private String name;

   private Float price;

   /**
    * This decorator allows us to realize a many to one relationship between the
    * ComponentFamily's primary key(id) and
    * each table referencing it with a foreign key, in this case family_id is a
    * foreign key for ComponentFamily's id
    */
   @ManyToOne
   @JoinColumn(name = "family_id", updatable = true)
   private ComponentFamily componentFamily;

   private Float power;

   // @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy =
   // "componentList")
   // private Set<ReadySetup> readySetups;
}