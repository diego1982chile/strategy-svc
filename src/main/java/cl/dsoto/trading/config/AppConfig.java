package cl.dsoto.trading.config;

import cl.dsoto.trading.repositories.StrategyRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 12-10-22.
 */
@ApplicationScoped
public class AppConfig {

    Map<String, Object> processMap;

    @PostConstruct
    private void init() {
        processMap = new HashMap<>();
    }

    public Map<String, Object> getProcessMap() {
        return processMap;
    }
}
