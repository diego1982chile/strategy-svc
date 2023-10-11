package cl.dsoto.trading.filters;

import lombok.extern.java.Log;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Created by root on 02-10-23.
 */
@Provider
@Log
public class RequestLoggingFilter implements ContainerRequestFilter {
    private static final Logger logger = Logger.getLogger(RequestLoggingFilter.class.getName());
    @Override
    public void filter(ContainerRequestContext crc) {
        System.out.println(crc.getMethod() + " " + crc.getUriInfo().getAbsolutePath());
        for (String key : crc.getHeaders().keySet()) {
            logger.info("[REST Logging] " +key + ": " + crc.getHeaders().get(key));
        }
    }
}
