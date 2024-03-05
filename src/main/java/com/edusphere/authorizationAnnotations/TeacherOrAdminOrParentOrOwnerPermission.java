package com.edusphere.authorizationAnnotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)

@PreAuthorize("hasAnyAuthority(T(com.edusphere.enums.RolesEnum).OWNER.name()," +
        "T(com.edusphere.enums.RolesEnum).ADMIN.name()," +
        "T(com.edusphere.enums.RolesEnum).TEACHER.name(),"+
        "T(com.edusphere.enums.RolesEnum).PARENT.name())")
public @interface TeacherOrAdminOrParentOrOwnerPermission {

}