package com.test.bean.sub;

import com.codesorcerer.targets.BBBTypescript;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@clazz"
)
@BBBTypescript
public interface ParentXY {
    String getXY();
}
