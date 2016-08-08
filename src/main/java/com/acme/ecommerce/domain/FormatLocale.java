package com.acme.ecommerce.domain;//

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

// eCommerce
// com.acme.ecommerce.domain created by zzheads on 08.08.2016.
//
public class FormatLocale {


    public static String format (BigDecimal digit) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat)nf;
        df.applyPattern("#0.##");
        return df.format(digit);
    }

}
