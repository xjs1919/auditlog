package com.github.xjs.auditlog.aop;

import java.util.Map;

public interface RequestParamExtractor {
    Map<String, Object> extractRequestParams(Object[] args);
}
