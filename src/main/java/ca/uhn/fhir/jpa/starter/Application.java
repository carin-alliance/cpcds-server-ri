package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.batch2.jobs.config.Batch2JobsConfig;
import ca.uhn.fhir.jpa.batch2.JpaBatch2Config;
import ca.uhn.fhir.jpa.starter.annotations.OnEitherVersion;
import ca.uhn.fhir.jpa.starter.authorization.OauthEndpointController;
import ca.uhn.fhir.jpa.starter.cdshooks.StarterCdsHooksConfig;
import ca.uhn.fhir.jpa.starter.cr.StarterCrDstu3Config;
import ca.uhn.fhir.jpa.starter.cr.StarterCrR4Config;
import ca.uhn.fhir.jpa.starter.debug.DebugEndpointController;
import ca.uhn.fhir.jpa.starter.mdm.MdmConfig;
import ca.uhn.fhir.jpa.starter.wellknown.WellKnownEndpointController;
import ca.uhn.fhir.jpa.subscription.channel.config.SubscriptionChannelConfig;
import ca.uhn.fhir.jpa.subscription.match.config.SubscriptionProcessorConfig;
import ca.uhn.fhir.jpa.subscription.match.config.WebsocketDispatcherConfig;
import ca.uhn.fhir.jpa.subscription.submit.config.SubscriptionSubmitterConfig;
import ca.uhn.fhir.rest.server.RestfulServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@ServletComponentScan(basePackageClasses = {RestfulServer.class})
@SpringBootApplication(exclude = {ElasticsearchRestClientAutoConfiguration.class, ThymeleafAutoConfiguration.class})
@Import({
	StarterCrR4Config.class,
	StarterCrDstu3Config.class,
	StarterCdsHooksConfig.class,
	SubscriptionSubmitterConfig.class,
	SubscriptionProcessorConfig.class,
	SubscriptionChannelConfig.class,
	WebsocketDispatcherConfig.class,
	MdmConfig.class,
	JpaBatch2Config.class,
	Batch2JobsConfig.class
})
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);

		// Server is now accessible at eg. http://localhost:8080/fhir/metadata
		// UI is now accessible at http://localhost:8080/
	}

	@Autowired
	AutowireCapableBeanFactory beanFactory;

	@Autowired
	private AppProperties appProperties;

	@Bean
	@Conditional(OnEitherVersion.class)
	public ServletRegistrationBean<RestfulServer> hapiServletRegistration(RestfulServer restfulServer) {

		restfulServer.registerInterceptor(new Metadata(appProperties));
		restfulServer.registerInterceptor(new PatientAuthorizationInterceptor());
		restfulServer.registerInterceptor(new ReadOnlyInterceptor());

		ServletRegistrationBean<RestfulServer> servletRegistrationBean = new ServletRegistrationBean<RestfulServer>();
		beanFactory.autowireBean(restfulServer);
		servletRegistrationBean.setServlet(restfulServer);
		servletRegistrationBean.addUrlMappings("/fhir/*");
		servletRegistrationBean.setLoadOnStartup(1);

		return servletRegistrationBean;
	}

	@Bean
	public ServletRegistrationBean<DispatcherServlet> dispatcherRegistration(DispatcherServlet dispatcherServlet) {
		dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);
		dispatcherServlet.setContextConfigLocation(WellKnownEndpointController.class.getName());

		ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(dispatcherServlet, "/fhir/.well-known/*");
		registration.setLoadOnStartup(1);
		return registration;
	}

	@Bean
	public ServletRegistrationBean<DispatcherServlet> oauthEndpointRegistration(DispatcherServlet dispatcherServlet) {
			dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);
			dispatcherServlet.setContextConfigLocation(OauthEndpointController.class.getName());

			ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(dispatcherServlet, "/oauth/*");
			registration.setLoadOnStartup(2);
			return registration;
	}

	@Bean
	public ServletRegistrationBean<DispatcherServlet> debugEndpointRegistration(DispatcherServlet dispatcherServlet) {
			dispatcherServlet.setContextClass(AnnotationConfigWebApplicationContext.class);
			dispatcherServlet.setContextConfigLocation(DebugEndpointController.class.getName());

			ServletRegistrationBean<DispatcherServlet> registration = new ServletRegistrationBean<>(dispatcherServlet, "/debug/*");
			registration.setLoadOnStartup(3);
			return registration;
	}

}
