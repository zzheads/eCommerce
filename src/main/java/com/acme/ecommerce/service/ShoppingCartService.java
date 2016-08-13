package com.acme.ecommerce.service;//

import com.acme.ecommerce.controller.FlashMessage;
import com.acme.ecommerce.domain.ShoppingCart;

// eCommerce
// com.acme.ecommerce.service created by zzheads on 13.08.2016.
//
public interface ShoppingCartService {
    public FlashMessage addQuantity (ShoppingCart cart, Long productId, int changeQuantity) throws ProductNotFoundException, ExceedsProductQuantityException;

    public FlashMessage updateQuantity (ShoppingCart cart, Long productId, int newQuantity) throws ProductNotFoundException, ExceedsProductQuantityException, ShoppingCartNotFoundException;
}
