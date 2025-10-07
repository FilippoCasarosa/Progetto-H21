package it.skinjobs.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.Set;

/**
 * @author Filippo Casarosa
 *         The decorator entity tells Hibernate that this object will be a
 *         database entity
 */
@Entity
@Getter
@Setter
public class ReadySetup {

   /**
    * @author Filippo Casarosa
    *         This decorator generates automatically the primary key
    */
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Integer id;

   private String name;

   @ManyToMany
   @JoinTable(name = "Ready_set_up_components", joinColumns = @JoinColumn(name = "readySetup_id"), inverseJoinColumns = @JoinColumn(name = "component_id"))
   private Set<Component> componentList;

   private String typeUse;

   private String imagePath;

   private Float totalPrice;

   // Metodi custom per il campo typeUse (mantengono la naming convention
   // originale)
   public String getUsage() {
      return typeUse;
   }

   public void setUsage(String usage) {
      this.typeUse = usage;
   }
}