package com.acme.ecommerce.service;//

import com.acme.ecommerce.controller.FlashMessage;
import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.domain.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// eCommerce
// com.acme.ecommerce.service created by zzheads on 13.08.2016.
//
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ProductService productService;

    @Autowired
    private PurchaseService purchaseService;

    @Override
    public FlashMessage addQuantity(ShoppingCart cart, Long productId, int changeQuantity) throws ProductNotFoundException, ExceedsProductQuantityException {
        // Changes quantity of product (productId) in shopping cart on changeQuantity, returns FlashMessage
        FlashMessage flashMessage;
        Product product = productService.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("Attempt to buy unknown product: "+productId);
        }

        Purchase purchase = cart.getPurchase();
        boolean productAlreadyInCart = false;
        if (purchase == null) {
            purchase = new Purchase();
            cart.setPurchase(purchase);
        }
        else {
            for (ProductPurchase pp : purchase.getProductPurchases()) {
                if (pp.getProduct() != null) {
                    if (pp.getProduct().getId().equals(productId)) {
                        if ((pp.getQuantity()+changeQuantity) > product.getQuantity()) {  // We want buy more than have
                            throw new ExceedsProductQuantityException(FlashMessage.outOfStock(product));
                        }
                        pp.setQuantity(pp.getQuantity() + changeQuantity);
                        productAlreadyInCart = true;
                        break;
                    }
                }
            }
        }

        if (!productAlreadyInCart) {
            if (changeQuantity>product.getQuantity()) {   // We want buy more than have in stock that product
                throw new ExceedsProductQuantityException(FlashMessage.outOfStock(product));
            }

            ProductPurchase newProductPurchase = new ProductPurchase();
            newProductPurchase.setProduct(product);
            newProductPurchase.setQuantity(changeQuantity);
            newProductPurchase.setPurchase(purchase);
            purchase.getProductPurchases().add(newProductPurchase);
        }

        flashMessage = new FlashMessage (String.format("Added %d of %s to cart.", changeQuantity, product.getName()), FlashMessage.Status.SUCCESS);
        cart.setPurchase(purchaseService.save(purchase));
        return flashMessage;
    }

    @Override
    public FlashMessage updateQuantity(ShoppingCart cart, Long productId, int newQuantity) throws ProductNotFoundException, ExceedsProductQuantityException, ShoppingCartNotFoundException {

        FlashMessage flashMessage = new FlashMessage("", FlashMessage.Status.SUCCESS);
        Product updateProduct = productService.findById(productId);
        if (updateProduct != null) {
            Purchase purchase = cart.getPurchase();
            if (purchase == null) {
                throw new ShoppingCartNotFoundException("Unable to find shopping cart for update");
            } else {
                for (ProductPurchase pp : purchase.getProductPurchases()) {
                    if (pp.getProduct() != null) {
                        if (pp.getProduct().getId().equals(productId)) {
                            if (newQuantity>updateProduct.getQuantity()) {
                                throw new ExceedsProductQuantityException(FlashMessage.outOfStock(updateProduct));
                            }

                            if (newQuantity > 0) {
                                pp.setQuantity(newQuantity);
                                flashMessage.setMessage(String.format("Updated %s to %d.", updateProduct.getName(), newQuantity));
                            } else {
                                purchase.getProductPurchases().remove(pp);
                                flashMessage.setMessage(String.format("Removed %s because quantity was set to %d.", updateProduct.getName(), newQuantity));
                            }
                            break;
                        }
                    }
                }
            }
            cart.setPurchase(purchaseService.save(purchase));
        } else {
            throw new ProductNotFoundException("Attempt to update on non-existent product");
        }

        return flashMessage;
    }

}
