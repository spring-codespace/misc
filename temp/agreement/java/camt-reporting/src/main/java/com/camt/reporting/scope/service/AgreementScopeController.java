package com.camt.reporting.scope.service;

import com.camt.reporting.scope.dto.ScopeDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AgreementScopeController {

    private final AgreementScopeService scopeService;

    @GetMapping("/versions/{versionId}/scopes")
    public ResponseEntity<List<ScopeDto.ScopeResponse>> getByVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(scopeService.getScopesByVersion(versionId));
    }

    @PostMapping("/versions/{versionId}/scopes")
    public ResponseEntity<ScopeDto.ScopeResponse> addScope(
            @PathVariable Long versionId,
            @Valid @RequestBody ScopeDto.CreateScopeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scopeService.addScope(versionId, request));
    }

    @GetMapping("/scopes/{scopeId}")
    public ResponseEntity<ScopeDto.ScopeResponse> getScope(@PathVariable Long scopeId) {
        return ResponseEntity.ok(scopeService.getScope(scopeId));
    }

    @PostMapping("/scopes/{scopeId}/cancel")
    public ResponseEntity<ScopeDto.ScopeResponse> cancelScope(@PathVariable Long scopeId) {
        return ResponseEntity.ok(scopeService.cancelScope(scopeId));
    }
}
