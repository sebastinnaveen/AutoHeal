package com.techgig.autoheal.services;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;




public interface ConfigBean {
    String getUrl();
    int getPort();
    Map getAMap();
    List getAList();
}
