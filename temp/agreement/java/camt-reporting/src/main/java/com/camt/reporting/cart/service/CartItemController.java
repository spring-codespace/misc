package com.camt.reporting.cart.service;

import com.camt.reporting.cart.dto.CartItemDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    public ResponseEntity<CartItemDto.Response> create(@Valid @RequestBody CartItemDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemService.createCartItem(request));
    }

    @GetMapping("/{cartItemId}")
    public ResponseEntity<CartItemDto.Response> get(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartItemService.getCartItem(cartItemId));
    }

    @GetMapping("/by-version/{versionId}")
    public ResponseEntity<CartItemDto.Response> getByVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(cartItemService.getCartItemByVersion(versionId));
    }

    @GetMapping
    public ResponseEntity<List<CartItemDto.Response>> getByCorporate(@RequestParam String corporateId) {
        return ResponseEntity.ok(cartItemService.getCartItemsByCorporate(corporateId));
    }

    @PostMapping("/{cartItemId}/approve")
    public ResponseEntity<CartItemDto.Response> approve(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartItemService.approveCartItem(cartItemId));
    }
}
