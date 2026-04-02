package com.camt.reporting.agreement.service;

import com.camt.reporting.agreement.dto.AgreementDto;
import com.camt.reporting.agreement.dto.AgreementVersionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agreements")
@RequiredArgsConstructor
public class AgreementController {

    private final AgreementService agreementService;

    @PostMapping
    public ResponseEntity<AgreementDto.Response> create(@Valid @RequestBody AgreementDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agreementService.createAgreement(request));
    }

    @GetMapping("/{agreementId}")
    public ResponseEntity<AgreementDto.Response> get(@PathVariable String agreementId) {
        return ResponseEntity.ok(agreementService.getAgreement(agreementId));
    }

    @GetMapping
    public ResponseEntity<List<AgreementDto.Response>> getByCorporate(@RequestParam String corporateId) {
        return ResponseEntity.ok(agreementService.getAgreementsByCorporate(corporateId));
    }

    // Versions

    @GetMapping("/{agreementId}/versions")
    public ResponseEntity<List<AgreementVersionDto.Response>> getVersions(@PathVariable String agreementId) {
        return ResponseEntity.ok(agreementService.getVersions(agreementId));
    }

    @GetMapping("/{agreementId}/versions/{versionId}")
    public ResponseEntity<AgreementVersionDto.Response> getVersion(@PathVariable String agreementId,
                                                                    @PathVariable Long versionId) {
        return ResponseEntity.ok(agreementService.getVersion(agreementId, versionId));
    }

    @PostMapping("/{agreementId}/versions")
    public ResponseEntity<AgreementVersionDto.Response> createVersion(
            @PathVariable String agreementId,
            @Valid @RequestBody AgreementVersionDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agreementService.createNewVersion(agreementId, request));
    }

    @PostMapping("/{agreementId}/versions/{versionId}/activate")
    public ResponseEntity<AgreementVersionDto.Response> activate(@PathVariable String agreementId,
                                                                  @PathVariable Long versionId) {
        return ResponseEntity.ok(agreementService.activateVersion(agreementId, versionId));
    }

    @PostMapping("/{agreementId}/versions/{versionId}/initiate-cancellation")
    public ResponseEntity<AgreementVersionDto.Response> initiateCancellation(@PathVariable String agreementId,
                                                                              @PathVariable Long versionId) {
        return ResponseEntity.ok(agreementService.initiateCancellation(agreementId, versionId));
    }

    @PostMapping("/{agreementId}/versions/{versionId}/approve-cancellation")
    public ResponseEntity<AgreementVersionDto.Response> approveCancellation(@PathVariable String agreementId,
                                                                             @PathVariable Long versionId) {
        return ResponseEntity.ok(agreementService.approveCancellation(agreementId, versionId));
    }
}
