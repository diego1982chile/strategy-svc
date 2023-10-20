package cl.dsoto.trading.services;

import cl.dsoto.trading.mappers.StrategyMapper;
import cl.dsoto.trading.model.ProblemType;
import cl.dsoto.trading.model.Strategy;
import cl.dsoto.trading.repositories.StrategyRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * Created by root on 13-10-22.
 */
@RequestScoped
public class DatabaseService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    StrategyRepository strategyRepository;

    @Inject
    private StrategyMapper strategyMapper;

    @PostConstruct
    private void init() {
        // Instantiate Spring Data factory
        RepositoryFactorySupport factory = new JpaRepositoryFactory(entityManager);
        // Get an implemetation of PersonRepository from factory
        this.strategyRepository = factory.getRepository(StrategyRepository.class);
    }

    @Transactional
    public void removeData() {
        strategyRepository.deleteAll();
    }

    @Transactional
    public void loadData() {

        Strategy rsi2 = Strategy.builder()
                .name("RSI2Strategy")
                .variables(4)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(rsi2));

        Strategy globalExtrema = Strategy.builder()
                .name("GlobalExtremaStrategy")
                .variables(1)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(globalExtrema));

        Strategy stochastic = Strategy.builder()
                .name("StochasticStrategy")
                .variables(7)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(stochastic));

        Strategy movingAverages = Strategy.builder()
                .name("MovingAveragesStrategy")
                .variables(4)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(movingAverages));

        Strategy tunnel = Strategy.builder()
                .name("TunnelStrategy")
                .variables(5)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(tunnel));

        Strategy parabolicSAR = Strategy.builder()
                .name("ParabolicSARStrategy")
                .variables(5)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(parabolicSAR));

        Strategy fxBootcamp = Strategy.builder()
                .name("FXBootCampStrategy")
                .variables(11)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(fxBootcamp));

        Strategy winslow = Strategy.builder()
                .name("WinslowStrategy")
                .variables(10)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(winslow));

        Strategy cciCorrection = Strategy.builder()
                .name("CCICorrectionStrategy")
                .variables(2)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(cciCorrection));

        Strategy macd = Strategy.builder()
                .name("MACDStrategy")
                .variables(6)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(macd));

        Strategy bagovino = Strategy.builder()
                .name("BagovinoStrategy")
                .variables(3)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(bagovino));

        Strategy movingMomemtum = Strategy.builder()
                .name("MovingMomentumStrategy")
                .variables(6)
                .problemType(ProblemType.INTEGER)
                .build();

        strategyRepository.save(strategyMapper.toEntity(movingMomemtum));
    }
}
