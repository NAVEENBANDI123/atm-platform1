package com.atm.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Standard reducing-balance EMI math.
 * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
 * where r is the monthly interest rate and n is the tenure in months.
 */
public final class EmiCalculator {

    private EmiCalculator() {
    }

    public static BigDecimal monthlyEmi(BigDecimal principal, BigDecimal annualRatePercent, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Tenure must be positive");
        }
        BigDecimal r = annualRatePercent
                .divide(new BigDecimal("100"), 12, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 12, RoundingMode.HALF_UP);
        if (r.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(months), 2, RoundingMode.HALF_UP);
        }
        BigDecimal one = BigDecimal.ONE;
        BigDecimal onePlusR = one.add(r);
        BigDecimal pow = onePlusR.pow(months);
        BigDecimal numerator = principal.multiply(r).multiply(pow);
        BigDecimal denominator = pow.subtract(one);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal monthlyRate(BigDecimal annualRatePercent) {
        return annualRatePercent
                .divide(new BigDecimal("100"), 12, RoundingMode.HALF_UP)
                .divide(new BigDecimal("12"), 12, RoundingMode.HALF_UP);
    }
}
