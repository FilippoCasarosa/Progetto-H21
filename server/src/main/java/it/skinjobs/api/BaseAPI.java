package it.skinjobs.api;

import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import it.skinjobs.utils.Callable;

/**
 *
 * @author Filippo Casarosa
 */
/**
 *
 * @param <T>
 * @param <U>
 * @param <I>
 *
 *            This class contains the basic operations that are necessary to be
 *            done before the admin manipulates database under authentication
 */
public abstract class BaseAPI<T, U, I> implements API<T, U, I> {

    protected final CredentialAPI credentialAPI;

    /**
     * Constructor injection for CredentialAPI dependency
     * 
     * @param credentialAPI the credential API instance
     * @throws NullPointerException if credentialAPI is null
     */
    protected BaseAPI(CredentialAPI credentialAPI) {
        this.credentialAPI = Objects.requireNonNull(credentialAPI, "CredentialAPI must not be null");
    }

    /**
     *
     * @param headers
     * @param dto
     * @param callable
     * @return ResponseEntity
     *
     *         If the token is valid for that session, the operation can be done.
     *         Otherwise it cannot be completed.
     */
    public ResponseEntity<T> sessionOperation(Map<String, String> headers, U dto,
            Callable<ResponseEntity<T>, U> callable) {
        String token = headers.get("token");
        if (credentialAPI.sessionIsValid(token)) {
            return callable.call(dto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     *
     * @param headers
     * @param id
     * @return ResponseEntity
     *
     *         If the token is valid for that session, the delete operation can be
     *         done. Otherwise it cannot be completed.
     *         For design reasons a separate method for delete operation is
     *         necessary.
     */

    public ResponseEntity<Boolean> sessionDeleteOperation(Map<String, String> headers, Integer id) {
        String token = headers.get("token");
        if (credentialAPI.sessionIsValid(token)) {
            if (deleteEntity(id)) {
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    protected abstract Boolean deleteEntity(Integer index);
}