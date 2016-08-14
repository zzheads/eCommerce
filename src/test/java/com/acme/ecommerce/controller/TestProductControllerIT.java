package com.acme.ecommerce.controller;

import com.acme.ecommerce.Application;
import com.acme.ecommerce.config.PersistenceConfig;
import com.acme.ecommerce.domain.FormatLocale;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.acme.ecommerce.domain.FormatLocale.format;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, PersistenceConfig.class})
@WebIntegrationTest
public class TestProductControllerIT {

	@Autowired
	WebApplicationContext context;
	
	private static final String PRODUCT_ID = "1";
	private static final String PRODUCT_NAME = "Corkscrew";
	private static final String PRODUCT_DESC = "A screw for corks";
	private static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(189.79);
	private static final Integer ORDER_QUANTITY = 3;
	
	WebClient webClient;
	
	 static {
		 System.setProperty("properties.home", "properties");
	 }

	@Before
	public void setUp() throws Exception {
		webClient = MockMvcWebClientBuilder
				.webAppContextSetup(context)
				.build();
	}
	
	@After
	public void cleanup() {
		this.webClient.close();
	}

	@Test
	public void ProductDetailAddItemIntegrationTest() throws Exception {
		HtmlPage productPage = webClient.getPage("http://localhost:8080/product/detail/" + PRODUCT_ID);
		String productName = productPage.getHtmlElementById("productName").getTextContent();
		String productPrice = productPage.getHtmlElementById("productPrice").getTextContent();
		String productDesc = productPage.getHtmlElementById("productDescription").getTextContent();
		String productPriceExpected = "$" + format(PRODUCT_PRICE);

		assertThat(productName).isEqualTo(PRODUCT_NAME);
		assertThat(productPrice).isEqualTo(productPriceExpected);
		assertThat(productDesc).isEqualTo(PRODUCT_DESC);
		
		HtmlForm form = productPage.getFormByName("detailCartForm");
		HtmlTextInput quantityInput = form.getInputByName("quantity");
		HtmlAnchor formAnchor = productPage.getAnchorByName("detailButton");
		quantityInput.setValueAttribute(ORDER_QUANTITY.toString());
		HtmlPage productListPage = formAnchor.click();
		
		assertThat(productListPage.getUrl().toString()).endsWith("/product/");
		HtmlAnchor cartAnchor = productListPage.getAnchorByName("cartButton");
		HtmlPage cartPage = cartAnchor.click();
		
		assertThat(cartPage.getUrl().toString()).endsWith("/cart");
		
		String productCartName = cartPage.getAnchorByName("productName" + PRODUCT_ID).getTextContent();
		String productCartPrice = cartPage.getHtmlElementById("productPrice" + PRODUCT_ID).getTextContent();
		
		String productCartSubtotal = cartPage.getHtmlElementById("subtotal").getTextContent();
		
		assertThat(productCartName).isEqualTo(PRODUCT_NAME);
		assertThat(productCartPrice).isEqualTo(productPriceExpected);

		assertThat(productCartSubtotal).isEqualTo("$" + format(PRODUCT_PRICE.multiply(BigDecimal.valueOf(ORDER_QUANTITY))));
	}

	@Test
	public void ProductsSubTotalTest() throws Exception {
		HtmlPage indexPage = webClient.getPage("http://localhost:8080/");
		HtmlAnchor formAnchor;
		HtmlTextArea subTotal = null;
		final int COUNT_CLICKS = 3;

		try {
			subTotal = indexPage.getHtmlElementById("subTotal");
		} catch (ElementNotFoundException ex) {
			assertThat(subTotal).isEqualTo(null);
		}

		HtmlForm form = indexPage.getFormByName("form "+PRODUCT_ID);

		for (int i=0;i<COUNT_CLICKS;i++) {
			formAnchor = indexPage.getAnchorByName("addButton "+PRODUCT_ID);
			indexPage = formAnchor.click();
		}

		String subTotalString = indexPage.getHtmlElementById("subTotal").getTextContent();
		assertThat(subTotalString).isEqualTo("SubTotal: $" + format(PRODUCT_PRICE.multiply(BigDecimal.valueOf(COUNT_CLICKS))));
	}


}
