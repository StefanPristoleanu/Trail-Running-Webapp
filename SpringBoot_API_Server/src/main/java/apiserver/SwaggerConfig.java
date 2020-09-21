package apiserver;

import com.google.common.base.Predicates;
import java.util.ArrayList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Profile("dev")
public class SwaggerConfig extends WebMvcConfigurationSupport {

  // http://localhost:9090/swagger-ui.html
  // http://localhost:9090/v2/api-docs
  @Bean
  public Docket productApi() {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(apiInfo())
        .select()
        .apis(RequestHandlerSelectors.basePackage("apiserver.controllers"))
        .paths(PathSelectors.any())
        // .paths(PathSelectors.regex("/api-server.*"))
        .paths(Predicates.not(PathSelectors.regex("/error.*")))
        // .paths(Predicates.not(PathSelectors.regex("/swagger.*")))
        .build()
        .securitySchemes(securitySchemes());
  }

  // Describe your apis
  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("Trail Running RestAPI")
        .description("This application display the methods for user and trail controllers.")
        .version("0.1")
        .build();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");

    registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  // spring security auth:
  private static ArrayList<? extends SecurityScheme> securitySchemes() {
    ArrayList securitylist = new ArrayList();
    securitylist.add(new ApiKey("Bearer", "Authorization", "header"));
    return securitylist;
  }
}
