package com.cebrail.ecbrates.data.xpath;

import com.cebrail.ecbrates.Model.Currency;
import com.cebrail.ecbrates.Model.Day;
import com.cebrail.ecbrates.Service.DayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@ConditionalOnProperty(
        value = "xpath.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class EcbXPathServiceImpl implements EcbXPathService {
    public static String DAILY_RATES_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    public static String LAST_90_DAY_RATES_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml";
    public static String HISTORICAL_RATES_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.xml";
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private RestTemplate restTemplate;
    private DayService dayService;

    @Autowired
    public EcbXPathServiceImpl(RestTemplate restTemplate, DayService dayService) {
        this.restTemplate = restTemplate;
        this.dayService = dayService;
        System.err.println("XPath service initialized");
    }

    @Scheduled(initialDelay = 864_00_000L, fixedRate = 864_00_000L)
    @Override
    public void dailyUpdate() {
        logger.log(Level.INFO, "Started the daily update at: " + LocalDateTime.now().toString());
        try {
            parseAndUpdate(DAILY_RATES_URL);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            //TODO: Update failed send email to the admin
        }
    }

    @Scheduled(initialDelay = 600_800_000L, fixedRate = 600_800_000L)
    @Override
    public void weeklyUpdate() {
        logger.log(Level.INFO, "Started the weekly update at: " + LocalDateTime.now().toString());
        try {
            parseAndUpdate(LAST_90_DAY_RATES_URL);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            //TODO: Update failed send email to the admin
        }
    }

    @Scheduled(initialDelay = 10000, fixedRate = 259_200_0000L)
    @Override
    public void monthlyUpdate() {
        logger.log(Level.INFO, "Started the monthly update at: " + LocalDateTime.now().toString());
        try {
            parseAndUpdate(HISTORICAL_RATES_URL);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            //TODO: Update failed send email to the admin
        }
    }

    private void parseAndUpdate(String fileUrl) throws XPathExpressionException {

        Document document = createDocument(fileUrl);

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(getNamespaceContext());


        String expressionTime = "//eurofxref:Cube[@time]";
        String expressionAltNode = "//eurofxref:Cube[@time='%s']/descendant::eurofxref:Cube";

        NodeList nodeList = (NodeList) xPath.compile(expressionTime).evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String time = node.getAttributes().getNamedItem("time").getNodeValue();
            System.out.println("time : " + time);

            LocalDate date = LocalDate.parse(time, DateTimeFormatter.ISO_DATE);
            if (!dayService.existsById(date)) {
                List<Currency> currencies = new ArrayList<>();
                Day newDay = new Day(date, currencies);

                NodeList nodeList1 = (NodeList) xPath.compile(String.format(expressionAltNode, time)).evaluate(document, XPathConstants.NODESET);
                System.out.println(nodeList1.getLength());
                for (int j = 0; j < nodeList1.getLength(); j++) {
                    Node cr = nodeList1.item(j);
                    String rate = cr.getAttributes().getNamedItem("rate").getNodeValue();
                    String name = cr.getAttributes().getNamedItem("currency").getNodeValue();
                    System.out.println("rate: " + rate + " currency: " + name);

                    Double theRate = Double.parseDouble(rate);
                    Currency currency = new Currency(name, theRate);
                    newDay.getCurrencies().add(currency);
                }

                dayService.save(newDay);
            }
        }
    }

    private Document createDocument(String fileUrl) {
        Document document = null;
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            var builder = DocumentBuilderFactory.newInstance();
            builder.setNamespaceAware(true);
            builder.setIgnoringElementContentWhitespace(true);

            DocumentBuilder newBuilder = builder.newDocumentBuilder();
            String content = downloadTheFileToString(fileUrl);
            var inputSource = new InputSource(new StringReader(content));
            document = newBuilder.parse(inputSource);
            return document;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    private String downloadTheFileToString(String fileUrl) {
        return restTemplate.getForObject(fileUrl, String.class);
    }

    private SimpleNamespaceContext getNamespaceContext() {
        SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
        String gesmes = "http://www.gesmes.org/xml/2002-08-01";
        String normal = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref";
        Map<String, String> bindings = new HashMap<>();
        bindings.put("gesmes", gesmes);
        bindings.put("eurofxref", normal);
        namespaceContext.setBindings(bindings);

        return namespaceContext;
    }
}
