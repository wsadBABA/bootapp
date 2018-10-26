package com.bootapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.util.Properties;

/**
 * @Author: mrcheyg
 * @Date: Created in 2017/12/16 19:32
 */
public class SpringBootStartupListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger log = LoggerFactory.getLogger(SpringBootStartupListener.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String configPath = SpringBootStartupListener.class.getResource("/properties/").getPath();
        File file = new File(configPath);
        String [] files = file.list();
        loadProjectProperties(files, configPath, event);
        log.info(event.getEnvironment().getPropertySources().toString());
    }

    private void loadProjectProperties(String[] files, String configPath, ApplicationEnvironmentPreparedEvent event){
        for(String file : files) {
            Properties properties = loadConfigProperties(configPath + file);
            PropertySource propertySource = new PropertiesPropertySource(file, properties);
            event.getEnvironment().getPropertySources().addBefore("systemEnvironment", propertySource);
        }
    }

    private  Properties loadConfigProperties(String configPath) {
        Properties properties = null;
        try {
            Resource resource = new UrlResource("file:" + configPath);
            properties = PropertiesLoaderUtils.loadProperties(resource);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("can not load project config properties!" + e.getMessage());
            throw new Error(e.getMessage());
        }
        return properties;
    }

}
