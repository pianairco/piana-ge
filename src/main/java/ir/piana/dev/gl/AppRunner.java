package ir.piana.dev.gl;

import ir.piana.dev.gl.t6.T6RenderUnit;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ComponentScan(basePackages = "ir.piana.dev.gl")
@PropertySource(value = {
        "classpath:/application.yml"
})
public class AppRunner {
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("/application.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    public static void main(String[] args) throws ClassNotFoundException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AppRunner.class);
        context.refresh();

        ControlManager controlManager = context.getBean(ControlManager.class);
        controlManager.run();
    }
}
