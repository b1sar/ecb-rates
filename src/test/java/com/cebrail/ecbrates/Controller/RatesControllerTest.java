package com.cebrail.ecbrates.Controller;

import com.cebrail.ecbrates.Model.Currency;
import com.cebrail.ecbrates.Model.Day;
import com.cebrail.ecbrates.Service.DayService;
import com.cebrail.ecbrates.util.ExchangeRatesUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = RatesController.class)
class RatesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    /*Test Variables*/
    LocalDate todayDate = LocalDate.now();
    LocalDate from = LocalDate.of(2000, 1, 1);
    LocalDate to = LocalDate.of(2015, 10, 3);
    LocalDate unavailableDate = LocalDate.of(1990, 1, 1);
    List<String> unsupportedSymbols = List.of("test-1", "test-2", "test-3");
    List<String> emptySymbolList = Collections.emptyList();
    List<String> validSymbols = List.of("TRY", "GBP", "USD");
    String unvalidBase = "AAA";
    String validBase = "USD";
    String emptyBase = "";
    Currency currency = new Currency("TRY", 23.23d);
    Currency currency1 = new Currency("GBP", 75.73d);
    Currency currency2 = new Currency("USD", 3.21d);
    Currency currency3 = new Currency("PLN", 16.72d);
    Currency currency4 = new Currency("CNY", 25.83d);
    Currency currency5 = new Currency("HKD", 34.94d);
    List<Currency> validCurrencyList = List.of(currency, currency1, currency2,
            currency3, currency4, currency5);
    Day aNormalDay = new Day(todayDate, validCurrencyList);
    @SpyBean
    private ExchangeRatesUtils exchangeRatesUtils;
    @MockBean
    private DayService dayService;
    @InjectMocks
    @Autowired
    private RatesController ratesController;

    //if data of today is requested but its not available in the database
    //it should update the databese, at the /latest endpoint.
    @BeforeEach
    void setUp() {
    }

    /**
     * LATEST
     **/
    @Test
    void getLatest() throws Exception {

        //when
        when(dayService.findById(todayDate)).thenReturn(Optional.of(aNormalDay));

        MvcResult result = mockMvc.perform(get("/latest").requestAttr("at", todayDate)).andReturn();

        //then
        String sResult = result.getResponse().getContentAsString();
        verify(dayService, times(1)).findById(todayDate);
        assertThat(objectMapper.writeValueAsString(aNormalDay)).isEqualToIgnoringWhitespace(sResult);
    }

    //T1 alternative
    @Test
    void ShouldReturnToday_whenInputWithNoParameters() throws Exception {
        when(dayService.findById(LocalDate.now())).thenReturn(Optional.of(aNormalDay));

        MvcResult mvcResult = mockMvc.perform(get("/latest")).andReturn();

        verify(exchangeRatesUtils, times(0)).rebase(any(Day.class), anyString());
        verify(exchangeRatesUtils, times(0)).pickAllSelected(any(Day.class), anyList());
        verify(dayService, times(1)).findById(todayDate);

        String result = mvcResult.getResponse().getContentAsString();
        assertThat(objectMapper.writeValueAsString(aNormalDay)).isEqualToIgnoringWhitespace(result);

    }

    //In this test, actually it shouldn't return an empty object, It should first run the daily update, and then
    //get the latest day, then return that day. For now this is skipped.

    //@T1
    @Test
    void ShouldReturnEmptyDayObject_whenInputWithNoParameters_and_thereIsNoDataOfToday() throws Exception {
        when(dayService.findById(LocalDate.now())).thenReturn(Optional.empty());

        MvcResult mvcResult = mockMvc.perform(get("/latest")).andReturn();

        verify(dayService, times(1)).findById(any(LocalDate.class));
        verify(exchangeRatesUtils, times(0)).rebase(any(Day.class), anyString());

        String response = mvcResult.getResponse().getContentAsString();
        assertThat(objectMapper.writeValueAsString(new Day())).isEqualToIgnoringWhitespace(response);
    }


    //T8
    @Test
    void ShouldReturnRebasedAndFilteredDayObject_whenRequestedWith_At_Base_and_SymbolsParameters() throws Exception {
        //given
        LocalDate randomDate = LocalDate.of(2011, 2, 12);
        Day resultDay = new Day(randomDate, validCurrencyList);
        Optional<String> base = Optional.of("USD");
        Optional<List<String>> symbols = Optional.of(List.of("USD", "TRY"));

        //ExchangeRatesUtils testUtil = spy(ExchangeRatesUtils.class);

        //when
        doCallRealMethod().when(exchangeRatesUtils).rebase(nullable(Day.class), anyString());
        doCallRealMethod().when(exchangeRatesUtils).pickAllSelected(nullable(Day.class), anyList());
        when(dayService.findById(randomDate)).thenReturn(Optional.of(resultDay));

        MvcResult mvcResult = mockMvc.perform(
                get("/latest")
                        .queryParam("at", "2011-02-12")
                        .queryParam("base", base.get())
                        .queryParam("symbols", "USD,TRY")).andReturn();

        verify(exchangeRatesUtils, times(1)).rebase(any(Day.class), anyString());
        verify(exchangeRatesUtils, times(1)).pickAllSelected(any(Day.class), anyList());

        String content = mvcResult.getResponse().getContentAsString();
        Day resultObject = objectMapper.readValue(content, Day.class);
        assertThat(resultObject.getDate()).isEqualTo(randomDate);
        assertThat(resultObject.getCurrencies().size()).isEqualTo(2);

        assertThat(resultObject).is(filteredCorrectly("USD,TRY"));
    }


    /**
     * LATEST V2
     **/

    @Test
    void ShouldReturn_Today_whenRequestedWith_NoParameters() throws Exception {
        when(dayService.findById(any(LocalDate.class))).thenReturn(Optional.of(aNormalDay));

        MvcResult mvcResult = mockMvc.perform(get("/latest")).andReturn();

        verify(exchangeRatesUtils, times(0)).rebase(any(Day.class), anyString());
        verify(exchangeRatesUtils, times(0)).pickAllSelected(any(Day.class), anyList());

        String resultContent = mvcResult.getResponse().getContentAsString();
        String expectedResult = objectMapper.writeValueAsString(aNormalDay);
        assertThat(resultContent).isEqualToIgnoringWhitespace(expectedResult);
    }

    @Test
    void ShouldReturn_FilteredDay_whenRequestedWith_OnlySymbolsParameter() throws Exception {
        when(dayService.findById(any(LocalDate.class))).thenReturn(Optional.of(aNormalDay));
        String symbols = "USD,TRY";
        MvcResult mvcResult = mockMvc.perform(get("/latest").queryParam("symbols", symbols)).andReturn();

        verify(exchangeRatesUtils, times(0)).rebase(any(Day.class), anyString());
        verify(exchangeRatesUtils, times(1)).pickAllSelected(any(Day.class), anyList());

        String resultContent = mvcResult.getResponse().getContentAsString();
        Day actualResultJSON = objectMapper.readValue(resultContent, Day.class);

        assertThat(actualResultJSON).is(filteredCorrectly(symbols));
    }

    @Test
    void ShouldReturn_RebasedDay_whenRequestedWith_OnlyBaseParameter() throws Exception {
        when(dayService.findById(any(LocalDate.class))).thenReturn(Optional.of(aNormalDay));
        String base = "TRY";

        MvcResult mvcResult = mockMvc.perform(get("/latest").queryParam("base", base)).andReturn();

        verify(exchangeRatesUtils, times(0)).pickAllSelected(any(Day.class), anyList());
        verify(exchangeRatesUtils, times(1)).rebase(any(Day.class), anyString());

        String resultContent = mvcResult.getResponse().getContentAsString();
        Day actualResultObject = objectMapper.readValue(resultContent, Day.class);
        assertThat(actualResultObject.getCurrencies().size()).isEqualTo(aNormalDay.getCurrencies().size());
    }

    void ShouldReturn_FilteredAndRebasedDay_whenRequestedWith_onlyAtParameterAbsent() {
    }

    void ShouldReturn_OriginalDay_whenRequestedWith_OnlyAtParameterPresent() {
    }

    void ShouldReturn_NotRebasedButFilteredOriginalDay_whenRequestedWith_OnlyBaseParameterAbsent() {
    }

    void Shouldreturn_NotFilteredButRebasedOriginalDay_whenRequestedWith_OnlySymbolsParameterAbsent() {
    }

    void ShouldReturn_RebasedandFilteredOriginalDay_whenRequestedWith_AllParametersPresent() {
    }
    /** Latest V2**/


    /**
     * HISTORICAL
     **/

    @Test
    void getHistorical() {
    }


    //utilities
    private Condition<? super Day> rebasedCorrectly(Day originalDay, String base) {
        return new Condition<>() {
            @Override
            public boolean matches(Day value) {
                return control(value, originalDay, base);
            }

            private boolean control(Day result, Day originalDay, String base) {
                List<Currency> resultedCur = result.getCurrencies();
                List<Currency> originalCur = originalDay.getCurrencies();

                Optional<Currency> baseCurrencyRate = originalCur.stream()
                        .filter(e -> e.getName().toUpperCase().equals(base.toUpperCase()))
                        .findFirst();

                if (!baseCurrencyRate.isEmpty()) return false;
                if (!baseCurrencyRate.get().getRate().equals(1.0d)) return false;

                for (Currency cur : resultedCur) {
                    Optional<Currency> temp = originalCur.stream()
                            .filter(e -> e.getName().toUpperCase().equals(base.toUpperCase()))
                            .findFirst();
                    if (!cur.getRate().equals(temp.get().getRate())) {
                        return false;
                    }
                }
                return true;
            }
        };
    }


    private Condition<? super Day> filteredCorrectly(String s) {
        List<String> symbols = Arrays.asList(s.split(","));
        return new Condition<>() {
            @Override
            public boolean matches(Day value) {
                long expectedSize = value.getCurrencies().stream().filter(e -> symbols.contains(e.getName())).count();
                long actualSize = value.getCurrencies().size();
                return expectedSize == actualSize;
            }
        };
    }
}