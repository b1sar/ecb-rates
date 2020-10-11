package com.cebrail.ecbrates.data.xml;

import com.cebrail.ecbrates.data.ScheduledUpdater;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public interface EcbXmlService extends ScheduledUpdater {
    JSONObject getDailyRates();

    JSONObject getLast90DaysRates();

    JSONObject getHistoricalRates();

    JSONObject getJSONObject(String fileURL);

    String downloadTheFileToString(String fileUrl);
}
