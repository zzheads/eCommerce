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
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;

import static com.acme.ecommerce.controller.web.ReferrerInterceptor.*;


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
    public RedirectView addToCart(@ModelAttribute(value="productId") long productId, @ModelAttribute(value="quantity") int quantity, RedirectAttributes redirectAttributes) {

    	RedirectView redirect = new RedirectView("/product/");
			redirect.setExposeModelAttributes(false);

			FlashMessage flashMessage = shoppingCartService.addQuantity(sCart, productId, quantity);
			redirectAttributes.addFlashAttribute("flash", flashMessage);

    	return redirect;
    }

	@RequestMapping(path="/update", method = RequestMethod.POST)
	public RedirectView updateCart(@ModelAttribute(value="productId") long productId, @ModelAttribute(value="newQuantity") int newQuantity, RedirectAttributes redirectAttributes) {

		logger.debug("Updating Product: " + productId + " with Quantity: " + newQuantity);
		RedirectView redirect = new RedirectView("/cart");
		redirect.setExposeModelAttributes(false);

		redirectAttributes.addFlashAttribute("flash", shoppingCartService.updateQuantity(sCart, productId, newQuantity));

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
					throw new ShoppingCartNotFoundException("Unable to find shopping cart for update");
    		}
    	} else {
    		throw new ProductNotFoundException("Attempt to remove non-existent product");
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
			throw new ShoppingCartNotFoundException("Unable to find shopping cart for update");
		}
		
    	return redirect;
    }

//	@ExceptionHandler(ProductNotFoundException.class)
//	public ModelAndView handleProductNotFoundException(ProductNotFoundException ex) {
//
//		ModelAndView model = new ModelAndView("error");
//		model.addObject("exception", ex);
//
//		return model;
//	}


	@ExceptionHandler(ExceedsProductQuantityException.class)
	public String handleExceedsProductQuantityException(Model model, HttpServletRequest req, Exception ex) {

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(req);
		if(flashMap != null) {
			flashMap.put("flash",new FlashMessage(ex.getMessage(), FlashMessage.Status.FAILURE));
		}

		return "redirect:" + req.getHeader("referer");
	}

	@ExceptionHandler({ShoppingCartNotFoundException.class, ProductNotFoundException.class})
	public String handleNotFoundException(Model model, HttpServletRequest req, Exception ex) {

		FlashMap flashMap = RequestContextUtils.getOutputFlashMap(req);
		if(flashMap != null) {
			flashMap.put("exception", ex);
		}

		return "redirect:/error";
	}



	public ShoppingCart getsCart() {
		return sCart;
	}

	public void setsCart(ShoppingCart sCart) {
		this.sCart = sCart;
	}

}
