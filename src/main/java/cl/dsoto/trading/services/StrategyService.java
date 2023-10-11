package cl.dsoto.trading.services;

import cl.dsoto.trading.entities.StrategyEntity;
import cl.dsoto.trading.mappers.StrategyMapper;
import cl.dsoto.trading.model.ProblemType;
import cl.dsoto.trading.model.Strategy;
import cl.dsoto.trading.repositories.StrategyRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by root on 13-10-22.
 */
@RequestScoped
public class StrategyService {

    @PersistenceContext
    private EntityManager entityManager;
    private StrategyRepository strategyRepository;

    @Named
    private StrategyMapper strategyMapper;

    @PostConstruct
    private void init() {
        // Instantiate Spring Data factory
        RepositoryFactorySupport factory = new JpaRepositoryFactory(entityManager);
        // Get an implemetation of PersonRepository from factory
        this.strategyRepository = factory.getRepository(StrategyRepository.class);
    }

    public List<Strategy> getStrategies(ProblemType problemType) {

        List<StrategyEntity> strategiesEntities = strategyRepository.findAll();

        List<Strategy> strategies = new ArrayList<>();

        strategiesEntities.forEach(s -> strategies.add(strategyMapper.toDomain(s)));

        return strategies.stream().filter(s -> s.getProblemType().equals(problemType)).collect(Collectors.toList());
    }
}
