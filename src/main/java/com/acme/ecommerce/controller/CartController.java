package com.acme.ecommerce.controller;

import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.exceptions.ExceedsProductQuantityException;
import com.acme.ecommerce.exceptions.ProductNotFoundException;
import com.acme.ecommerce.exceptions.ShoppingCartNotFoundException;
import com.acme.ecommerce.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
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
	private ShoppingCartService shoppingCartService;
	
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
    public RedirectView addToCart(@ModelAttribute(value="productId") long productId, @ModelAttribute(value="quantity") int quantity, RedirectAttributes redirectAttributes)	throws ProductNotFoundException {

    	RedirectView redirect = new RedirectView("/product/");
			redirect.setExposeModelAttributes(false);

			try {
				 redirectAttributes.addFlashAttribute("flash", shoppingCartService.addQuantity(sCart, productId, quantity));
			} catch (ExceedsProductQuantityException e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute("flash", new FlashMessage(e.getMessage(),	FlashMessage.Status.FAILURE));
			}

    	return redirect;
    }

	@RequestMapping(path="/update", method = RequestMethod.POST)
	public RedirectView updateCart(@ModelAttribute(value="productId") long productId, @ModelAttribute(value="newQuantity") int newQuantity, RedirectAttributes redirectAttributes) throws ProductNotFoundException {
		logger.debug("Updating Product: " + productId + " with Quantity: " + newQuantity);
		RedirectView redirect = new RedirectView("/cart");
		redirect.setExposeModelAttributes(false);

		try {
			redirectAttributes.addFlashAttribute("flash", shoppingCartService.updateQuantity(sCart, productId, newQuantity));
		} catch (ShoppingCartNotFoundException e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("flash", new FlashMessage(e.getMessage(),	FlashMessage.Status.FAILURE));
			redirect.setUrl("/error");
		} catch (ExceedsProductQuantityException e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("flash", new FlashMessage(e.getMessage(),	FlashMessage.Status.FAILURE));
		}

		return redirect;
    }
    
    @RequestMapping(path="/remove", method = RequestMethod.POST)
    public RedirectView removeFromCart(@ModelAttribute(value="productId") long productId, RedirectAttributes redirectAttributes) throws ProductNotFoundException {
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
    		throw new ProductNotFoundException("Attempt to update on non-existent product");
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

	@ExceptionHandler(ProductNotFoundException.class)
	public ModelAndView handleProductNotFoundException(ProductNotFoundException ex) {

		ModelAndView model = new ModelAndView("error");
		model.addObject("exception", ex);

		return model;
	}

	public ShoppingCart getsCart() {
		return sCart;
	}

	public void setsCart(ShoppingCart sCart) {
		this.sCart = sCart;
	}

}
