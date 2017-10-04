package com.test.bean.sub;

import com.codesorcerer.targets.BBBTypescript;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@BBBTypescript
public interface ParentXX {
    String getXX();
}
