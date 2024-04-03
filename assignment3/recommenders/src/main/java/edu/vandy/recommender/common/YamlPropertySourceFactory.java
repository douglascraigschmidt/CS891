package edu.vandy.recommender.common;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * Helper class that loads application resources from a .yaml file.
 */
public class YamlPropertySourceFactory
       implements PropertySourceFactory {

    @NotNull
    @Override
    public PropertySource<?> createPropertySource(
            String name,
            EncodedResource encodedResource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        Properties properties = factory.getObject();

        return new PropertiesPropertySource(
                Objects.requireNonNull(encodedResource.getResource().getFilename()),
                Objects.requireNonNull(properties));
    }
}
