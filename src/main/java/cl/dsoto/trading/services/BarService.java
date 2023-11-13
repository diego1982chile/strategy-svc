package cl.dsoto.trading.services;

import cl.dsoto.trading.entities.StrategyEntity;
import cl.dsoto.trading.mappers.StrategyMapper;
import cl.dsoto.trading.model.ProblemType;
import cl.dsoto.trading.model.Strategy;
import cl.dsoto.trading.model.WFO;
import cl.dsoto.trading.repositories.StrategyRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.util.Pair;
import org.ta4j.core.Bar;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by root on 13-10-22.
 */
@RequestScoped
public class BarService {

    @PersistenceContext
    private EntityManager entityManager;
    private StrategyRepository strategyRepository;

    @Inject
    private StrategyMapper strategyMapper;

    @PostConstruct
    private void init() {
        // Instantiate Spring Data factory
        RepositoryFactorySupport factory = new JpaRepositoryFactory(entityManager);
        // Get an implemetation of PersonRepository from factory
        this.strategyRepository = factory.getRepository(StrategyRepository.class);
    }

    public Pair<Integer,Integer> getTrainRange(WFO wfo, List<Bar> bars, int year) throws Exception {
        int trainRange = calculateTrainRange(wfo);

        int lowerBound = 0;
        int upperBound = 0;


        for (Bar bar : bars) {
            if (bar.getEndTime().getYear() == year) {
                break;
            }
            lowerBound++;
        }

        for (Bar bar : bars) {
            if (bar.getEndTime().getYear() == year + trainRange) {
                break;
            }
            upperBound++;
        }

        return Pair.of(lowerBound + 1, upperBound);
    }

    public Pair<Integer,Integer> getTestRange(WFO wfo, List<Bar> bars, int year) throws Exception {
        int trainRange = calculateTrainRange(wfo);
        int testRange = calculateTestRange(wfo);

        int lowerBound = 0;
        int upperBound = 0;


        for (Bar bar : bars) {
            if (bar.getEndTime().getYear() == year + trainRange) {
                break;
            }
            lowerBound++;
        }

        for (Bar bar : bars) {
            if (bar.getEndTime().getYear() == year + trainRange + testRange) {
                break;
            }
            upperBound++;
        }

        return Pair.of(lowerBound + 1, upperBound - 1);
    }

    private int calculateTrainRange(WFO wfo) throws Exception {
        Period duration = wfo.getStart().until(wfo.getEnd());
        long range = duration.getYears();
        return (int) (range * wfo.getInSample());
    }

    private int calculateTestRange(WFO wfo) throws Exception {
        Period duration = wfo.getStart().until(wfo.getEnd());
        long range = duration.getYears();
        return (int) (range * wfo.getOutSample());
    }

    public int calculateIterations(WFO wfo) {
        Period duration = wfo.getStart().until(wfo.getEnd());
        long range = duration.getYears();
        return (int) ((1 - (wfo.getInSample() + wfo.getOutSample())) * range);
    }
}
