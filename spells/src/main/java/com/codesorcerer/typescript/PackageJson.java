package com.codesorcerer.typescript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class PackageJson {
    public String name;
    public String version;
    public Map<String, String> dependencies = Maps.newHashMap();
    public Map<String, String> devDependencies = Maps.newHashMap();
    public Map<String, String> peerDependencies = Maps.newHashMap();

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }
}
