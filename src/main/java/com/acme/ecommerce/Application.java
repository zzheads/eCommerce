package com.acme.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//  DONE: Run and preview the application:
//	DONE: Page through products in the storefront
//	DONE: Click on product picture to reveal product detail page
//	DONE: Add products from the storefront and from product detail pages
//	DONE: View the cart, updating and removing products
//	DONE: Go through the checkout process, providing valid and invalid data

//	DONE: Explore the source code
//	DONE: Open the controller classes, and find the methods that process individual requests
//	DONE: View the entity models in the domain package
//	DONE: Explore the service and repository packages
//	DONE: Look through the config package to see how the application is configured
//	DONE: Locate the application properties files and notice configuration values
//	DONE: Find the initial data that is loaded upon application startup
//	DONE: Locate and run all tests

// DONE: Moved all configuration values to /properties/eCommerce.properties
// DONE: Changed 'POINT' decimal separator in all templates for 'COMMA', because DecimalFormat("#0.##").format(PRODUCT_PRICE) which used in tests returns 'COMMA'
// DONE: changed imagePath to: @Value("${imagePath:src/main/resources/static/images/}")

// DONE: Bug fix: Add form validation to the coupon code field in the first step of the checkout process. A coupon code will be considered valid if it contains between 5 and 10 characters. A unit test should also be added to verify that the added validation is working. - TODO: Fix CSS for validation error message
// TODO: Bug fix: Ensure that enough products are in stock before adding to the shopping cart. Whether adding products to the cart from product detail pages or updating an product’s quantity from the cart view, more products than are in stock can be added to the cart. Fix this issue and add a unit test to cover this scenario.
// TODO: Bug fix: Update the order confirmation view template to mask all but the last 4 digits of the credit card number.
// TODO: Bug fix: Update the order confirmation email template to remove the billing address and all payment info.
// TODO: Note that the email feature is not fully implemented since it would require an SMTP server, so this is implemented instead as a file download on the confirmation page. The HTML of this file would be sent as the content of the confirmation email in a fully implemented version.
// TODO: Enhancement: Add the cart subtotal to the page header, as part of the “View Cart” link. The subtotal should display only if the cart is not empty, and the number should be formatted with a dollar sign, as well as a comma for the thousands separator. Add a unit test to verify that the rendered view contains the subtotal.
// TODO: Enhancement: Add flash messaging to the application for adding, updating, and removing products from the cart, and for emptying the cart. Don’t forget the case for when a user tries to request a product quantity that exceeds its amount in stock. Include unit tests to verify that flash messages work correctly.
// TODO: Enhancement: Detect when a product’s detail view is requested, but the id requested isn’t found in the database. The rendered view should display a message saying that the product wasn’t found.
//
// To get an "exceeds" rating, you can expand on the project in the following ways:
//
// TODO: Combine the common elements of Thymeleaf templates into a single template as fragments, and include those fragments in the individual templates, so as to eliminate the duplication of markup.
// TODO: Throw exceptions in the service layer for the case when an product’s requested quantity exceeds the quantity in stock, instead of checking the quantity in the controller.
// TODO: Throw an exception in the service layer for the case when a certain product entity is requested - ProductService.findById(Long) - but is not found. Explicitly catch this exception in the controller layer, or use an @ExceptionHandler as stated in the next extra credit opportunity.
// TODO: Research @ExceptionHandler controller methods in Spring, and add one for when a product’s detail view is requested for an unknown product id. This should produce a 404 response code and render a view that displays a friendly page saying that the product wasn’t found.





@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
