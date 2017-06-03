package com.techgig.autoheal.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonPropertySourceLoader implements PropertySourceLoader {

    @Override
    public String[] getFileExtensions() {
        return new String[] { "json" };
    }

    @Override
    public PropertySource<?> load(String name, Resource resource, String profile)
            throws IOException {
        if (ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", null)) {
            Processor processor = new Processor(resource, profile);
            Map<String, Object> source = processor.process();
            if (!source.isEmpty()) {
                return new MapPropertySource(name, source);
            }
        }
        return null;
    }

    
    private static class Processor {

        private Resource resource;

        Processor(Resource resource, String profile) {
            this.resource = resource;
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> process() {
            final Map<String, Object> result = new LinkedHashMap<String, Object>();
            Map<String, Object> map;
            try {
                map = new ObjectMapper().readValue(this.resource.getFile(), LinkedHashMap.class);
                result.putAll(getFlattenedMap(map));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

       
        private final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            buildFlattenedMap(result, source, null);
            return result;
        }

        private void buildFlattenedMap(Map<String, Object> result,
                Map<String, Object> source, String path) {
            for (Entry<String, Object> entry : source.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.hasText(path)) {
                    if (key.startsWith("[")) {
                        key = path + key;
                    } else {
                        key = path + "." + key;
                    }
                }
                Object value = entry.getValue();
                if (value instanceof String) {
                    result.put(key, value);

                } else if (value instanceof Map) {
                    // Need a compound key
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) value;
                    buildFlattenedMap(result, map, key);

                } else if (value instanceof Collection) {
                    // Need a compound key
                    @SuppressWarnings("unchecked")
                    Collection<Object> collection = (Collection<Object>) value;
                    int count = 0;
                    for (Object object : collection) {
                        buildFlattenedMap(result,
                                Collections.singletonMap("[" + (count++) + "]", object),
                                key);
                    }

                } else {
                    result.put(key, value == null ? "" : value);
                }
            }
        }
    }
}