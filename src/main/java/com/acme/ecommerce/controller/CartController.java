package com.acme.ecommerce.controller;

import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.service.ProductService;
import com.acme.ecommerce.service.PurchaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;


@Controller
@RequestMapping("/cart")
@Scope("request")
public class CartController {
	final Logger logger = LoggerFactory.getLogger(CartController.class);
	
	@Autowired
	PurchaseService purchaseService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ShoppingCart sCart;
	
	@Autowired
	private HttpSession session;
	
    @RequestMapping("")
    public String viewCart(Model model) {
    	logger.debug("Getting Product List");
    	logger.debug("Session ID = " + session.getId());
    	
    	Purchase purchase = sCart.getPurchase();

    	model.addAttribute("purchase", purchase);
    	if (purchase != null) {
    		model.addAttribute("subTotalCart", subTotal(sCart));
    	} else {
    		logger.error("No purchases Found for session ID=" + session.getId());
    		return "redirect:/error";
    	}
        return "cart";
    }

	public static BigDecimal subTotal (ShoppingCart cart) {
		BigDecimal subTotal = BigDecimal.valueOf(0);
		Purchase purchase = cart.getPurchase();
		if (purchase != null) {
			for (ProductPurchase pp : purchase.getProductPurchases()) {
				subTotal = subTotal.add(pp.getProduct().getPrice().multiply(new BigDecimal(pp.getQuantity())));
			}
		}
		return subTotal;
	}


    @RequestMapping(path="/add", method = RequestMethod.POST)
    public RedirectView addToCart(@ModelAttribute(value="productId") long productId, @ModelAttribute(value="quantity") int quantity, RedirectAttributes redirectAttributes) {

    	boolean productAlreadyInCart = false;
    	RedirectView redirect = new RedirectView("/product/");
			redirect.setExposeModelAttributes(false);

    	Product addProduct = productService.findById(productId);

			if (addProduct != null) {
	    	logger.debug("Adding Product: " + addProduct.getId());

    		Purchase purchase = sCart.getPurchase();
    		if (purchase == null) {
    			purchase = new Purchase();
    			sCart.setPurchase(purchase);
    		} else {
    			for (ProductPurchase pp : purchase.getProductPurchases()) {
    				if (pp.getProduct() != null) {
    					if (pp.getProduct().getId().equals(productId)) {
								if ((pp.getQuantity()+quantity) > addProduct.getQuantity()) {  // We want buy more than have
									redirectAttributes.addFlashAttribute("flash", FlashMessage.outOfStock(addProduct));
									return redirect;
								}
    						pp.setQuantity(pp.getQuantity() + quantity);
								redirectAttributes.addFlashAttribute("flash", new FlashMessage (String.format("Added %d of %s to cart.", quantity, addProduct.getName()), FlashMessage.Status.SUCCESS));
    						productAlreadyInCart = true;
    						break;
    					}
    				}
    			}
    		}
    		if (!productAlreadyInCart) {

					if (quantity>addProduct.getQuantity()) {   // We want buy more than have in stock that product
						redirectAttributes.addFlashAttribute("flash", FlashMessage.outOfStock(addProduct));
						return redirect;
					}

    			ProductPurchase newProductPurchase = new ProductPurchase();
    			newProductPurchase.setProduct(addProduct);
    			newProductPurchase.setQuantity(quantity);
    			newProductPurchase.setPurchase(purchase);
        	purchase.getProductPurchases().add(newProductPurchase);
    		}
    		logger.debug("Added " + quantity + " of " + addProduct.getName() + " to cart");
				redirectAttributes.addFlashAttribute("flash", new FlashMessage (String.format("Added %d of %s to cart.", quantity, addProduct.getName()), FlashMessage.Status.SUCCESS));
    		sCart.setPurchase(purchaseService.save(purchase));
			} else {
				logger.error("Attempt to add unknown product: " + productId);
				redirect.setUrl("/error");
			}

    	return redirect;
    }

	@RequestMapping(path="/update", method = RequestMethod.POST)
    public RedirectView updateCart(@ModelAttribute(value="productId") long productId, @ModelAttribute(value="newQuantity") int newQuantity, RedirectAttributes redirectAttributes) {
    	logger.debug("Updating Product: " + productId + " with Quantity: " + newQuantity);
			RedirectView redirect = new RedirectView("/cart");
			redirect.setExposeModelAttributes(false);
    	
    	Product updateProduct = productService.findById(productId);
    	if (updateProduct != null) {

    		Purchase purchase = sCart.getPurchase();
    		if (purchase == null) {
    			logger.error("Unable to find shopping cart for update");
    			redirect.setUrl("/error");
    		} else {
    			for (ProductPurchase pp : purchase.getProductPurchases()) {
    				if (pp.getProduct() != null) {
    					if (pp.getProduct().getId().equals(productId)) {

								if (newQuantity>updateProduct.getQuantity()) {
									redirectAttributes.addFlashAttribute("flash", FlashMessage.outOfStock(updateProduct));
									return redirect;
								}

    						if (newQuantity > 0) {
    							pp.setQuantity(newQuantity);
    							logger.debug("Updated " + updateProduct.getName() + " to " + newQuantity);
									redirectAttributes.addFlashAttribute("flash", new FlashMessage (String.format("Updated %s to %d.", updateProduct.getName(), newQuantity), FlashMessage.Status.SUCCESS));
    						} else {
    							purchase.getProductPurchases().remove(pp);
    							logger.debug("Removed " + updateProduct.getName() + " because quantity was set to " + newQuantity);
									redirectAttributes.addFlashAttribute("flash", new FlashMessage (String.format("Removed %s because quantity was set to %d.", updateProduct.getName(), newQuantity), FlashMessage.Status.SUCCESS));
    						}
    						break;
    					}
    				}
    			}
    		}
    		sCart.setPurchase(purchaseService.save(purchase));
    	} else {
    		logger.error("Attempt to update on non-existent product");
    		redirect.setUrl("/error");
    	}
    	
    	return redirect;
    }
    
    @RequestMapping(path="/remove", method = RequestMethod.POST)
    public RedirectView removeFromCart(@ModelAttribute(value="productId") long productId, RedirectAttributes redirectAttributes) {
    	logger.debug("Removing Product: " + productId);
		RedirectView redirect = new RedirectView("/cart");
		redirect.setExposeModelAttributes(false);
    	
    	Product updateProduct = productService.findById(productId);
    	if (updateProduct != null) {
    		Purchase purchase = sCart.getPurchase();
    		if (purchase != null) {
    			for (ProductPurchase pp : purchase.getProductPurchases()) {
    				if (pp.getProduct() != null) {
    					if (pp.getProduct().getId().equals(productId)) {
    						purchase.getProductPurchases().remove(pp);
   							logger.debug("Removed " + updateProduct.getName());
								redirectAttributes.addFlashAttribute("flash", new FlashMessage (String.format("Removed %s.", updateProduct.getName()), FlashMessage.Status.SUCCESS));
    						break;
    					}
    				}
    			}
    			purchase = purchaseService.save(purchase);
    			sCart.setPurchase(purchase);
    			if (purchase.getProductPurchases().isEmpty()) {
        	    	//if last item in cart redirect to product else return cart
        			redirect.setUrl("/product/");
        		}
    		} else {
    			logger.error("Unable to find shopping cart for update");
    			redirect.setUrl("/error");
    		}
    	} else {
    		logger.error("Attempt to update on non-existent product");
    		redirect.setUrl("/error");
    	}

    	return redirect;
    }
    
    @RequestMapping(path="/empty", method = RequestMethod.POST)
    public RedirectView emptyCart(RedirectAttributes redirectAttributes) {
    	RedirectView redirect = new RedirectView("/product/");
		redirect.setExposeModelAttributes(false);
    	
    	logger.debug("Emptying Cart");
    	Purchase purchase = sCart.getPurchase();
		if (purchase != null) {
			purchase.getProductPurchases().clear();
			redirectAttributes.addFlashAttribute("flash", new FlashMessage ("Cart is empty.", FlashMessage.Status.SUCCESS));
			sCart.setPurchase(purchaseService.save(purchase));
		} else {
			logger.error("Unable to find shopping cart for update");
			redirect.setUrl("/error");
		}
		
    	return redirect;
    }

	public ShoppingCart getsCart() {
		return sCart;
	}

	public void setsCart(ShoppingCart sCart) {
		this.sCart = sCart;
	}

}
