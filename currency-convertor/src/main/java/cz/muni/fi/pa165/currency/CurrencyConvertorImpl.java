package cz.muni.fi.pa165.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;


/**
 * This is base implementation of {@link CurrencyConvertor}.
 *
 * @author petr.adamek@embedit.cz
 */
public class CurrencyConvertorImpl implements CurrencyConvertor {

    private final ExchangeRateTable exchangeRateTable;
    private final Logger logger = LoggerFactory.getLogger(CurrencyConvertorImpl.class);

    public CurrencyConvertorImpl(ExchangeRateTable exchangeRateTable) {
        this.exchangeRateTable = exchangeRateTable;
    }

    @Override
    public BigDecimal convert(Currency sourceCurrency, Currency targetCurrency, BigDecimal sourceAmount) {
        BigDecimal exchangeRate;
        logger.trace("Conversion of {} {} to {}", sourceAmount, sourceCurrency, targetCurrency);

        if (sourceCurrency == null || targetCurrency == null || sourceAmount == null) {
            throw new IllegalArgumentException("Some of provided attributes was null");
        }

        try {
            exchangeRate = exchangeRateTable.getExchangeRate(sourceCurrency, targetCurrency);
            if (exchangeRate == null) {
                logger.warn("Exchange rate for given currencies ({}, {}) is not available", sourceCurrency, targetCurrency);
                throw new UnknownExchangeRateException("Exchange rate for given currencies is not known");
            }

        } catch (ExternalServiceFailureException e) {
            logger.error("Conversion failed for currencies {} and {}", sourceCurrency, targetCurrency);
            throw new UnknownExchangeRateException(e);
        }

        return exchangeRate.multiply(sourceAmount).setScale(2, RoundingMode.HALF_EVEN);
    }

}
