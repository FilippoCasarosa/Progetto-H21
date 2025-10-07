package it.skinjobs.api;

import it.skinjobs.model.ComponentType;
import it.skinjobs.dto.ComponentTypeDTO;
import it.skinjobs.repository.ComponentTypes;
import it.skinjobs.utils.Callable;
import it.skinjobs.utils.Environment;

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
 *         the classes into JSON objects. The methods define calls to URLs via
 *         HTTP request (POST, GET, PUT, DELETE...).
 */
@RestController
public class ComponentTypeAPI extends DeleteAllAPI<ComponentType, ComponentTypeDTO, Integer> {

   private final ComponentTypes componentTypes;
   private final ComponentFamilyAPI componentFamilyAPI;

   /**
    * Constructor with dependency injection
    * 
    * @param credentialAPI      the credential API for authentication
    * @param componentTypes     the repository for component types
    * @param componentFamilyAPI the API for managing related families
    */
   public ComponentTypeAPI(CredentialAPI credentialAPI,
         ComponentTypes componentTypes,
         ComponentFamilyAPI componentFamilyAPI) {
      super(credentialAPI);
      this.componentTypes = componentTypes;
      this.componentFamilyAPI = componentFamilyAPI;
   }

   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @GetMapping("/componentTypes")
   public @ResponseBody Iterable<ComponentType> getAll() {
      return componentTypes.findAllSorted();
   }

   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @GetMapping("/componentTypes/{index}")
   public ResponseEntity<ComponentType> getById(@PathVariable Integer index) {
      Optional<ComponentType> result = this.componentTypes.findById(index);
      return result.map(componentType -> new ResponseEntity<>(componentType, HttpStatus.OK))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
   }

   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @PostMapping("/componentType")
   public ResponseEntity<ComponentType> newElement(@RequestHeader Map<String, String> headers,
         @RequestBody ComponentTypeDTO componentTypeDTO) {
      return super.sessionOperation(headers, componentTypeDTO, new Callable<>() {
         @Override
         public ResponseEntity<ComponentType> call(ComponentTypeDTO dto) {
            ComponentType componentType = new ComponentType();
            componentType.setName(dto.getName());
            componentType.setSortOrder(dto.getSortOrder());
            return new ResponseEntity<>(componentTypes.save(componentType), HttpStatus.OK);
         }
      });
   }

   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @PutMapping("/componentType/{index}")
   public ResponseEntity<ComponentType> updateElement(@RequestHeader Map<String, String> headers,
         @RequestBody ComponentTypeDTO componentTypeDTO, @PathVariable Integer index) {
      return super.sessionOperation(headers, componentTypeDTO, new Callable<>() {
         @Override
         public ResponseEntity<ComponentType> call(ComponentTypeDTO dto) {
            return componentTypes.findById(index)
                  .map(componentType -> {
                     componentType.setName(dto.getName());
                     componentType.setSortOrder(dto.getSortOrder());
                     return new ResponseEntity<>(componentTypes.save(componentType), HttpStatus.OK);
                  })
                  .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
         }
      });
   }

   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @DeleteMapping("/componentType/{typeId}")
   public ResponseEntity<Boolean> deleteElement(@RequestHeader Map<String, String> headers,
         @PathVariable Integer typeId) {
      return super.sessionDeleteOperation(headers, typeId);
   }

   @CrossOrigin(origins = Environment.LOCAL_ANGULAR_DOMAIN)
   @DeleteMapping("/deleteAll")
   public ResponseEntity<Boolean> deleteAll(@RequestHeader Map<String, String> headers) {
      return super.sessionDeleteAllOperation(headers, new Callable<>() {
         @Override
         public ResponseEntity<Boolean> call(ComponentTypeDTO dto) {
            Iterable<ComponentType> componentTypeList = componentTypes.findAll();
            for (ComponentType componentType : componentTypeList) {
               deleteEntity(componentType.getId());
            }
            return new ResponseEntity<>(true, HttpStatus.OK);
         }
      });
   }

   public Boolean deleteEntity(Integer typeId) {
      if (componentTypes.findById(typeId).isPresent()) {
         componentFamilyAPI.deleteCascade(typeId);
         componentTypes.deleteById(typeId);
         return true;
      } else {
         return false;
      }
   }
}
