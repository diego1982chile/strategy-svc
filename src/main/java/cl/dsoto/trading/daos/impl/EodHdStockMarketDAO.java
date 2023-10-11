package cl.dsoto.trading.daos.impl;

import cl.dsoto.trading.daos.StockMarketDAO;
import cl.dsoto.trading.clients.EodhdClient;
import cl.dsoto.trading.model.TimeFrame;
import lombok.extern.log4j.Log4j;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Created by root on 02-10-23.
 */
@RequestScoped
@Log4j
public class EodHdStockMarketDAO implements StockMarketDAO {

    @Inject
    EodhdClient eodhdClient;

    @Override
    public TimeSeries getHistoricalPrice(TimeFrame timeFrame, LocalDate from, LocalDate to) throws Exception {
        TimeSeries timeSeries = new BaseTimeSeries();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fromStr;
        String toStr;
        JsonArray jsonArray;

        switch (timeFrame) {
            case DAY:
                fromStr = from.format(formatter);
                toStr = to.format(formatter);
                jsonArray = eodhdClient.getEodHistoricalStockPrice("651a20bbcd89d0.83340578", "json", fromStr, toStr);
                break;
            case HOUR:
                fromStr = String.valueOf(from.atStartOfDay().atZone(ZoneId.of("UTC")).toEpochSecond());
                toStr = String.valueOf(to.atStartOfDay().atZone(ZoneId.of("UTC")).toEpochSecond());
                jsonArray = eodhdClient.getIntradayHistoricalStockPrice("651a20bbcd89d0.83340578", "json", fromStr, toStr);
                break;
            default:
                throw new Exception("Unsupported time frame for historical price");
        }

        for (JsonValue jsonValue : jsonArray) {
            JsonObject jsonObject = jsonValue.asJsonObject();
            LocalDate localDate = LocalDate.parse(jsonObject.get("date").toString().replace("\"",""),formatter);
            double open = Double.parseDouble(jsonObject.getString("open"));
            double high = Double.parseDouble(jsonObject.getString("high"));
            double low = Double.parseDouble(jsonObject.getString("low"));
            double close = Double.parseDouble(jsonObject.getString("close"));
            double volume = Double.parseDouble(jsonObject.getString("volume"));

            timeSeries.addBar(new BaseBar(localDate.atStartOfDay(ZoneId.of("America/New_York")), open, high, low, close, volume));

        }
        return timeSeries;
    }

    @Override
    public Bar getLastPrice() {
        return null;
    }
}
