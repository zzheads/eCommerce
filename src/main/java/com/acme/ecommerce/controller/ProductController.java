package com.acme.ecommerce.controller;

import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.exceptions.ExceedsProductQuantityException;
import com.acme.ecommerce.exceptions.ProductNotFoundException;
import com.acme.ecommerce.exceptions.ShoppingCartNotFoundException;
import com.acme.ecommerce.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;

@Controller
@RequestMapping("/product")
@Scope("request")
public class ProductController {

	@Autowired
	private ShoppingCart sCart;

	final Logger logger = LoggerFactory.getLogger(ProductController.class);
	
	private static final int INITIAL_PAGE = 0;
	private static final int PAGE_SIZE = 5;
	
	@Autowired
	ProductService productService;

	@Autowired
	HttpSession session;
	// /Projects/techdegree-javaweb-ecommerce-master/src/main/resources/static/images/
	@Value("${imagePath:src/main/resources/static/images/}")
	String imagePath;
	
    @RequestMapping("/")
    public String index(Model model, @RequestParam(value = "page", required = false) Integer page) {
    	logger.debug("Getting Product List");
    	logger.debug("Session ID = " + session.getId());
    	
			// Evaluate page. If requested parameter is null or less than 0 (to
			// prevent exception), return initial size. Otherwise, return value of
			// param. decreased by 1.
			int evalPage = (page == null || page < 1) ? INITIAL_PAGE : page - 1;

			Page<Product> products = productService.findAll(new PageRequest(evalPage, PAGE_SIZE));
    	
			model.addAttribute("products", products);
			model.addAttribute("subTotal", CartController.subTotal(sCart));

			return "index";
    }
    
    @RequestMapping(path = "/detail/{id}", method = RequestMethod.GET)
    public String productDetail(@PathVariable long id, Model model, RedirectAttributes redirectAttributes) throws ProductNotFoundException {
    	logger.debug("Details for Product " + id);

    	Product returnProduct = productService.findById(id);
    	if (returnProduct != null) {
    		model.addAttribute("product", returnProduct);
    		ProductPurchase productPurchase = new ProductPurchase();
    		productPurchase.setProduct(returnProduct);
    		productPurchase.setQuantity(1);
    		model.addAttribute("productPurchase", productPurchase);
    	} else {
    		logger.error("Product " + id + " Not Found!");
				//redirectAttributes.addFlashAttribute("flash", new FlashMessage (String.format("Product with id=%d was not found..", id), FlashMessage.Status.FAILURE));
				throw new ProductNotFoundException(String.format("Product with id=%d was not found.", id));
    		//return "redirect:/error";
    	}

        return "product_detail";
    }



    @RequestMapping(path="/{id}/image", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<InputStreamResource> productImage(@PathVariable long id) throws FileNotFoundException {
    	MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
    	
    	logger.debug("Product Image Request for " + id);
    	logger.info("Using imagePath [" + imagePath + "]");
    	
    	Product returnProduct = productService.findById(id);
    	String imageFilePath = null;
    	if (returnProduct != null) {
    		if (!imagePath.endsWith("/")) {
    			imagePath = imagePath + "/";
    		}
    		imageFilePath = imagePath + returnProduct.getFullImageName();
    	}

    	File imageFile = new File(imageFilePath);

			FileInputStream file = new FileInputStream(imageFile);
			InputStreamResource resource = new InputStreamResource(file);

    	return ResponseEntity.ok()
                .contentLength(imageFile.length())
                .contentType(MediaType.parseMediaType(mimeTypesMap.getContentType(imageFile)))
                .body(resource);
    }
    
  @RequestMapping(path = "/about")
  public String aboutCartShop() {
  	logger.warn("Happy Easter! Someone actually clicked on About.");
    return("about");
	}

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

}
