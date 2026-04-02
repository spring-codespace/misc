package com.camt.reporting.agreement.service;

import com.camt.reporting.agreement.dto.AgreementDto;
import com.camt.reporting.agreement.dto.AgreementVersionDto;
import com.camt.reporting.agreement.entity.Agreement;
import com.camt.reporting.agreement.entity.AgreementContact;
import com.camt.reporting.agreement.entity.AgreementVersion;
import com.camt.reporting.agreement.mapper.AgreementMapper;
import com.camt.reporting.agreement.repository.AgreementRepository;
import com.camt.reporting.agreement.repository.AgreementVersionRepository;
import com.camt.reporting.common.exception.BusinessException;
import com.camt.reporting.common.exception.ResourceNotFoundException;
import com.camt.reporting.reference.entity.AgreementVersionStatus;
import com.camt.reporting.reference.repository.AgreementVersionStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgreementService {

    private static final String STATUS_DRAFT    = "DRAFT";
    private static final String STATUS_ACTIVE   = "ACTIVE";
    private static final String STATUS_SUPERSEDED = "SUPERSEDED";
    private static final String STATUS_DRAFT_CANCEL = "DRAFT_CANCEL";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final AgreementRepository agreementRepository;
    private final AgreementVersionRepository agreementVersionRepository;
    private final AgreementVersionStatusRepository statusRepository;
    private final AgreementMapper mapper;

    // -------------------------------------------------------------------------
    // Agreement
    // -------------------------------------------------------------------------

    @Transactional
    public AgreementDto.Response createAgreement(AgreementDto.CreateRequest request) {
        if (agreementRepository.existsById(request.getId())) {
            throw new BusinessException("Agreement already exists with id: " + request.getId());
        }

        AgreementVersionStatus draftStatus = resolveStatus(STATUS_DRAFT);

        Agreement agreement = Agreement.builder()
                .id(request.getId())
                .name(request.getName())
                .bankId(request.getBankId())
                .corporateId(request.getCorporateId())
                .channel(request.getChannel())
                .createdAt(LocalDateTime.now())
                .build();

        request.getContacts().forEach(c -> {
            AgreementContact contact = AgreementContact.builder()
                    .agreement(agreement)
                    .contactName(c.getContactName())
                    .contactEmail(c.getContactEmail())
                    .contactPhone(c.getContactPhone())
                    .createdAt(LocalDateTime.now())
                    .build();
            agreement.getContacts().add(contact);
        });

        AgreementVersion version = AgreementVersion.builder()
                .agreement(agreement)
                .status(draftStatus)
                .pricingOrderRef(request.getPricingOrderRef())
                .createdAt(LocalDateTime.now())
                .build();
        agreement.getVersions().add(version);

        Agreement saved = agreementRepository.save(agreement);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AgreementDto.Response getAgreement(String agreementId) {
        return mapper.toResponse(findAgreement(agreementId));
    }

    @Transactional(readOnly = true)
    public List<AgreementDto.Response> getAgreementsByCorporate(String corporateId) {
        return agreementRepository.findByCorporateId(corporateId)
                .stream().map(mapper::toResponse).toList();
    }

    // -------------------------------------------------------------------------
    // Version lifecycle
    // -------------------------------------------------------------------------

    @Transactional
    public AgreementVersionDto.Response createNewVersion(String agreementId,
                                                          AgreementVersionDto.CreateRequest request) {
        Agreement agreement = findAgreement(agreementId);

        if (agreementVersionRepository.existsByAgreementIdAndStatusCode(agreementId, STATUS_DRAFT)) {
            throw new BusinessException("A DRAFT version already exists for agreement: " + agreementId);
        }

        AgreementVersion version = AgreementVersion.builder()
                .agreement(agreement)
                .status(resolveStatus(STATUS_DRAFT))
                .pricingOrderRef(request.getPricingOrderRef())
                .createdAt(LocalDateTime.now())
                .build();

        return mapper.toVersionResponse(agreementVersionRepository.save(version));
    }

    @Transactional
    public AgreementVersionDto.Response activateVersion(String agreementId, Long versionId) {
        AgreementVersion version = findVersion(agreementId, versionId);

        if (!STATUS_DRAFT.equals(version.getStatus().getCode())) {
            throw new BusinessException("Only a DRAFT version can be activated.");
        }

        // Supersede existing ACTIVE version if present
        agreementVersionRepository.findActiveByAgreementId(agreementId).ifPresent(active -> {
            active.setStatus(resolveStatus(STATUS_SUPERSEDED));
            active.setSupersededAt(LocalDateTime.now());
            agreementVersionRepository.save(active);
        });

        version.setStatus(resolveStatus(STATUS_ACTIVE));
        version.setActivatedAt(LocalDateTime.now());

        return mapper.toVersionResponse(agreementVersionRepository.save(version));
    }

    @Transactional
    public AgreementVersionDto.Response initiateCancellation(String agreementId, Long versionId) {
        AgreementVersion version = findVersion(agreementId, versionId);

        if (!STATUS_ACTIVE.equals(version.getStatus().getCode())) {
            throw new BusinessException("Only an ACTIVE version can be submitted for cancellation.");
        }

        AgreementVersion cancelDraft = AgreementVersion.builder()
                .agreement(version.getAgreement())
                .status(resolveStatus(STATUS_DRAFT_CANCEL))
                .pricingOrderRef(version.getPricingOrderRef())
                .createdAt(LocalDateTime.now())
                .build();

        return mapper.toVersionResponse(agreementVersionRepository.save(cancelDraft));
    }

    @Transactional
    public AgreementVersionDto.Response approveCancellation(String agreementId, Long versionId) {
        AgreementVersion draftCancel = findVersion(agreementId, versionId);

        if (!STATUS_DRAFT_CANCEL.equals(draftCancel.getStatus().getCode())) {
            throw new BusinessException("Version is not in DRAFT_CANCEL status.");
        }

        // Cancel the currently ACTIVE version
        agreementVersionRepository.findActiveByAgreementId(agreementId).ifPresent(active -> {
            active.setStatus(resolveStatus(STATUS_CANCELLED));
            active.setCancelledAt(LocalDateTime.now());
            agreementVersionRepository.save(active);
        });

        draftCancel.setStatus(resolveStatus(STATUS_CANCELLED));
        draftCancel.setCancelledAt(LocalDateTime.now());

        return mapper.toVersionResponse(agreementVersionRepository.save(draftCancel));
    }

    @Transactional(readOnly = true)
    public List<AgreementVersionDto.Response> getVersions(String agreementId) {
        findAgreement(agreementId);
        return agreementVersionRepository.findByAgreementId(agreementId)
                .stream().map(mapper::toVersionResponse).toList();
    }

    @Transactional(readOnly = true)
    public AgreementVersionDto.Response getVersion(String agreementId, Long versionId) {
        return mapper.toVersionResponse(findVersion(agreementId, versionId));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Agreement findAgreement(String agreementId) {
        return agreementRepository.findById(agreementId)
                .orElseThrow(() -> ResourceNotFoundException.of("Agreement", agreementId));
    }

    private AgreementVersion findVersion(String agreementId, Long versionId) {
        AgreementVersion version = agreementVersionRepository.findById(versionId)
                .orElseThrow(() -> ResourceNotFoundException.of("AgreementVersion", versionId));
        if (!version.getAgreement().getId().equals(agreementId)) {
            throw new ResourceNotFoundException("AgreementVersion " + versionId
                    + " does not belong to agreement " + agreementId);
        }
        return version;
    }

    public AgreementVersion findVersionEntity(Long versionId) {
        return agreementVersionRepository.findById(versionId)
                .orElseThrow(() -> ResourceNotFoundException.of("AgreementVersion", versionId));
    }

    private AgreementVersionStatus resolveStatus(String code) {
        return statusRepository.findById(code)
                .orElseThrow(() -> new BusinessException("Unknown AgreementVersionStatus: " + code));
    }
}
