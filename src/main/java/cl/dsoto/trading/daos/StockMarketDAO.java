package cl.dsoto.trading.daos;

import cl.dsoto.trading.model.TimeFrame;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

import java.time.LocalDate;

/**
 * Created by root on 02-10-23.
 */
public interface StockMarketDAO {

    public TimeSeries getHistoricalPrice(TimeFrame timeFrame, LocalDate from, LocalDate to) throws Exception;

    public Bar getLastPrice();



}
