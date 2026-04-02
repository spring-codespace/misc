package com.camt.reporting.scheduler;

import com.camt.reporting.cart.service.CartItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartExpiryScheduler {

    private final CartItemService cartItemService;

    /**
     * Runs nightly at 00:05.
     * Finds all CartItems where ExpiresAt has passed, ApprovedAt is null, and ExpiredAt is null.
     * For each: sets CartItem.ExpiredAt and cascades AgreementVersion.Status → EXPIRED.
     */
    @Scheduled(cron = "0 5 0 * * *")
    public void expireOverdueCartItems() {
        log.info("CartExpiryScheduler: starting cart expiry run.");
        try {
            cartItemService.expirePendingCartItems();
            log.info("CartExpiryScheduler: completed successfully.");
        } catch (Exception ex) {
            log.error("CartExpiryScheduler: failed with error: {}", ex.getMessage(), ex);
        }
    }
}
