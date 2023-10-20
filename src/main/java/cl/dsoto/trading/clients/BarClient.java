package cl.dsoto.trading.clients;

import cl.dsoto.trading.model.TimeFrame;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.json.JsonArray;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Created by root on 01-10-23.
 */
//@RegisterRestClient(baseUri = "https://eodhd.com/api/eod/MCD.US?api_token=demo")
@RegisterRestClient(baseUri = "http://dnssemantikos:9090/bar-svc")
@Path("/api")
public interface BarClient {


    @GET
    @Path("bars")
    Response getBars(@QueryParam("timeFrame") TimeFrame timeFrame,
                     @QueryParam("start") String start,
                     @QueryParam("id") String end);

}
