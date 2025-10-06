package it.skinjobs.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import it.skinjobs.dto.CredentialDTO;
import it.skinjobs.model.Credential;
import it.skinjobs.model.Session;
import it.skinjobs.repository.Credentials;
import it.skinjobs.repository.Sessions;
import it.skinjobs.utils.CredentialsProperties;

@RestController
public class CredentialAPI {

    private final Credentials credentials;
    private final Sessions sessions;
    private final Credential adminCredential;

    // Constructor injection replaces all field injections
    public CredentialAPI(Credentials credentials, Sessions sessions, CredentialsProperties credentialsProperties) {
        this.credentials = credentials;
        this.sessions = sessions;

        Credential tempAdmin = null;
        String name = credentialsProperties.getName();
        try {
            if (credentials.findByName(name).isEmpty()) {
                String password = credentialsProperties.getPassword();
                Credential credential = new Credential();
                credential.setName(name);
                credential.setPassword(password);
                tempAdmin = credentials.save(credential);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        this.adminCredential = tempAdmin;
    }

    public Credential getAdminCredential() {
        return adminCredential;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/login")
    public ResponseEntity<Session> login(@RequestBody CredentialDTO credentialDTO) {
        List<Credential> credentialList = this.credentials.findByName(credentialDTO.getName());
        if (!credentialList.isEmpty()) {
            Credential credential = credentialList.get(0);
            if (credential.getPassword().equals(credentialDTO.getPassword())) {
                Session session = new Session();
                session.setCredential(credential);
                return new ResponseEntity<>(this.sessions.save(session), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    public Session renewSession(Session session) {
        session.renewSession();
        return this.sessions.save(session);
    }

    public Boolean sessionIsValid(String token) {
        List<Session> sessionList = this.sessions.findByToken(token);
        if (!sessionList.isEmpty()) {
            Session session = sessionList.get(0);
            if (!session.isExpired()) {
                this.renewSession(session);
                return true;
            }
        }
        return false;
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestHeader Map<String, String> headers) {
        String token = headers.get("token");
        List<Session> sessionList = this.sessions.findByToken(token);
        if (sessionList.isEmpty() || sessionList.get(0).isExpired()) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        Session session = sessionList.get(0);
        session.setNowExpired();
        this.sessions.save(session);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
