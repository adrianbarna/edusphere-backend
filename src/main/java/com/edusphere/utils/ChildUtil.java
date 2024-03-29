package com.edusphere.utils;

import com.edusphere.entities.ChildEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.repositories.ChildRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChildUtil {
    private final ChildRepository childRepository;

    public ChildUtil(ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    public ChildEntity getChildOrThrowException(Integer childId, Integer organizationId) {
        Optional<ChildEntity> childOptional = childRepository.findByIdAndParentOrganizationId(childId,organizationId);
        if(childOptional.isEmpty()){
            throw new ChildNotFoundException(childId);
        }
        return childOptional.get();
    }
}
