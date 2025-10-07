package it.skinjobs.api;

import it.skinjobs.model.Component;
import it.skinjobs.model.ComponentFamily;
import it.skinjobs.dto.ComponentDTO;
import it.skinjobs.repository.ComponentFamilies;
import it.skinjobs.repository.Components;
import it.skinjobs.utils.Callable;
import it.skinjobs.utils.Environment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jessica Vecchia
 *
 *         The REST controller transforms all the methods into web services and
 *         the classes into JSON object. The methods define
 *         calls to URLs via HTTP request(POST, GET, PUT, DELETE...)
 */
@RestController
public class ComponentAPI extends BaseAPI<Component, ComponentDTO, Integer> {
   private final Components components;
   private final ComponentFamilies componentFamilies;
   private final ReadySetupAPI readySetupAPI;

   /**
    * Constructor with dependency injection
    * 
    * @param credentialAPI     the credential API for authentication
    * @param components        the components repository
    * @param componentFamilies the component families repository
    * @param readySetupAPI     the ready setup API for cascade operations
    */
   public ComponentAPI(CredentialAPI credentialAPI,
         Components components,
         ComponentFamilies componentFamilies,
         ReadySetupAPI readySetupAPI) {
      super(credentialAPI);
      this.components = components;
      this.componentFamilies = componentFamilies;
      this.readySetupAPI = readySetupAPI;
   }

   /**
    *
    * @return ResponseBody
    *
    *         This API returns all the components in the database.
    */
   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @GetMapping("/components")
   public @ResponseBody Iterable<Component> getAll() {
      return components.findAll();
   }

   /**
    *
    * @param index
    * @return ResponseBody
    *
    *         This API return a component according to its id.
    */
   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @GetMapping("/component/{index}")
   public ResponseEntity<Iterable<Component>> getAllByType(@PathVariable Integer index) {
      List<Component> result = components.findByTypeId(index);
      return new ResponseEntity<>(result, HttpStatus.OK);

   }

   /**
    *
    * @param index
    * @return ResponseEntity
    *
    *         This API returns components according to their component type.
    */
   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @GetMapping("/components/{index}")
   public ResponseEntity<Component> getById(@PathVariable Integer index) {
      return components.findById(index)
            .map(component -> new ResponseEntity<>(component, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
   }

   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)

   /**
    *
    * @param headers
    * @param componentDTO
    * @return ResponseEntity
    *
    *         This API allows the admin to add a new component.
    */
   @PostMapping("/component")
   public ResponseEntity<Component> newElement(@RequestHeader Map<String, String> headers,
         @RequestBody ComponentDTO componentDTO) {
      return super.sessionOperation(headers, componentDTO, new Callable<>() {
         @Override
         public ResponseEntity<Component> call(ComponentDTO componentDTO) {
            Component component = new Component();
            component.setName(componentDTO.getName());
            component.setPrice(componentDTO.getPrice());
            component.setPower(componentDTO.getPower());
            Optional<ComponentFamily> optionalFamily = componentFamilies.findById(componentDTO.getFamilyId());
            if (optionalFamily.isPresent()) {
               component.setComponentFamily(optionalFamily.get());
               return new ResponseEntity<>(components.save(component), HttpStatus.OK);
            } else {
               return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
         }
      });
   }

   /**
    *
    * @param headers
    * @param componentDTO
    * @param index
    * @return ResponseEntity
    *
    *         This API allows the admin to modify a component.
    */
   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @PutMapping("/component/{index}")
   public ResponseEntity<Component> updateElement(@RequestHeader Map<String, String> headers,
         @RequestBody ComponentDTO componentDTO, final @PathVariable Integer index) {
      return super.sessionOperation(headers, componentDTO, new Callable<>() {
         @Override
         public ResponseEntity<Component> call(ComponentDTO componentDTO) {
            Optional<ComponentFamily> optionalFamily = componentFamilies.findById(componentDTO.getFamilyId());
            if (optionalFamily.isPresent()) {
               return components.findById(index).map(component -> {
                  component.setName(componentDTO.getName());
                  component.setPrice(componentDTO.getPrice());
                  component.setPower(componentDTO.getPower());
                  component.setComponentFamily(optionalFamily.get());

                  return new ResponseEntity<>(components.save(component), HttpStatus.OK);
               }).orElseGet(() -> null);
            } else {
               return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
         }
      });
   };

   /**
    *
    * @param headers
    * @param index
    * @return boolean
    *
    *         This API allows the admin to delete a component.
    */
   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @DeleteMapping("/component/{index}")
   public ResponseEntity<Boolean> deleteElement(@RequestHeader Map<String, String> headers,
         @PathVariable Integer index) {
      return super.sessionDeleteOperation(headers, index);
   }

   /**
    * @author Andrei Blindu
    * @author Jessica Vecchia
    * @param familyId
    * @return Boolean
    */
   public Boolean deleteCascade(Integer familyId) {
      Iterable<Component> componentList = this.components.findComponentsByFamilyId(familyId);
      for (Component component : componentList) {
         this.deleteEntity(component.getId());
      }
      return true;
   }

   /**
    * @author Andrei Blindu
    * @author Jessica Vecchia
    * @param index
    * @return Booolean
    */
   @Override
   protected Boolean deleteEntity(Integer index) {
      if (components.findById(index).isPresent()) {
         readySetupAPI.deleteCascade(index);
         components.deleteById(index);
         return true;
      } else {
         return false;
      }
   }

}