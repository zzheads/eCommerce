package com.acme.ecommerce.domain;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Component
@Scope("session")
public class CouponCode {

	// @Size (min = 5, max = 10, message = "Coupon code length must be minimum 5 and 10 maximum characters.")
	@Pattern(regexp = "|.{5,10}", message = "Coupon code length must be minimum 5 and maximum 10 characters.")
	private String code;

	public CouponCode () {}

	public CouponCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
