package com.camt.reporting.cart.mapper;

import com.camt.reporting.agreement.entity.CartItem;
import com.camt.reporting.cart.dto.CartItemDto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CartItemMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public CartItemDto.Response toResponse(CartItem cartItem) {
        CartItemDto.Response response = new CartItemDto.Response();
        response.setId(cartItem.getId());
        response.setAgreementVersionId(cartItem.getAgreementVersion().getId());
        response.setCorporateId(cartItem.getCorporateId());
        response.setExpiresAt(cartItem.getExpiresAt() != null
                ? cartItem.getExpiresAt().format(FORMATTER) : null);
        response.setApprovedAt(cartItem.getApprovedAt() != null
                ? cartItem.getApprovedAt().format(FORMATTER) : null);
        response.setExpiredAt(cartItem.getExpiredAt() != null
                ? cartItem.getExpiredAt().format(FORMATTER) : null);
        response.setCreatedAt(cartItem.getCreatedAt() != null
                ? cartItem.getCreatedAt().format(FORMATTER) : null);
        response.setStatus(deriveStatus(cartItem));
        return response;
    }

    public List<CartItemDto.Response> toResponseList(List<CartItem> cartItems) {
        return cartItems.stream().map(this::toResponse).toList();
    }

    private String deriveStatus(CartItem cartItem) {
        if (cartItem.getExpiredAt() != null) return "EXPIRED";
        if (cartItem.getApprovedAt() != null) return "APPROVED";
        return "PENDING";
    }
}
