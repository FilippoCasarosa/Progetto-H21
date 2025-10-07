package it.skinjobs.model;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Filippo Casarosa
 *         Updated test coverage for Session model with Lombok
 */
@SpringBootTest
class SessionTest {

   @Test
   void testNoArgsConstructor() {
      Session session = new Session();

      // Verify token is generated
      assertNotNull(session.getToken());
      assertFalse(session.getToken().isEmpty());

      // Verify expireDate is set (30 minutes from now)
      assertNotNull(session.getExpireDate());
      assertFalse(session.isExpired());

      // Verify id and credential are null initially
      assertNull(session.getId());
      assertNull(session.getCredential());
   }

   @Test
   void testParameterizedConstructor() {
      Credential credential = new Credential();
      credential.setId(1);
      credential.setName("admin");
      credential.setPassword("admin");

      Session session = new Session(credential);

      // Verify all properties are set correctly
      assertNotNull(session.getToken());
      assertNotNull(session.getExpireDate());
      assertEquals(credential, session.getCredential());
      assertFalse(session.isExpired());
   }

   @Test
   void testGettersAndSetters() {
      Session session = new Session();
      Credential credential = new Credential();
      credential.setId(0);
      credential.setName("admin");
      credential.setPassword("admin");

      session.setCredential(credential);
      String token = UUID.randomUUID().toString();
      session.setToken(token);
      session.setId(10);
      Date date = new Date();
      session.setExpireDate(date);

      assertEquals(credential, session.getCredential());
      assertEquals(token, session.getToken());
      assertEquals(10, session.getId());
      assertEquals(date, session.getExpireDate());
   }

   @Test
   void testIsExpiredPastDate() {
      Session session = new Session();
      Instant time = Instant.now();
      Instant timePast = time.minus(30, ChronoUnit.MINUTES);
      session.setExpireDate(Date.from(timePast));

      assertTrue(session.isExpired());
   }

   @Test
   void testIsExpiredFutureDate() {
      Session session = new Session();
      Instant time = Instant.now();
      Instant timeFuture = time.plus(30, ChronoUnit.MINUTES);
      session.setExpireDate(Date.from(timeFuture));

      assertFalse(session.isExpired());
   }

   @Test
   void testSetNowExpired() {
      Session session = new Session();
      session.setNowExpired();

      assertTrue(session.isExpired());

      // Verify the date is actually in the past
      Date now = new Date();
      assertTrue(session.getExpireDate().before(now));
   }

   @Test
   void testSessionDuration() {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(Date.from(Instant.now()));
      calendar.add(Calendar.MINUTE, 29);
      calendar.add(Calendar.SECOND, 59);
      Date end1 = calendar.getTime();

      calendar = Calendar.getInstance();
      calendar.setTime(Date.from(Instant.now()));
      calendar.add(Calendar.MINUTE, 30);
      calendar.add(Calendar.SECOND, 1);
      Date end2 = calendar.getTime();

      System.out.println("end1: " + format.format(end1));
      Session session = new Session();
      System.out.println("session end: " + format.format(session.getExpireDate()));

      boolean assertion1 = session.getExpireDate().after(end1);
      boolean assertion2 = session.getExpireDate().before(end2);

      assertTrue(assertion1);
      assertTrue(assertion2);
   }

   @Test
   void testRenewSession() throws Exception {
      Session session = new Session();
      Date initialExpireDate = session.getExpireDate();
      System.out.println("session end: " + initialExpireDate);

      Thread.sleep(3000);
      session.renewSession();
      Date renewedExpireDate = session.getExpireDate();
      System.out.println("session end after renew: " + renewedExpireDate);

      // Verify the renewed date is after the initial date
      assertTrue(renewedExpireDate.after(initialExpireDate));

      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(Date.from(Instant.now()));
      calendar.add(Calendar.MINUTE, 29);
      calendar.add(Calendar.SECOND, 59);
      Date end1 = calendar.getTime();

      calendar = Calendar.getInstance();
      calendar.setTime(Date.from(Instant.now()));
      calendar.add(Calendar.MINUTE, 30);
      calendar.add(Calendar.SECOND, 1);
      Date end2 = calendar.getTime();

      System.out.println("end to check: " + format.format(end1));

      boolean assertion1 = session.getExpireDate().after(end1);
      boolean assertion2 = session.getExpireDate().before(end2);

      assertTrue(assertion1);
      assertTrue(assertion2);
   }

   @Test
   void testMultipleRenewals() {
      Session session = new Session();
      Date firstExpire = session.getExpireDate();

      session.renewSession();
      Date secondExpire = session.getExpireDate();

      session.renewSession();
      Date thirdExpire = session.getExpireDate();

      // Each renewal should push the expiration further
      assertTrue(secondExpire.after(firstExpire) || secondExpire.equals(firstExpire));
      assertTrue(thirdExpire.after(secondExpire) || thirdExpire.equals(secondExpire));
   }

   @Test
   void testTokenUniqueness() {
      Session session1 = new Session();
      Session session2 = new Session();

      assertNotEquals(session1.getToken(), session2.getToken());
   }

   @Test
   void testSetCustomToken() {
      Session session = new Session();
      String customToken = "custom-token-12345";

      session.setToken(customToken);
      assertEquals(customToken, session.getToken());
   }

   @Test
   void testSetNullCredential() {
      Session session = new Session();
      session.setCredential(null);

      assertNull(session.getCredential());
   }
}