package com.acme.ecommerce.controller;

import com.acme.ecommerce.domain.Product;

public class FlashMessage {
    private String message;
    private Status status;

    public FlashMessage(String message, Status status) {
        this.message = message;
        this.status = status;
    }

    public static FlashMessage outOfStock (Product product) {
        String text = "";
        if (product.getQuantity()>0) {
            text = String.format("You can't buy quantity of product more than in stock. We have only %d items of %s.", product.getQuantity(), product.getName());
        } else {
            text = String.format("You can't buy product out of stock. We dont have %s in stock.", product.getName());
        }
        return new FlashMessage(text, Status.FAILURE);
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }

    public static enum Status {
        SUCCESS, FAILURE
    }

}
