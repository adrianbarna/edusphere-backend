package com.edusphere.mappers;

import com.edusphere.entities.SkippedDaysEntity;
import com.edusphere.vos.SkippedDaysVO;
import org.springframework.stereotype.Component;

@Component
public class SkippedDaysMapper {


    public SkippedDaysVO toVO(SkippedDaysEntity skippedDaysEntity) {
        if (skippedDaysEntity == null) {
            return null;
        }

        return SkippedDaysVO.builder()
                .id(skippedDaysEntity.getId())
                .childId(skippedDaysEntity.getChild().getId())
                .startDate(skippedDaysEntity.getStartDate())
                .endDate(skippedDaysEntity.getEndDate())
                .proccessed(skippedDaysEntity.getProccessed())
                .build();
    }
}
