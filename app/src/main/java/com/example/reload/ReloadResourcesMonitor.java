package com.example.reload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class ReloadResourcesMonitor implements ApplicationContextAware {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private ApplicationContext ctx;

    private List<String> resourceNames = new ArrayList<>();

    private Map<String, Long> lastModifiedMap = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @PostConstruct
    protected void init() {
        Pattern pattern = Pattern.compile("class path resource \\[(.*?)\\]");
        MutablePropertySources propertySources = ((ConfigurableEnvironment) ctx.getEnvironment()).getPropertySources();
        resourceNames = StreamSupport.stream(propertySources.spliterator(), false)
                .filter(ps -> ps.getName().startsWith("Config resource 'class path resource ["))
                .map(ps -> {
                    Matcher m = pattern.matcher(ps.getName());
                    if(m.find()) {
                        return m.group(1);
                    }
                    return "";
                })
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 2000)
    public void reload() {
        List<String> reloadFiles = new ArrayList<>();
        for (String resourceName : resourceNames) {
            Resource resource = ctx.getResource("classpath:" + resourceName);
            long newTime = getLastModified(resource);
            long lastTime = lastModifiedMap.computeIfAbsent(resourceName, k -> newTime);
            if(lastTime < newTime) {
                reloadFiles.add(resourceName);
                lastModifiedMap.put(resourceName, newTime);
            }
        }
        if(!reloadFiles.isEmpty()) {
            eventPublisher.publishEvent(new RefreshEvent(this, reloadFiles, "refresh:" + reloadFiles));
        }
    }

    private long getLastModified(Resource resource)  {
        try {
            return resource.isFile() ? resource.getFile().lastModified() : 0;
        } catch (IOException e) {
            log.error("resource:" + resource.getFilename(), e);
            return 0;
        }
    }

}
