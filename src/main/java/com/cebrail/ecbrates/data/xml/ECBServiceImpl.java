package com.cebrail.ecbrates.data.xml;

import com.cebrail.ecbrates.Model.Currency;
import com.cebrail.ecbrates.Model.Day;
import com.cebrail.ecbrates.Repository.CurrencyRepository;
import com.cebrail.ecbrates.Repository.DayRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(
        value = "xpath.enabled",
        havingValue = "false")
public class ECBServiceImpl implements EcbXmlService {
    public static String DAILY_RATES_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    public static String LAST_90_DAY_RATES_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
    public static String HISTORICAL_RATES_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.xml";


    final private RestTemplate restTemplate;
    final private CurrencyRepository currencyRepository;
    final private DayRepository dayRepository;

    @Autowired
    public ECBServiceImpl(RestTemplate restTemplate, CurrencyRepository currencyRepository, DayRepository dayRepository) {
        this.restTemplate = restTemplate;
        this.currencyRepository = currencyRepository;
        this.dayRepository = dayRepository;
        System.err.println("JSON service initialized");
    }

    @Override
    public JSONObject getDailyRates() {
        return getJSONObject(DAILY_RATES_URL);
    }

    @Override
    public JSONObject getLast90DaysRates() {
        return getJSONObject(LAST_90_DAY_RATES_URL);
    }

    @Override
    public JSONObject getHistoricalRates() {
        return getJSONObject(HISTORICAL_RATES_URL);
    }

    @Override
    public JSONObject getJSONObject(String fileURL) {
        String xmlFile = downloadTheFileToString(fileURL);
        return XML.toJSONObject(xmlFile);
    }

    @Override
    public String downloadTheFileToString(String fileUrl) {
        return restTemplate.getForObject(fileUrl, String.class);
    }

    @Override
    public void dailyUpdate() {
        JSONObject object = getDailyRates();
        //The Envelope contains all the data
        JSONObject envelope = object.getJSONObject("gesmes:Envelope");

        //The Cube object, contains a single Cube[@time] object
        JSONObject cube = envelope.getJSONObject("Cube");

        //The Cube[@time] object
        JSONObject cubeTime = cube.getJSONObject("Cube");

        String time = cubeTime.getString("time");//YYYY-MM-DD
        LocalDate currentDate = LocalDate.parse(time, DateTimeFormatter.BASIC_ISO_DATE);

        Day today = new Day();
        List<Currency> currencies = new ArrayList<>();
        today.setDate(currentDate);
        today.setCurrencies(currencies);

        //The Cube[@time] contains a list(array) of Cube[@rate][@currency]
        JSONArray cubeRateCurrency = cubeTime.getJSONArray("Cube");
        for (Object o : cubeRateCurrency) {
            JSONObject currency = (JSONObject) o;

            Double rate = (Double) currency.get("rate");
            String symbol = (String) currency.get("currency");

            Currency currentCurrency = new Currency(symbol, rate);
            today.getCurrencies().add(currentCurrency);
        }
        dayRepository.save(today);
    }

    private void updateXDays(JSONObject object) {
        //All the data is inside this object
        JSONObject envelope = object.getJSONObject("gesmes:Envelope");

        //First Cube object, which contains a list(array) of Cube[@time]s (incorrect)
        JSONObject cube = envelope.getJSONObject("Cube");

        //Second Cube is Cube[@time] which contains a list of Cube[@rate][@currency](incorrect)
        JSONArray cubeTimes = cube.getJSONArray("Cube");
        for (Object cubeTimeObject : cubeTimes) {

            Day day1 = new Day();
            List<Currency> currencies = new ArrayList<>();

            JSONObject cubeTimeJObject = (JSONObject) cubeTimeObject;

            LocalDate currentDate = LocalDate.parse(cubeTimeJObject.getString("time"), DateTimeFormatter.BASIC_ISO_DATE);


            day1.setDate(currentDate);
            day1.setCurrencies(currencies);

            //The Last cube, Cube[@rate][@currency]
            for (Object cubeRateCurrencyObject : cubeTimeJObject.getJSONArray("Cube")) {
                JSONObject cubeRateCurrencyJO = (JSONObject) cubeRateCurrencyObject;

                Double rate = cubeRateCurrencyJO.getDouble("rate");
                String currency = cubeRateCurrencyJO.getString("currency");

                Currency nc = new Currency(currency, rate);
                day1.getCurrencies().add(nc);
                currencyRepository.save(nc);
            }
            dayRepository.save(day1);
        }
    }

    @Override
    public void weeklyUpdate() {
        updateXDays(getLast90DaysRates());
    }

    @Override
    public void monthlyUpdate() {
        updateXDays(getHistoricalRates());
    }
}
