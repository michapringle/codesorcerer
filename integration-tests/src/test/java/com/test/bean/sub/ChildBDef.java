package com.test.bean.sub;

import com.codesorcerer.targets.BBBJson;
import com.codesorcerer.targets.BBBTypescript;
import com.codesorcerer.targets.BasicTypescriptMapping;

@BBBTypescript
@BasicTypescriptMapping
@BBBJson
public interface ChildBDef extends ParentX {
    String getThingB();
}
