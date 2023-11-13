package cl.dsoto.trading.resources;

import cl.dsoto.trading.entities.WFOEntity;
import cl.dsoto.trading.model.Status;
import cl.dsoto.trading.model.TimeFrame;
import cl.dsoto.trading.model.WFO;
import cl.dsoto.trading.services.WFOService;
import lombok.extern.log4j.Log4j;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by root on 13-10-22.
 */
@RequestScoped
@Produces(APPLICATION_JSON)
@Path("wfos")
@RolesAllowed({"ADMIN","USER"})
//@PermitAll
@Log4j
public class WFOResource {

    @Inject
    WFOService wfoService;

    String errorMsg;


    @GET
    public Response getAllWFOs() {
        try {
            List<WFO> houses = wfoService.getAllWFOs();
            return Response.ok(houses).build();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return Response.serverError().build();
    }


    @GET
    @Path("new")
    public Response getNewWFO() {
        try {
            LocalDate start = LocalDate.ofYearDay(LocalDate.now().getYear() - 10, 1);
            LocalDate end = LocalDate.ofYearDay(LocalDate.now().getYear(), 1);
            String name = "WFO_" + start + "-" + end;

            WFO wfo = WFO.builder()
                    .start(start)
                    .end(end)
                    .name(name)
                    .status(Status.NEW)
                    .timeFrame(TimeFrame.DAY)
                    .inSample(0.4)
                    .outSample(0.1)
                    .iterations(3)
                    .wfoRecords(new ArrayList<>())
                    .build();
            return Response.ok(wfo).build();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return Response.serverError().build();
    }


    @GET
    @Path("{id}")
    public Response getWFOeById(@PathParam("id") int id) {
        try {
            WFO wfo = wfoService.getWFOById(id);
            return Response.ok(wfo).build();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @POST
    @Path("save")
    public Response saveWFO(WFO wfo) {
        try {
            WFO newWFO = wfoService.create(wfo);
            return Response.ok(newWFO).build();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return Response.serverError().entity(errorMsg).build();
    }

    @POST
    @Path("process")
    public Response processWFO(WFO wfo) {
        try {
            WFO newWFO = wfoService.process(wfo);
            return Response.ok(newWFO).build();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return Response.serverError().entity(errorMsg).build();
    }

    @POST
    @Path("abort")
    public Response abortWFO(WFO wfo) {
        try {
            WFO newWFO = wfoService.abort(wfo);
            return Response.ok(newWFO).build();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return Response.serverError().entity(errorMsg).build();
    }


    @DELETE
    @Path("delete/{id}")
    public Response deleteWFO(@PathParam("id") long id) {
        try {
            wfoService.deleteWFO(id);
            return Response.ok(id).build();
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

}
