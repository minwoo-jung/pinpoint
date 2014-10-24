package com.nhn.pinpoint.profiler.modifier.spring.beans.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;

public class TargetBeanFilter {
    private static final int CACHE_SIZE = 1024;
    private static final int CACHE_CONCURRENCY_LEVEL = Runtime.getRuntime().availableProcessors() * 2;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final List<Pattern> targetNamePatterns;
    private final List<Pattern> targetClassPatterns;
    private final List<String> targetAnnotationNames;
    private final ConcurrentMap<ClassLoader, List<Class<? extends Annotation>>> targetAnnotationMap = new ConcurrentHashMap<ClassLoader, List<Class<? extends Annotation>>>();
    
    private final Cache<Class<?>, Boolean> transformed = CacheBuilder.newBuilder().concurrencyLevel(CACHE_CONCURRENCY_LEVEL).maximumSize(CACHE_SIZE).weakKeys().build();
    private final Cache<Class<?>, Boolean> rejected = CacheBuilder.newBuilder().concurrencyLevel(CACHE_CONCURRENCY_LEVEL).maximumSize(CACHE_SIZE).weakKeys().build();
    
    public static TargetBeanFilter of(ProfilerConfig config) {
        List<String> targetNamePatternStrings = split(config.getSpringBeansNamePatterns());
		List<Pattern> beanNamePatterns = compilePattern(targetNamePatternStrings);

		List<String> targetClassPatternStrings = split(config.getSpringBeansClassPatterns());
		List<Pattern> beanClassPatterns = compilePattern(targetClassPatternStrings);

        List<String> targetAnnotationNames = split(config.getSpringBeansAnnotations());

        return new TargetBeanFilter(beanNamePatterns, beanClassPatterns, targetAnnotationNames);
    }

	private static List<Pattern> compilePattern(List<String> patternStrings) {
		if (patternStrings == null || patternStrings.isEmpty()) {
			return null;
		}
		List<Pattern> beanNamePatterns = new ArrayList<Pattern>(patternStrings.size());
		for (String patternString : patternStrings) {
			Pattern pattern = Pattern.compile(patternString);
			beanNamePatterns.add(pattern);
		}
		return beanNamePatterns;
	}

	private TargetBeanFilter(List<Pattern> targetNamePatterns, List<Pattern> targetClassPatterns, List<String> targetAnnotationNames) {
        this.targetNamePatterns = targetNamePatterns;
        this.targetClassPatterns = targetClassPatterns;
        this.targetAnnotationNames = targetAnnotationNames;
    }

    public boolean isTarget(String beanName, Class<?> clazz) {
        if (transformed.getIfPresent(clazz) == Boolean.TRUE) {
            return false;
        }
        
        return isTarget(beanName) || isTarget(clazz);
    }

    private boolean isTarget(String beanName) {
        if (targetNamePatterns != null) {
            for (Pattern pattern : targetNamePatterns) {
                if (pattern.matcher(beanName).matches()) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean isTarget(Class<?> clazz) {
        if (rejected.getIfPresent(clazz) == Boolean.TRUE) {
            return false;
        }

        if (targetAnnotationNames != null) {
            List<Class<? extends Annotation>> targetAnnotations = getTargetAnnotations(clazz.getClassLoader());
            
            for (Class<? extends Annotation> a : targetAnnotations) {
                if (clazz.isAnnotationPresent(a)) {
                    return true;
                }
            }
            
            for (Annotation a : clazz.getAnnotations()) {
                for (Class<? extends Annotation> ac : targetAnnotations) {
                    if (a.annotationType().isAnnotationPresent(ac)) {
                        return true;
                    }
                } 
            }
        }

        if (targetClassPatterns != null) {
            String className = clazz.getName();

            for (Pattern pattern : targetClassPatterns) {
                if (pattern.matcher(className).matches()) {
                    return true;
                }
            }
        }

        rejected.put(clazz, Boolean.TRUE);
        return false;
    }

    public void addTransformed(Class<?> clazz) {
        transformed.put(clazz, Boolean.TRUE);
    }

    private List<Class<? extends Annotation>> getTargetAnnotations(ClassLoader loader) {
        List<Class<? extends Annotation>> targetAnnotations = targetAnnotationMap.get(loader);
        
        if (targetAnnotations == null) {
            targetAnnotations = loadTargetAnnotations(loader);
            targetAnnotationMap.put(loader, targetAnnotations);
        }
        
        return targetAnnotations;
    }

    private List<Class<? extends Annotation>> loadTargetAnnotations(ClassLoader loader) {
        if (targetAnnotationNames.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Class<? extends Annotation>> targetAnnotationClasses = new ArrayList<Class<? extends Annotation>>(targetAnnotationNames.size());
        for (String targetAnnotationName : targetAnnotationNames) {
            try {
                Class<?> clazz = loader.loadClass(targetAnnotationName);
                Class<? extends Annotation> ac = clazz.asSubclass(Annotation.class);
                targetAnnotationClasses.add(ac);
            } catch (ClassNotFoundException e) {
                logger.warn("Cannot find Spring beans profile target annotation class: {}. This configuration will be ignored.", targetAnnotationName, e);
            } catch (ClassCastException e) {
                logger.warn("Given Spring beans profile target annotation class is not subclass of Annotation: {}. This configuration will be ignored.", targetAnnotationName, e);
            }
        }
        
        return targetAnnotationClasses;
    }
    
    private static List<String> split(String values) {
        if (values == null) {
            return Collections.emptyList();
        }
        
        String[] tokens = values.split(",");
        List<String> result = new ArrayList<String>(tokens.length); 

        for (String token : tokens) {
            String trimmed = token.trim();
            
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        
        return result;
    }
}
