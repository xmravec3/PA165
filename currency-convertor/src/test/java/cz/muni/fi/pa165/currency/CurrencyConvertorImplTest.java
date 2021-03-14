package cz.muni.fi.pa165.currency;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrencyConvertorImplTest {
    private CurrencyConvertor currencyConvertor;
    private ExchangeRateTable exchangeRateTable;

    @Before
    public void setUp() throws Exception {
        exchangeRateTable = mock(ExchangeRateTable.class);

        when(exchangeRateTable.getExchangeRate(Currency.getInstance("EUR"), Currency.getInstance("CZK")))
                .thenReturn(new BigDecimal("25"));

        when(exchangeRateTable.getExchangeRate(Currency.getInstance("EUR"), Currency.getInstance("USD")))
                .thenReturn(new BigDecimal("1.122"));

        when(exchangeRateTable.getExchangeRate(Currency.getInstance("EUR"), Currency.getInstance("EUR")))
                .thenThrow(new ExternalServiceFailureException("Unknown currency"));


        when(exchangeRateTable.getExchangeRate(Currency.getInstance("EUR"), Currency.getInstance("JPY")))
                .thenReturn(null);

        when(exchangeRateTable.getExchangeRate(null , Currency.getInstance("EUR")))
                .thenThrow(new IllegalArgumentException());

        when(exchangeRateTable.getExchangeRate(Currency.getInstance("EUR"), null))
                .thenThrow(new IllegalArgumentException());

        currencyConvertor = new CurrencyConvertorImpl(exchangeRateTable);
    }

    @Test
    public void testConvert() throws ExternalServiceFailureException {
        //No rounding
        BigDecimal result = currencyConvertor.convert(Currency.getInstance("EUR"), Currency.getInstance("CZK"), new BigDecimal((10)));
        assertEquals(exchangeRateTable.getExchangeRate(
                Currency.getInstance("EUR"),
                Currency.getInstance("CZK"))
                    .multiply(new BigDecimal("10").setScale(2,BigDecimal.ROUND_HALF_EVEN)),
                result);

        //Rounding up  1.122 * 4 -> 4.488 -> 4.49
        result = currencyConvertor.convert(Currency.getInstance("EUR"), Currency.getInstance("USD"), new BigDecimal((4)));
        assertEquals(exchangeRateTable.getExchangeRate(
                Currency.getInstance("EUR"),
                Currency.getInstance("USD")).multiply(new BigDecimal("4")).setScale(2,BigDecimal.ROUND_HALF_EVEN),
                result);

        //Rounding down  1.122 * 1 -> 1.122 -> 1.12
        result = currencyConvertor.convert(Currency.getInstance("EUR"), Currency.getInstance("USD"), new BigDecimal((1)));
        assertEquals(exchangeRateTable.getExchangeRate(
                Currency.getInstance("EUR"),
                Currency.getInstance("USD"))
                        .multiply(new BigDecimal("1")).setScale(2,BigDecimal.ROUND_HALF_EVEN),
                result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertWithNullSourceCurrency() {
        currencyConvertor.convert(null, Currency.getInstance("EUR"), new BigDecimal("20"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertWithNullTargetCurrency() {
        currencyConvertor.convert(Currency.getInstance("EUR"), null, new BigDecimal("15"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertWithNullSourceAmount() {
        currencyConvertor.convert(Currency.getInstance("EUR"), Currency.getInstance("CZK"), null);
    }

    @Test(expected = UnknownExchangeRateException.class)
    public void testConvertWithUnknownCurrency() {
        currencyConvertor.convert(Currency.getInstance("EUR"), Currency.getInstance("JPY"), new BigDecimal("15"));
    }

    @Test(expected = UnknownExchangeRateException.class)
    public void testConvertWithExternalServiceFailure() {
        currencyConvertor.convert(Currency.getInstance("EUR"), Currency.getInstance("EUR"), new BigDecimal("15"));
    }
}
