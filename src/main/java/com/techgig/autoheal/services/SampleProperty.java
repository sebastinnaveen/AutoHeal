package com.techgig.autoheal.services;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "prefix")
@Component
public class SampleProperty {
    private String stringProp1;
    public String getStringProp1() {
		return stringProp1;
	}
	public void setStringProp1(String stringProp1) {
		this.stringProp1 = stringProp1;
	}
	private String stringProp2;
    
    private Integer intProp1;
    private List<String> listProp;
    private Map<String, String> mapProp;
    
}