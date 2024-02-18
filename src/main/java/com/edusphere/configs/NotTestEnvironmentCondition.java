package com.edusphere.configs;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NotTestEnvironmentCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context,  AnnotatedTypeMetadata metadata) {
        // Check if the "test" profile is active
        return !context.getEnvironment().matchesProfiles("test");
    }
}