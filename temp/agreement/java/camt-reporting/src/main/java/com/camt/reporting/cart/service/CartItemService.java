package com.camt.reporting.cart.service;

import com.camt.reporting.agreement.entity.AgreementVersion;
import com.camt.reporting.agreement.entity.CartItem;
import com.camt.reporting.agreement.repository.AgreementVersionRepository;
import com.camt.reporting.agreement.service.AgreementService;
import com.camt.reporting.cart.dto.CartItemDto;
import com.camt.reporting.cart.mapper.CartItemMapper;
import com.camt.reporting.cart.repository.CartItemRepository;
import com.camt.reporting.common.exception.BusinessException;
import com.camt.reporting.common.exception.ResourceNotFoundException;
import com.camt.reporting.reference.entity.AgreementVersionStatus;
import com.camt.reporting.reference.repository.AgreementVersionStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemService {

    private static final int CART_EXPIRY_DAYS = 30;
    private static final String STATUS_EXPIRED = "EXPIRED";

    private final CartItemRepository cartItemRepository;
    private final AgreementVersionRepository agreementVersionRepository;
    private final AgreementVersionStatusRepository versionStatusRepository;
    private final AgreementService agreementService;
    private final CartItemMapper mapper;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Transactional
    public CartItemDto.Response createCartItem(CartItemDto.CreateRequest request) {
        AgreementVersion version = agreementService.findVersionEntity(request.getAgreementVersionId());

        if (cartItemRepository.findByAgreementVersionId(version.getId()).isPresent()) {
            throw new BusinessException("A cart item already exists for version: " + version.getId());
        }

        String versionStatus = version.getStatus().getCode();
        if (!"DRAFT".equals(versionStatus) && !"DRAFT_CANCEL".equals(versionStatus)) {
            throw new BusinessException("Cart items can only be created for DRAFT or DRAFT_CANCEL versions.");
        }

        CartItem cartItem = CartItem.builder()
                .agreementVersion(version)
                .corporateId(request.getCorporateId())
                .expiresAt(LocalDateTime.now().plusDays(CART_EXPIRY_DAYS))
                .createdAt(LocalDateTime.now())
                .build();

        return mapper.toResponse(cartItemRepository.save(cartItem));
    }

    // -------------------------------------------------------------------------
    // Approve
    // -------------------------------------------------------------------------

    @Transactional
    public CartItemDto.Response approveCartItem(Long cartItemId) {
        CartItem cartItem = findCartItem(cartItemId);

        if (cartItem.getApprovedAt() != null) {
            throw new BusinessException("Cart item is already approved.");
        }
        if (cartItem.getExpiredAt() != null) {
            throw new BusinessException("Cart item has expired and cannot be approved.");
        }
        if (LocalDateTime.now().isAfter(cartItem.getExpiresAt())) {
            throw new BusinessException("Cart item has passed its expiry time.");
        }

        cartItem.setApprovedAt(LocalDateTime.now());
        return mapper.toResponse(cartItemRepository.save(cartItem));
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public CartItemDto.Response getCartItem(Long cartItemId) {
        return mapper.toResponse(findCartItem(cartItemId));
    }

    @Transactional(readOnly = true)
    public CartItemDto.Response getCartItemByVersion(Long versionId) {
        return mapper.toResponse(
                cartItemRepository.findByAgreementVersionId(versionId)
                        .orElseThrow(() -> ResourceNotFoundException.of("CartItem for version", versionId)));
    }

    @Transactional(readOnly = true)
    public List<CartItemDto.Response> getCartItemsByCorporate(String corporateId) {
        return mapper.toResponseList(cartItemRepository.findByCorporateId(corporateId));
    }

    // -------------------------------------------------------------------------
    // Expiry — called by scheduler
    // -------------------------------------------------------------------------

    @Transactional
    public void expirePendingCartItems() {
        LocalDateTime now = LocalDateTime.now();
        List<CartItem> expired = cartItemRepository.findPendingExpired(now);

        if (expired.isEmpty()) {
            log.debug("Cart expiry job: no items to expire.");
            return;
        }

        AgreementVersionStatus expiredStatus = versionStatusRepository.findById(STATUS_EXPIRED)
                .orElseThrow(() -> new BusinessException("Unknown status: " + STATUS_EXPIRED));

        for (CartItem cartItem : expired) {
            cartItem.setExpiredAt(now);
            cartItemRepository.save(cartItem);

            AgreementVersion version = cartItem.getAgreementVersion();
            version.setStatus(expiredStatus);
            version.setExpiredAt(now);
            agreementVersionRepository.save(version);

            log.info("Expired cart item id={} for agreementVersion id={}", cartItem.getId(), version.getId());
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private CartItem findCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> ResourceNotFoundException.of("CartItem", cartItemId));
    }
}
