package com.humerau;


import lombok.Setter;
import lombok.SneakyThrows;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class ObjectFactory {

    private List<ObjectConfigurator> configurators = new ArrayList<>();
    private List<ProxiConfigurator> proxiConfigurators = new ArrayList<>();
    private final ApplicationContext context;


    @SneakyThrows
    public ObjectFactory(ApplicationContext context) {

//        config = new JavaConfig("com.humerau", new HashMap<>(Map.of(Policeman.class, AngryPoliceman.class)));
        this.context = context;

        for (Class<? extends ObjectConfigurator> aClass : context.getConfig().getScanner().getSubTypesOf(ObjectConfigurator.class)) {
            configurators.add(aClass.getDeclaredConstructor().newInstance());
        }
        for (Class<? extends ProxiConfigurator> aClass : context.getConfig().getScanner().getSubTypesOf(ProxiConfigurator.class)) {
            proxiConfigurators.add(aClass.getDeclaredConstructor().newInstance());
        }
    }

    @SneakyThrows
    public <T> T createObject (Class<T> implClass) {

        T t = create(implClass);

        configure(t);

        invokeInit(implClass, t);

        t = wrapWithProxyIfNeeded(implClass, t);

        return t;

    }

    private <T> T wrapWithProxyIfNeeded(Class<T> implClass, T t) {
        for (ProxiConfigurator proxiConfigurator : proxiConfigurators) {
            t = (T) proxiConfigurator.replaceWithProxyIfNeeded(t, implClass);
        }
        return t;
    }

    private <T> void invokeInit(Class<T> implClass, T t) throws IllegalAccessException, InvocationTargetException {
        for (Method method : implClass.getMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.invoke(t);
            }
        }
    }

    private <T> void configure(T t) {
        configurators.forEach(objectConfigurator -> objectConfigurator.configure(t, context));
    }

    private <T> T create(Class<T> implClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return implClass.getDeclaredConstructor().newInstance();
    }
}
