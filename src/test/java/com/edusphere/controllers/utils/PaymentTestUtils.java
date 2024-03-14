package com.edusphere.controllers.utils;

import com.edusphere.entities.ChildEntity;
import com.edusphere.entities.OrganizationEntity;
import com.edusphere.entities.PaymentEntity;
import com.edusphere.enums.PaymentTypeEnum;
import com.edusphere.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PaymentTestUtils {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ChildTestUtils childUtils;

    public PaymentEntity savePayment(OrganizationEntity organizationEntity){
        PaymentEntity paymentEntity = new PaymentEntity();
        ChildEntity childEntity = childUtils.saveAChildInOrganization(organizationEntity);
        paymentEntity.setChild(childEntity);
        paymentEntity.setAmount(1000);
        paymentEntity.setDueDate(new Date());
        paymentEntity.setIssueDate(new Date());
        paymentEntity.setPayType(PaymentTypeEnum.TRANSFER);

        return paymentRepository.save(paymentEntity);
    }

    public PaymentEntity saveAPaidPayment(OrganizationEntity organizationEntity){
        PaymentEntity paymentEntity = new PaymentEntity();
        ChildEntity childEntity = childUtils.saveAChildInOrganization(organizationEntity);
        paymentEntity.setChild(childEntity);
        paymentEntity.setAmount(1000);
        paymentEntity.setDueDate(new Date());
        paymentEntity.setIssueDate(new Date());
        paymentEntity.setPayType(PaymentTypeEnum.TRANSFER);
        paymentEntity.setPaid(true);

        return paymentRepository.save(paymentEntity);
    }
}
