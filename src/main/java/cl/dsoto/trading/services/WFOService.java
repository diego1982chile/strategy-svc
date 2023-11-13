package cl.dsoto.trading.services;


import cl.dsoto.trading.clients.BarClient;
import cl.dsoto.trading.entities.WFOEntity;
import cl.dsoto.trading.mappers.WFOMapper;
import cl.dsoto.trading.model.*;
import cl.dsoto.trading.model.Strategy;
import cl.dsoto.trading.repositories.WFORepository;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j;
import lombok.var;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.ta4j.core.*;
import org.ta4j.core.analysis.CashFlow;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.uma.jmetal.runner.multiobjective.MOCellStockMarketIntegerRunner;
import ta4jexamples.research.MultipleStrategy;
import ta4jexamples.strategies.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static cl.dsoto.trading.constants.ServiceConstants.*;
import static java.util.Collections.EMPTY_LIST;

/**
 * Created by des01c7 on 28-03-19.
 */
@RequestScoped
@Log4j
public class WFOService {

    @Inject
    BarClient barClient;

    @PersistenceContext
    private EntityManager entityManager;
    private WFORepository wfoRepository;

    @Inject
    private WFOMapper wfoMapper;

    @Inject
    StrategyService strategyService;

    @Inject
    BarService barService;

    @Inject
    AsyncService asyncService;

    static Map<String, Double> efficiency = new HashMap<>();

    static int cont = 0;

    static int test = 0;

    static String id = UUID.randomUUID().toString();

    @PostConstruct
    private void init() {
        // Instantiate Spring Data factory
        RepositoryFactorySupport factory = new JpaRepositoryFactory(entityManager);
        // Get an implemetation of PersonRepository from factory
        this.wfoRepository = factory.getRepository(WFORepository.class);
    }


    public List<WFO> getAllWFOs() {
        List<WFOEntity> wfoEntities = wfoRepository.findAllOrderByIdDesc();
        List<WFO> wfos = wfoEntities.stream().map(w -> wfoMapper.toDomain(w)).collect(Collectors.toList());
        return wfos;
    }

    public WFO getWFOById(int id) {
        WFOEntity wfoEntity = wfoRepository.findById(id);
        WFO wfo = null;
        if (Objects.nonNull(wfoEntity)) {
            wfo = wfoMapper.toDomain(wfoEntity);
        }
        return wfo;
    }

    @Transactional
    public WFO create(WFO wfo) {
        wfo.setName("WFO_" + wfo.getStart().getYear() + "-" + wfo.getEnd().getYear());
        wfo.setStep(wfo.getTimeFrame());
        wfo.setStatus(Status.CREATED);
        // Set to the final of the year
        wfo.setEnd(wfo.getEnd().plusYears(1).minusDays(1));
        WFOEntity wfoEntity = wfoMapper.toEntity(wfo);
        wfoEntity = wfoRepository.save(wfoEntity);
        wfo = wfoMapper.toDomain(wfoEntity);
        return wfo;
    }

    @Transactional
    public WFO abort(WFO wfo) {
        wfo.setName("WFO_" + wfo.getStart().getYear() + "-" + wfo.getEnd().getYear());
        wfo.setStep(wfo.getTimeFrame());
        wfo.setStatus(Status.ABORTED);
        // Set to the final of the year
        //wfo.setEnd(wfo.getEnd().plusYears(1).minusDays(1));
        WFOEntity wfoEntity = wfoMapper.toEntity(wfo);
        wfoEntity = wfoRepository.save(wfoEntity);
        wfo = wfoMapper.toDomain(wfoEntity);
        return wfo;
    }


    public WFO process(WFO wfo) throws Exception {

        WFOEntity wfoEntity = wfoRepository.findById(wfo.getId());
        WFO persistedWFO = wfoMapper.toDomain(wfoEntity);

        if (persistedWFO.getStatus().equals(Status.PROCESSING)) {
            log.error("WFO is currently processing!!");
            throw new Exception("WFO is currently processing!!");
        }

        asyncService.updateStatus(wfo, Status.PROCESSING);

        var future = asyncService.process(wfo);
        log.info(future);
        //updateWFO(wfo);

        return wfo;
    }

    @Transactional
    private void updateWFO(WFO wfo) {
        WFOEntity wfoEntity = wfoMapper.toEntity(wfo);
        wfoRepository.save(wfoEntity);
    }

    @Transactional
    public void deleteWFO(long id) {
        wfoRepository.delete(id);
    }


}
