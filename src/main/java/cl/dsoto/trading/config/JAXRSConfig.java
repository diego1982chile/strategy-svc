package cl.dsoto.trading.config;

import org.eclipse.microprofile.auth.LoginConfig;

import javax.annotation.security.DeclareRoles;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Created by des01c7 on 12-12-19.
 */
@DeclareRoles({"ADMIN", "USER"})
//@BasicAuthenticationMechanismDefinition(realmName = "incomes")
@LoginConfig(authMethod = "MP-JWT")
@ApplicationPath("/api")
@ApplicationScoped
public class JAXRSConfig extends Application {

}
