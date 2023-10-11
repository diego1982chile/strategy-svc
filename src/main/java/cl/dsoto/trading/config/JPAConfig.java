package cl.dsoto.trading.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by root on 12-10-22.
 */
@ApplicationScoped
public class JPAConfig {

    @PersistenceContext
    @Produces
    private EntityManager em;
}
