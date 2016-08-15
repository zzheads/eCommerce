package com.acme.ecommerce.controller;

import com.acme.ecommerce.Application;
import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.domain.ProductPurchase;
import com.acme.ecommerce.domain.Purchase;
import com.acme.ecommerce.domain.ShoppingCart;
import com.acme.ecommerce.exceptions.ExceedsProductQuantityException;
import com.acme.ecommerce.exceptions.ProductNotFoundException;
import com.acme.ecommerce.exceptions.ShoppingCartNotFoundException;
import com.acme.ecommerce.service.ProductService;
import com.acme.ecommerce.service.PurchaseService;
import com.acme.ecommerce.service.ShoppingCartService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.ServletContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class CartControllerTest {

	final String BASE_URL = "http://localhost:8080/";

	@Mock
	private MockHttpSession session;

	@Mock
	private ProductService productService;
	@Mock
	private PurchaseService purchaseService;
	@Mock
	private ShoppingCartService shoppingCartService;
	@Mock
	private ShoppingCart sCart;
	@Mock
	private RedirectAttributes redirectAttributes = Mockito.mock(RedirectAttributes.class);
	@InjectMocks
	private CartController cartController;

	private MockMvc mockMvc;

	static {
		System.setProperty("properties.home", "properties");
	}

	@Before
	public void setup() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setPrefix("/WEB-INF/");
		viewResolver.setSuffix(".html");

		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(cartController).setViewResolvers(viewResolver).build();
	}

	@Test
	public void viewCartTest() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);

		when(sCart.getPurchase()).thenReturn(purchase);
		mockMvc.perform(MockMvcRequestBuilders.get("/cart")).andDo(print())
				.andExpect(status().isOk())
				.andExpect(view().name("cart"));
	}

	@Test
	public void viewCartNoPurchasesTest() throws Exception {

		when(sCart.getPurchase()).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.get("/cart")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error"));
	}

	@Test
	public void addToCartTest() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/add").param("quantity", "1").param("productId", "1"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/product/"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addExceedsProductQuantityTest() throws Exception {
		Product product = productBuilder();
		final int PRODUCT_QUANTITY = 5;
		product.setQuantity(PRODUCT_QUANTITY);

		when(productService.findById(1L)).thenReturn(product);
		when(shoppingCartService.addQuantity(sCart, 1L, PRODUCT_QUANTITY+1)).thenThrow(ExceedsProductQuantityException.class);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/add")
			.param("quantity", String.valueOf(PRODUCT_QUANTITY+1))
			.param("productId", "1"))
			.andDo(print())
			.andExpect(flash().attribute("flash", Matchers.instanceOf(FlashMessage.class)))
			.andExpect(flash().attribute("flash", Matchers.hasProperty("status", Matchers.equalTo(FlashMessage.Status.FAILURE))));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void addProductNotFoundTest() throws Exception {

		when(shoppingCartService.addQuantity(sCart, 2L, 1)).thenThrow(ProductNotFoundException.class);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/add")
			.param("quantity", "1")
			.param("productId", "2"))
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/error"))
			.andExpect(flash().attribute("exception", Matchers.instanceOf(ProductNotFoundException.class)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addShoppingCartNotFoundTest() throws Exception {

		when(shoppingCartService.addQuantity(sCart, 2L, 1)).thenThrow(ShoppingCartNotFoundException.class);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/add")
			.param("quantity", "1")
			.param("productId", "2"))
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/error"))
			.andExpect(flash().attribute("exception", Matchers.instanceOf(ShoppingCartNotFoundException.class)));
	}


	@Test
	public void updateCartTest() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);

		when(sCart.getPurchase()).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/update").param("newQuantity", "2").param("productId", "1"))
				.andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart"));
	}

	@Test
	public void removeFromCartTest() throws Exception {
		Product product = productBuilder();

		Product product2 = productBuilder();
		product2.setId(2L);

		when(productService.findById(1L)).thenReturn(product);

		ProductPurchase pp = new ProductPurchase();
		pp.setProductPurchaseId(1L);
		pp.setQuantity(1);
		pp.setProduct(product);

		ProductPurchase pp2 = new ProductPurchase();
		pp2.setProductPurchaseId(2L);
		pp2.setQuantity(2);
		pp2.setProduct(product2);

		List<ProductPurchase> ppList = new ArrayList<ProductPurchase>();
		ppList.add(pp);
		ppList.add(pp2);

		Purchase purchase = new Purchase();
		purchase.setId(1L);
		purchase.setProductPurchases(ppList);

		when(sCart.getPurchase()).thenReturn(purchase);

		when(purchaseService.save(purchase)).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove").param("productId", "1")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/cart"));
	}

	@Test
	public void removeUnknownCartTest() throws Exception {
		when(productService.findById(1L)).thenReturn(null);

		try {
			mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove").param("productId", "1"))
				.andDo(print())
				.andExpect(redirectedUrl("/error"));
		} catch (ProductNotFoundException ex) {
			assertThat(ex.getCode()).isEqualTo(404);
		}
	}

	@Test
	public void removeLastFromCartTest() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);

		when(sCart.getPurchase()).thenReturn(purchase);

		when(purchaseService.save(purchase)).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/remove").param("productId", "1")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/product/"));
	}

	@Test
	public void emptyCartTest() throws Exception {
		Product product = productBuilder();

		Product product2 = productBuilder();
		product2.setId(2L);

		when(productService.findById(1L)).thenReturn(product);

		ProductPurchase pp = new ProductPurchase();
		pp.setProductPurchaseId(1L);
		pp.setQuantity(1);
		pp.setProduct(product);

		ProductPurchase pp2 = new ProductPurchase();
		pp2.setProductPurchaseId(2L);
		pp2.setQuantity(2);
		pp2.setProduct(product2);

		List<ProductPurchase> ppList = new ArrayList<ProductPurchase>();
		ppList.add(pp);
		ppList.add(pp2);

		Purchase purchase = new Purchase();
		purchase.setId(1L);
		purchase.setProductPurchases(ppList);

		when(sCart.getPurchase()).thenReturn(purchase);

		when(purchaseService.save(purchase)).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/empty")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/product/"));
	}

	@Test
	public void emptyInvalidCartTest() throws Exception {

		when(sCart.getPurchase()).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.post("/cart/empty")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error"));
	}

	private Product productBuilder() {
		Product product = new Product();
		product.setId(1L);
		product.setDesc("TestDesc");
		product.setName("TestName");
		product.setPrice(new BigDecimal(1.99));
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
