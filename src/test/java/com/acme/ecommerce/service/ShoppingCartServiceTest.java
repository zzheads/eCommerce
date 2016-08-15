package com.acme.ecommerce.service;//

import com.acme.ecommerce.controller.FlashMessage;
import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.exceptions.ExceedsProductQuantityException;
import com.acme.ecommerce.exceptions.ProductNotFoundException;
import com.acme.ecommerce.exceptions.ShoppingCartNotFoundException;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// eCommerce
// com.acme.ecommerce.service created by zzheads on 15.08.2016.
//
@RunWith(MockitoJUnitRunner.class)
public class ShoppingCartServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private PurchaseService purchaseService;

    @Mock
    private ShoppingCart cart;

    private static final FlashMessage FLASH_MESSAGE_ADD_SUCCESS = new FlashMessage("Added 1 of TestName to cart.", FlashMessage.Status.SUCCESS);
    private static final FlashMessage FLASH_MESSAGE_UPDATE_SUCCESS = new FlashMessage("Updated TestName to 3.", FlashMessage.Status.SUCCESS);

    @InjectMocks
    private ShoppingCartService shoppingCartService = new ShoppingCartServiceImpl();



    @Test
    public void addQuantityTest () throws Exception {
        Product product = productBuilder();
        when(productService.findById(1L)).thenReturn(product);

        FlashMessage flashMessage = shoppingCartService.addQuantity(cart, 1L, 1);
        assertThat(flashMessage, Matchers.equalTo(FLASH_MESSAGE_ADD_SUCCESS));
        verify(productService, times(1)).findById(1L);
    }

    @Test(expected = ProductNotFoundException.class)
    public void addQuantityProductNotFoundTest () throws Exception {
        Product product = productBuilder();
        when(productService.findById(1L)).thenReturn(null);

        shoppingCartService.addQuantity(cart, 1L, 1);
    }

    @Test(expected = ExceedsProductQuantityException.class)
    public void addQuantityExceedsProductQuantityTest () throws Exception {
        Product product = productBuilder();
        when(productService.findById(1L)).thenReturn(product);
        product.setQuantity(5);

        shoppingCartService.addQuantity(cart, 1L, 6);
    }

    @Test
    public void updateQuantityTest () throws Exception {
        Product product = productBuilder();
        when(productService.findById(1L)).thenReturn(product);
        Purchase purchase = purchaseBuilder(product);
        when(cart.getPurchase()).thenReturn(purchase);

        FlashMessage flashMessage = shoppingCartService.updateQuantity(cart, 1L, 3);
        assertThat(flashMessage, Matchers.equalTo(FLASH_MESSAGE_UPDATE_SUCCESS));
        verify(productService, times(1)).findById(1L);
        verify(purchaseService, times(1)).save(purchase);
    }

    @Test(expected = ProductNotFoundException.class)
    public void updateQuantityProductNotFoundTest () throws Exception {
        Product product = productBuilder();
        when(productService.findById(1L)).thenReturn(null);
        Purchase purchase = purchaseBuilder(product);
        when(cart.getPurchase()).thenReturn(purchase);

        shoppingCartService.updateQuantity(cart, 1L, 3);
    }

    @Test(expected = ExceedsProductQuantityException.class)
    public void updateQuantityExceedsProductQuantityTest () throws Exception {
        Product product = productBuilder();
        when(productService.findById(1L)).thenReturn(product);
        product.setQuantity(5);
        Purchase purchase = purchaseBuilder(product);
        when(cart.getPurchase()).thenReturn(purchase);

        shoppingCartService.updateQuantity(cart, 1L, 6);
    }

    @Test(expected = ShoppingCartNotFoundException.class)
    public void updateQuantityShoppingCartNotFoundTest () throws Exception {
        Product product = productBuilder();
        when(productService.findById(1L)).thenReturn(product);
        Purchase purchase = purchaseBuilder(product);
        when(cart.getPurchase()).thenReturn(null);

        shoppingCartService.updateQuantity(cart, 1L, 3);
    }

    @Test
    public void updateQuantityRemovePurchaseTest () throws Exception {
        Product product = productBuilder();
        when(productService.findById(1L)).thenReturn(product);
        product.setQuantity(5);
        Purchase purchase = purchaseBuilder(product);
        when(cart.getPurchase()).thenReturn(purchase);

        assertThat(cart.getPurchase().getProductPurchases().get(0).getQuantity(),Matchers.equalTo(1));
        verify(purchaseService, times(0)).save(purchase);

        FlashMessage flashMessage = shoppingCartService.addQuantity(cart, 1L, 3);
        assertThat(cart.getPurchase().getProductPurchases().get(0).getQuantity(),Matchers.equalTo(4));
        verify(purchaseService, times(1)).save(purchase);

        flashMessage = shoppingCartService.updateQuantity(cart, 1L, 5);
        assertThat(cart.getPurchase().getProductPurchases().get(0).getQuantity(),Matchers.equalTo(5));
        verify(purchaseService, times(2)).save(purchase);

        // assert that when updated quantity of purchased product to 0 - remove ProductPurchases
        flashMessage = shoppingCartService.updateQuantity(cart, 1L, 0);
        assertThat(cart.getPurchase().getProductPurchases().size(),Matchers.equalTo(0));
        verify(purchaseService, times(3)).save(purchase);
    }



    private Product productBuilder() {
        Product product = new Product();
        product.setId(1L);
        product.setDesc("TestDesc");
        product.setName("TestName");
        product.setPrice(BigDecimal.valueOf(1.99));
        product.setQuantity(3);
        product.setFullImageName("imagename");
        product.setThumbImageName("imagename");
        return product;
    }

    private Purchase purchaseBuilder(Product product) {
        ProductPurchase pp = new ProductPurchase();
        pp.setProductPurchaseId(1L);
        pp.setQuantity(1);
        pp.setProduct(product);
        List<ProductPurchase> ppList = new ArrayList<ProductPurchase>();
        ppList.add(pp);

        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setProductPurchases(ppList);
        return purchase;
    }



}
