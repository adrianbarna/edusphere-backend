package com.edusphere.mappers;

import com.edusphere.entities.InvoiceEntity;
import com.edusphere.exceptions.ChildNotFoundException;
import com.edusphere.repositories.ChildRepository;
import com.edusphere.vos.InvoiceVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class InvoiceMapper {

    private final ChildMapper childMapper;
    private final ChildRepository childRepository;

    public InvoiceMapper(ChildMapper childMapper, ChildRepository childRepository) {
        this.childMapper = childMapper;
        this.childRepository = childRepository;
    }

    public InvoiceEntity toEntity(InvoiceVO invoiceVO, Integer organizationId) {
        if (invoiceVO == null) {
            return null;
        }

        return InvoiceEntity.builder()
                .id(invoiceVO.getId())
                .child(childRepository.findByIdAndParentOrganizationId(invoiceVO.getChildVO().getId(), organizationId)
                        .orElseThrow(() -> new ChildNotFoundException(invoiceVO.getChildVO().getId()))
                )
                .amount(invoiceVO.getAmountWithoutSkipDays())
                .issueDate(invoiceVO.getIssueDate())
                .dueDate(invoiceVO.getDueDate())
                .isPaid(invoiceVO.getIsPaid())
                .payType(invoiceVO.getPayType())
                .build();
    }

    public InvoiceVO toVO(InvoiceEntity invoiceEntity) {
        if (invoiceEntity == null) {
            return null;
        }

        return InvoiceVO.builder()
                .id(invoiceEntity.getId())
                .childVO(childMapper.toVO(invoiceEntity.getChild()))
                .amountWithoutSkipDays(invoiceEntity.getAmount())
                .amountWithSkipDays(invoiceEntity.getAmount())
                .issueDate(invoiceEntity.getIssueDate())
                .dueDate(invoiceEntity.getDueDate())
                .isPaid(invoiceEntity.getIsPaid())
                .payType(invoiceEntity.getPayType())
                .skippedDaysVOList(new ArrayList<>())
                .build();
    }
}
