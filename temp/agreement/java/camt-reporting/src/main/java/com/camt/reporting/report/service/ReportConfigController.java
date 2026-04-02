package com.camt.reporting.report.service;

import com.camt.reporting.report.dto.ReportDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report-configs")
@RequiredArgsConstructor
public class ReportConfigController {

    private final ReportConfigService reportConfigService;

    @PostMapping
    public ResponseEntity<ReportDto.Response> create(@Valid @RequestBody ReportDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportConfigService.createReportConfig(request));
    }

    @PutMapping("/{configId}")
    public ResponseEntity<ReportDto.Response> update(
            @PathVariable Long configId,
            @Valid @RequestBody ReportDto.UpdateRequest request) {
        return ResponseEntity.ok(reportConfigService.updateReportConfig(configId, request));
    }

    @GetMapping("/{configId}")
    public ResponseEntity<ReportDto.Response> get(@PathVariable Long configId) {
        return ResponseEntity.ok(reportConfigService.getReportConfig(configId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ReportDto.Response>> getAllActive() {
        return ResponseEntity.ok(reportConfigService.getAllActive());
    }

    @GetMapping
    public ResponseEntity<List<ReportDto.Response>> query(
            @RequestParam(required = false) String messageRecipientId,
            @RequestParam(required = false) String reportTypeCode) {
        if (messageRecipientId != null) {
            return ResponseEntity.ok(reportConfigService.getByRecipient(messageRecipientId));
        }
        if (reportTypeCode != null) {
            return ResponseEntity.ok(reportConfigService.getByReportType(reportTypeCode));
        }
        return ResponseEntity.ok(reportConfigService.getAllActive());
    }

    @PostMapping("/{configId}/scopes")
    public ResponseEntity<ReportDto.Response> linkScope(
            @PathVariable Long configId,
            @Valid @RequestBody ReportDto.LinkScopeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportConfigService.linkScope(configId, request));
    }

    @DeleteMapping("/{configId}/scopes/{reportAgreementScopeId}")
    public ResponseEntity<ReportDto.Response> unlinkScope(
            @PathVariable Long configId,
            @PathVariable Long reportAgreementScopeId) {
        return ResponseEntity.ok(reportConfigService.unlinkScope(configId, reportAgreementScopeId));
    }
}
