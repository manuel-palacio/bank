package com.palace.smallapp.bank.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import java.util.Map;

@Configuration
public class EventConfig {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    TaskExecutor taskExecutor;


    @Bean
    public EventBus eventBus() {
        EventBus eventBus = new AsyncEventBus(taskExecutor);
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EventHandler.class);
        for (String bean : beans.keySet()) {
            eventBus.register(beans.get(bean));
        }
        return eventBus;
    }
}
