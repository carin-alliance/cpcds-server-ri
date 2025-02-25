package ca.uhn.fhir.jpa.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "security")
@Configuration
@EnableConfigurationProperties
public class SecurityProperties {
  
  @Getter @Setter
  private String adminToken;

  @Getter @Setter
  private Boolean enabled = true;

  @Getter @Setter
  private Boolean readOnly = true;

}
