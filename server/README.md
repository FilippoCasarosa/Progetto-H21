# springapi

Simple SpringBoot API written in Java with Maven.

Useful command for running sonarquebe for this app:
mvn clean test
mvn sonar:sonar -Dsonar.projectKey=Progetto-H21 -Dsonar.projectName="Progetto H21" -Dsonar.host.url=http://localhost:9000 -Dsonar.login=MY_SONAR_TOKEN -Dsonar.java.coveragePlugin=jacoco -Dsonar.coverage.jacoco.xmlReportPaths=target/jacoco-report/jacoco.xml
