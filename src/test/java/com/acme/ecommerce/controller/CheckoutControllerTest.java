package com.acme.ecommerce.controller;

import com.acme.ecommerce.Application;
import com.acme.ecommerce.domain.*;
import com.acme.ecommerce.service.ProductService;
import com.acme.ecommerce.service.PurchaseService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class CheckoutControllerTest {

	final String BASE_URL = "http://localhost:8080/";

	@Mock
	private MockHttpSession session;

	@Mock
	private ProductService productService;
	@Mock
	private PurchaseService purchaseService;
	@Mock
	private ShoppingCart sCart;
	@InjectMocks
	private CheckoutController checkoutController;

	private MockMvc mockMvc;

	static {
		System.setProperty("properties.home", "properties");
	}

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(checkoutController).build();
	}

	@Test
	public void couponTest() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);

		when(sCart.getPurchase()).thenReturn(purchase);
		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/coupon")).andDo(print()).andExpect(status().isOk())
				.andExpect(view().name("checkout_1"));
	}

	@Test
	public void noCartCouponTest() throws Exception {
		when(sCart.getPurchase()).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/coupon")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error"));
	}

	// Added Test for different couponCode input: 3 Valid, 2 not valid

	@Test
	public void postCouponTest() throws Exception {
		MockHttpServletRequestBuilder request_Empty = MockMvcRequestBuilders.post("/checkout/coupon")
			.param("couponCode","");

		MockHttpServletRequestBuilder request_4Chars = MockMvcRequestBuilders.post("/checkout/coupon")
			.param("couponCode","abcd");

		MockHttpServletRequestBuilder request_5Chars = MockMvcRequestBuilders.post("/checkout/coupon")
			.param("couponCode","abcde");

		MockHttpServletRequestBuilder request_9Chars = MockMvcRequestBuilders.post("/checkout/coupon")
			.param("couponCode","abcde4567");

		MockHttpServletRequestBuilder request_15Chars = MockMvcRequestBuilders.post("/checkout/coupon")
			.param("couponCode","abcde36789abcde");


		// Valid requests: Empty, 5Chars, 9Chars
		mockMvc.perform(request_Empty)
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("shipping"));

		mockMvc.perform(request_5Chars)
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("shipping"));

		mockMvc.perform(request_9Chars)
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("shipping"));

		// Not valid requests: 4Chars, 15Chars
		mockMvc.perform(request_4Chars)
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/checkout/coupon"));

		mockMvc.perform(request_15Chars)
			.andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/checkout/coupon"));

	}

	@Test
	public void shippingTest() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);
		when(sCart.getPurchase()).thenReturn(purchase);

		CouponCode coupon = new CouponCode();
		coupon.setCode("abcd");
		when(sCart.getCouponCode()).thenReturn(coupon);

		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/shipping")).andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name("checkout_2"));
	}
	
	@Test
	public void noCartShippingTest() throws Exception {
		when(sCart.getPurchase()).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/shipping")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error"));
	}

	@Test
	public void postShippingTestValidationSuccess() throws Exception {

		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);
		when(sCart.getPurchase()).thenReturn(purchase);

		CouponCode coupon = new CouponCode();
		coupon.setCode("abcd");
		when(sCart.getCouponCode()).thenReturn(coupon);

		when(purchaseService.save(purchase)).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.post("/checkout/shipping").param("firstName", "john")
				.param("lastName", "smith").param("streetAddress", "123 main st.").param("city", "centerville")
				.param("state", "WA").param("zipCode", "12345").param("country", "USA")
				.param("phoneNumber", "1234567890").param("email", "ab@c.com")).andDo(print())
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("billing"));
	}
	
	@Test
	public void postShippingTestValidationFail() throws Exception {

		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);
		when(sCart.getPurchase()).thenReturn(purchase);

		CouponCode coupon = new CouponCode();
		coupon.setCode("abcd");
		when(sCart.getCouponCode()).thenReturn(coupon);

		when(purchaseService.save(purchase)).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.post("/checkout/shipping")).andDo(print())
				.andExpect(flash().attribute("org.springframework.validation.BindingResult.shippingAddress",
						hasProperty("fieldErrorCount", equalTo(9))))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("shipping"));
	}
	
	@Test
	public void noCartPostShippingTest() throws Exception {
		when(sCart.getPurchase()).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.post("/checkout/shipping").param("firstName", "john")
				.param("lastName", "smith").param("streetAddress", "123 main st.").param("city", "centerville")
				.param("state", "WA").param("zipCode", "12345").param("country", "USA")
				.param("phoneNumber", "1234567890").param("email", "ab@c.com")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error"));
	}

	@Test
	public void billingTest() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);
		when(sCart.getPurchase()).thenReturn(purchase);

		CouponCode coupon = new CouponCode();
		coupon.setCode("abcd");
		when(sCart.getCouponCode()).thenReturn(coupon);

		when(purchaseService.save(purchase)).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/billing")).andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name("checkout_3"));
	}
	
	@Test
	public void noCartBillingTest() throws Exception {
		when(sCart.getPurchase()).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/billing")).andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/error"));
	}

	@Test
	public void postBillingTestValidationSuccess() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);
		when(sCart.getPurchase()).thenReturn(purchase);

		CouponCode coupon = new CouponCode();
		coupon.setCode("abcd");
		when(sCart.getCouponCode()).thenReturn(coupon);

		when(purchaseService.save(purchase)).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.post("/checkout/billing").param("firstName", "john")
				.param("lastName", "smith").param("streetAddress", "123 main st.").param("city", "centerville")
				.param("state", "WA").param("zipCode", "12345").param("country", "USA")
				.param("phoneNumber", "1234567890").param("email", "ab@c.com")
				.param("creditCardNumber", "1234567890123456").param("creditCardName", "john smith")
				.param("creditCardExpMonth", "5").param("creditCardExpYear", "2016").param("creditCardCVC", "123")
				.param("billingAddressSame", "false")).andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("confirmation"));
	}

	@Test
	public void postBillingTestValidationFail() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);
		when(sCart.getPurchase()).thenReturn(purchase);

		CouponCode coupon = new CouponCode();
		coupon.setCode("abcd");
		when(sCart.getCouponCode()).thenReturn(coupon);

		when(purchaseService.save(purchase)).thenReturn(purchase);
		mockMvc.perform(MockMvcRequestBuilders.post("/checkout/billing")).andDo(print())
				.andExpect(flash().attribute("org.springframework.validation.BindingResult.billingObject",
						hasProperty("fieldErrorCount", equalTo(14))))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("billing"));
	}
	
	@Test
	public void noCartPostBillingTest() throws Exception {
		when(sCart.getPurchase()).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.post("/checkout/billing").param("firstName", "john")
				.param("lastName", "smith").param("streetAddress", "123 main st.").param("city", "centerville")
				.param("state", "WA").param("zipCode", "12345").param("country", "USA")
				.param("phoneNumber", "1234567890").param("email", "ab@c.com")
				.param("creditCardNumber", "1234567890123456").param("creditCardName", "john smith")
				.param("creditCardExpMonth", "5").param("creditCardExpYear", "2016").param("creditCardCVC", "123")
				.param("billingAddressSame", "false")).andDo(print())
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/error"));
	}

	@Test
	public void confirmationTest() throws Exception {
		Product product = productBuilder();

		when(productService.findById(1L)).thenReturn(product);

		Purchase purchase = purchaseBuilder(product);
		when(sCart.getPurchase()).thenReturn(purchase);

		CouponCode coupon = new CouponCode();
		coupon.setCode("abcd");
		when(sCart.getCouponCode()).thenReturn(coupon);

		when(purchaseService.save(purchase)).thenReturn(purchase);

		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/confirmation")).andDo(print())
			.andExpect(status().isOk())
			.andExpect(view().name("order_confirmation"));
	}
	
	@Test
	public void noCartConfirmationTest() throws Exception {
		when(sCart.getPurchase()).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/confirmation")).andDo(print())
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/error"));
	}

	@Test
	public void emailTest() throws Exception {
		//unfortunately it is not possible to mock the Thymeleaf Template Engine (final methods problem) which is known and will be fixed in 3.0
		//so all we test is that the endpoint is accessible.
		mockMvc.perform(MockMvcRequestBuilders.get("/checkout/email")).andDo(print()).andExpect(status().isOk());
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
