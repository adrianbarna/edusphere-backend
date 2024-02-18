package com.edusphere.authorizationAnnotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.edusphere.enums.RolesEnum).OWNER.name())")
public @interface OwnerPermission {

}