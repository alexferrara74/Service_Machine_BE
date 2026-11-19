package pilot.servizi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Serve il frontend compilato dalla stessa porta delle API (un solo servizio da installare).
 * Con {@code WEB_DIR} vuoto non fa nulla: in sviluppo il frontend gira con {@code ng serve}.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE) // deve registrare "/**" prima di Spring Boot, che altrimenti lo occupa con i suoi default
public class WebConfig implements WebMvcConfigurer {

    private static final String INDICE = "index.csr.html";

    @Value("${app.web-dir:}")
    private String webDir;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        if (!webDir.isBlank()) {
            registry.addViewController("/").setViewName("forward:/" + INDICE); // il percorso vuoto non passa dal risolutore
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (webDir.isBlank()) {
            return;
        }
        registry.addResourceHandler("/**")
                .addResourceLocations(Path.of(webDir.trim()).toUri().toString())
                .setCacheControl(CacheControl.noCache()) // a ogni aggiornamento i client rileggono i file (304 se invariati)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String path, Resource location) throws IOException {
                        Resource file = super.getResource(path, location);
                        if (file != null) {
                            return file;
                        }
                        // le rotte di Angular (/clienti/3, /appuntamenti/2/5...) non sono file: si rilegge l'indice;
                        // un /api/... sbagliato resta un 404 vero
                        return path.startsWith("api/") ? null : location.createRelative(INDICE);
                    }
                });
    }
}
