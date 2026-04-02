package com.camt.reporting.agreement.mapper;

import com.camt.reporting.agreement.dto.AgreementDto;
import com.camt.reporting.agreement.dto.AgreementVersionDto;
import com.camt.reporting.agreement.entity.Agreement;
import com.camt.reporting.agreement.entity.AgreementContact;
import com.camt.reporting.agreement.entity.AgreementVersion;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AgreementMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AgreementDto.Response toResponse(Agreement agreement) {
        AgreementDto.Response response = new AgreementDto.Response();
        response.setId(agreement.getId());
        response.setName(agreement.getName());
        response.setBankId(agreement.getBankId());
        response.setCorporateId(agreement.getCorporateId());
        response.setChannel(agreement.getChannel());
        response.setCreatedAt(agreement.getCreatedAt() != null
                ? agreement.getCreatedAt().format(FORMATTER) : null);
        response.setContacts(toContactResponseList(agreement.getContacts()));
        return response;
    }

    public AgreementDto.ContactResponse toContactResponse(AgreementContact contact) {
        AgreementDto.ContactResponse response = new AgreementDto.ContactResponse();
        response.setId(contact.getId());
        response.setContactName(contact.getContactName());
        response.setContactEmail(contact.getContactEmail());
        response.setContactPhone(contact.getContactPhone());
        response.setCreatedAt(contact.getCreatedAt() != null
                ? contact.getCreatedAt().format(FORMATTER) : null);
        return response;
    }

    public List<AgreementDto.ContactResponse> toContactResponseList(List<AgreementContact> contacts) {
        return contacts.stream().map(this::toContactResponse).toList();
    }

    public AgreementVersionDto.Response toVersionResponse(AgreementVersion version) {
        AgreementVersionDto.Response response = new AgreementVersionDto.Response();
        response.setId(version.getId());
        response.setAgreementId(version.getAgreement().getId());
        response.setStatus(version.getStatus().getCode());
        response.setPricingOrderRef(version.getPricingOrderRef());
        response.setCreatedAt(version.getCreatedAt() != null
                ? version.getCreatedAt().format(FORMATTER) : null);
        response.setActivatedAt(version.getActivatedAt() != null
                ? version.getActivatedAt().format(FORMATTER) : null);
        response.setSupersededAt(version.getSupersededAt() != null
                ? version.getSupersededAt().format(FORMATTER) : null);
        response.setCancelledAt(version.getCancelledAt() != null
                ? version.getCancelledAt().format(FORMATTER) : null);
        response.setExpiredAt(version.getExpiredAt() != null
                ? version.getExpiredAt().format(FORMATTER) : null);
        return response;
    }

    public List<AgreementVersionDto.Response> toVersionResponseList(List<AgreementVersion> versions) {
        return versions.stream().map(this::toVersionResponse).toList();
    }
}
