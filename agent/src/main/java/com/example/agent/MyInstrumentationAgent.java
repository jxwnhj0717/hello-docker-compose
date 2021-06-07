package com.example.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class MyInstrumentationAgent {

    private static Logger LOGGER = LoggerFactory.getLogger(MyInstrumentationAgent.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        LOGGER.info("[Agent] In premain method");

        String[] classNames = parseClassNames(agentArgs);
        transformClasses(classNames, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        LOGGER.info("[Agent] In agentmain method");

        String[] classNames = parseClassNames(agentArgs);
        transformClasses(classNames, inst);
    }

    private static String[] parseClassNames(String agentArgs) {
        return agentArgs.split("[,\\s]+");
    }

    private static void transformClasses(String[] classNames, Instrumentation instrumentation) {
        List<Class<?>> classes = new ArrayList<>(classNames.length);
        for (String className : classNames) {
            Class<?> clazz = findClassByName(className, instrumentation);
            if (clazz == null) {
                continue;
            }
            classes.add(clazz);
        }
        LOGGER.info("transformClasses:" + classes);
        if(classes.isEmpty()) {
            return;
        }
        for (Class<?> clz : classes) {
            AtmTransformer dt = new AtmTransformer(clz.getName(), clz.getClassLoader());
            instrumentation.addTransformer(dt, true);
        }
        try {
            instrumentation.retransformClasses(classes.toArray(new Class[0]));
        } catch (Exception e) {
            throw new RuntimeException("Transform failed", e);
        }
    }

    private static Class<?> findClassByName(String className, Instrumentation instrumentation) {
        try {
            Class<?> clz = Class.forName(className);
            if(clz != null) {
                return clz;
            }
        } catch (Exception e) {
            // ignore
        }
        return findClassFromCache(className, instrumentation);
    }

    private static Class<?> findClassFromCache(String className, Instrumentation instrumentation) {
        for(Class<?> clazz: instrumentation.getAllLoadedClasses()) {
            if(clazz.getName().equals(className)) {
                return clazz;
            }
        }
        return null;
    }



}
