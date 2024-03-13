package com.edusphere.mappers;

import com.edusphere.entities.PaymentEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.vos.PaymentVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PaymentMapper {

    private final ChildMapper childMapper;
    private final ChildRepository childRepository;

    public PaymentMapper(ChildMapper childMapper, ChildRepository childRepository) {
        this.childMapper = childMapper;
        this.childRepository = childRepository;
    }

    public PaymentEntity toEntity(PaymentVO paymentVO, Integer organizationId) {
        if (paymentVO == null) {
            return null;
        }

        return PaymentEntity.builder()
                .id(paymentVO.getId())
                .child(childRepository.findByIdAndParentOrganizationId(paymentVO.getChildVO().getId(), organizationId)
                        .orElseThrow(() -> new ChildNotFoundException(paymentVO.getChildVO().getId()))
                )
                .amount(paymentVO.getAmountWithoutSkipDays())
                .issueDate(paymentVO.getIssueDate())
                .dueDate(paymentVO.getDueDate())
                .isPaid(paymentVO.getIsPaid())
                .payType(paymentVO.getPayType())
                .build();
    }

    public PaymentVO toVO(PaymentEntity paymentEntity) {
        if (paymentEntity == null) {
            return null;
        }

        return PaymentVO.builder()
                .id(paymentEntity.getId())
                .childVO(childMapper.toVO(paymentEntity.getChild()))
                .amountWithoutSkipDays(paymentEntity.getAmount())
                .amountWithSkipDays(paymentEntity.getAmount())
                .issueDate(paymentEntity.getIssueDate())
                .dueDate(paymentEntity.getDueDate())
                .isPaid(paymentEntity.getIsPaid())
                .payType(paymentEntity.getPayType())
                .skippedDaysVOList(new ArrayList<>())
                .build();
    }
}
