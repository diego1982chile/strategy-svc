package cl.dsoto.trading.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.springframework.cache.annotation.Cacheable;
import org.ta4j.core.Bar;

import javax.json.JsonArray;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by root on 01-10-23.
 */
//@RegisterRestClient(baseUri = "https://eodhd.com/api/eod/MCD.US?api_token=demo")
@RegisterRestClient(baseUri = "https://eodhd.com")
@Path("/api")
public interface EodhdClient {

    @GET
    @Path("/eod/EURUSD.FOREX")
    JsonArray getEodHistoricalStockPrice(@QueryParam("api_token") String apiToken, @QueryParam("fmt") String fmt,
                                         @QueryParam("from") String from, @QueryParam("to") String to);

    @GET
    @Path("/intraday/EURUSD.FOREX")
    JsonArray getIntradayHistoricalStockPrice(@QueryParam("api_token") String apiToken, @QueryParam("fmt") String fmt,
                                              @QueryParam("from") String from, @QueryParam("to") String to);

}
