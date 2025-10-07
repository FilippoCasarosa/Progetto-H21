package it.skinjobs.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Jessica Vecchia
 *         This class represents a DB Entity in which each authenticated session
 *         is saved. A session
 *         needs an id, token, expireDate and credentialId.
 */
@Entity
@Getter
@Setter
public class Session {

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Integer id;

   private String token;

   @ManyToOne
   @JoinColumn(name = "credential_id", updatable = false)
   private Credential credential;

   private Date expireDate;

   /**
    * Default constructor that creates a session with unique token and 30-minute
    * expiration
    */
   public Session() {
      this.token = UUID.randomUUID().toString();
      this.renewSession();
   }

   /**
    * Constructor with credential that creates a session with unique token and sets
    * expiration
    */
   public Session(Credential credential) {
      this();
      this.credential = credential;
   }

   /**
    * @return Boolean
    *         This method checks if a session is expired or not and has to be
    *         called within the
    *         credentialApi class to check if a session is valid)
    */
   public Boolean isExpired() {
      Date date = new Date();
      return date.after(this.expireDate);
   }

   /**
    * this method has the single responsibility to renew a session letting it last
    * 30 minutes more
    */
   public void renewSession() {
      Instant time = Instant.now();
      Instant timeFuture = time.plus(30, ChronoUnit.MINUTES);
      this.expireDate = Date.from(timeFuture);
   }

   /**
    * this method set the expiration date on the session row within the DB one
    * second before
    * now (aimed at the logout API).
    */
   public void setNowExpired() {
      Instant time = Instant.now();
      Instant timePast = time.minus(1, ChronoUnit.SECONDS);
      this.expireDate = Date.from(timePast);
   }
}
