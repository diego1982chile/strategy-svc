package cl.dsoto.trading.services;


import cl.dsoto.trading.clients.BarClient;
import cl.dsoto.trading.clients.EodhdClient;
import cl.dsoto.trading.daos.StockMarketDAO;
import cl.dsoto.trading.entities.WFOEntity;
import cl.dsoto.trading.mappers.StrategyMapper;
import cl.dsoto.trading.mappers.WFOMapper;
import cl.dsoto.trading.model.*;
import cl.dsoto.trading.model.Strategy;
import cl.dsoto.trading.repositories.StrategyRepository;
import cl.dsoto.trading.repositories.WFORepository;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.concurrent.ListenableFuture;
import org.ta4j.core.*;
import org.ta4j.core.analysis.CashFlow;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.uma.jmetal.runner.multiobjective.MOCellStockMarketIntegerRunner;
import ta4jexamples.loaders.CsvTicksLoader;
import ta4jexamples.research.MultipleStrategy;
import ta4jexamples.strategies.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static cl.dsoto.trading.constants.ServiceConstants.*;

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
        WFOEntity wfoEntity = wfoMapper.toEntity(wfo);
        wfoEntity = wfoRepository.save(wfoEntity);
        wfo = wfoMapper.toDomain(wfoEntity);
        return wfo;
    }


    @Async
    @Transactional
    public ListenableFuture<Void> process(WFO wfo) throws Exception {

        wfo.setStatus(Status.PROCESSING);
        WFOEntity wfoEntity = wfoMapper.toEntity(wfo);
        wfoRepository.save(wfoEntity);

        Response response = barClient.getBars(wfo.getTimeFrame(), wfo.getStart().toString(), wfo.getEnd().toString());

        TimeSeries data = mapTimeSeriesFromResponse(response, wfo);

        int correlative = 1;

        for (int k = 0; k < wfo.getIterations(); ++k) {

            test = k + 1;

            List<Strategy> strategies = strategyService.getStrategies(ProblemType.INTEGER);

            //strategies = strategies.stream().filter(strategy -> strategy.getName().equals(MACD)).collect(Collectors.toList());

            for (Strategy strategy : strategies) {

                //int offset = data.getBarCount() / 11;

                int offset = wfo.calculateOffset();
                int trainRange = wfo.calculateTrainRange();
                int testRange = wfo.calculateTestRange();
                int iterations = wfo.calculateIterations();

                for (int i = 0; i <= iterations; ++i) {

                    String nameIn = String.valueOf(data.getBar(i*offset).getBeginTime().getYear());
                    nameIn = nameIn + "_" + String.valueOf(data.getBar((i + trainRange)*offset).getBeginTime().getYear());

                    String nameOut = String.valueOf(data.getBar((i + trainRange)*offset + 1).getBeginTime().getYear());

                    TimeSeries in = new BaseTimeSeries(nameIn,data.getBarData().subList(i*offset,(i + trainRange)*offset));
                    TimeSeries out = new BaseTimeSeries(nameOut,data.getBarData().subList((i + trainRange)*offset + 1,(i + trainRange + testRange)*offset));

                    BackTest period = createFromSeries(in);

                    /*
                    GenerationalGeneticAlgorithmStockMarketIntegerRunner runner =
                            new GenerationalGeneticAlgorithmStockMarketIntegerRunner(strategy.getName(), in, strategy.getVariables());
                    */

                    MOCellStockMarketIntegerRunner runner =
                            new MOCellStockMarketIntegerRunner(strategy.getName(), in, strategy.getVariables());

                    Optimization optimization = runner.run(strategy);
                    period.getOptimizations().add(optimization);
                    updateStrategy(optimization, strategy.getName());

                    //periodManager.persist(period);

                    WFORecord wfoRecordIn = computeResults(test, period, strategy, nameIn, null, "in", correlative);

                    correlative++;

                    period = createFromSeries(out);

                    period.getOptimizations().add(optimization);
                    //updateStrategy(optimization, strategy.getName());

                    //periodManager.persist(period);

                    WFORecord wfoRecordOut = computeResults(test, period, strategy, nameOut, nameIn, "out", correlative);

                    correlative++;

                    wfo.getWfoRecords().add(wfoRecordIn);
                    wfo.getWfoRecords().add(wfoRecordOut);

                }

            /*
            strategies = strategyManager.getBinaryProblemTypeStrategies();

            for (Strategy strategy : strategies) {

                TimeSeries in = new BaseTimeSeries("",data.getBarData().subList(i*offset,(i+4)*offset));
                TimeSeries out = new BaseTimeSeries("",data.getBarData().subList((i+4)*offset + 1,(i+5)*offset));

                Period period = periodManager.createFromSeries(in);

                GenerationalGeneticAlgorithmStockMarketRunner runner =
                        new GenerationalGeneticAlgorithmStockMarketRunner(strategy.getName(), file, strategy.getVariables());
                Optimization optimization = runner.run(strategy);
                optimization.setPeriod(period);
                period.getOptimizations().add(optimization);
            }
            */

                //periodManager.persist(period);
            }

        }

        wfo.setStatus(Status.COMPLETED);
        wfoEntity = wfoMapper.toEntity(wfo);
        wfoRepository.save(wfoEntity);

        updateWFO(wfo);

        return AsyncResult.forValue(null);
    }

    @Transactional
    private void updateWFO(WFO wfo) {
        WFOEntity wfoEntity = wfoMapper.toEntity(wfo);
        wfoRepository.save(wfoEntity);
    }

    private BackTest createFromSeries(TimeSeries timeSeries) throws Exception {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        java.sql.Date start = java.sql.Date.valueOf(timeSeries.getFirstBar().getBeginTime().toLocalDate());
        java.sql.Date end = java.sql.Date.valueOf(timeSeries.getLastBar().getBeginTime().toLocalDate());

        //TODO: Dejar esto parametrico
        TimeFrame timeFrame;

        switch ((int) Duration.between(timeSeries.getBar(0).getBeginTime(), timeSeries.getBar(1).getBeginTime()).getSeconds()) {
            case 2700:
                timeFrame = TimeFrame.HOUR;
                break;
            case 86400:
            case 345600:
            case 259200:
                timeFrame = TimeFrame.DAY;
                break;
            default:
                throw new Exception("unsupported TimeFrame!");
        }

        BackTest backTest = BackTest.builder()
                .name(timeSeries.getName())
                .timestamp(Timestamp.from(Instant.now()))
                .start(start)
                .end(end)
                .bars(timeSeries.getBarData())
                .timeFrame(timeFrame).build();

        return backTest;
    }

    public static void updateStrategy(Optimization optimization, String strategy) throws Exception {
        try {
            switch (strategy) {
                case "GlobalExtremaStrategy":
                    GlobalExtremaStrategy.mapFrom(optimization);
                    break;
                case "TunnelStrategy":
                    TunnelStrategy.mapFrom(optimization);
                    break;
                case "CCICorrectionStrategy":
                    CCICorrectionStrategy.mapFrom(optimization);
                    break;
                case "BagovinoStrategy":
                    BagovinoStrategy.mapFrom(optimization);
                    break;
                case "MovingAveragesStrategy":
                    MovingAveragesStrategy.mapFrom(optimization);
                    break;
                case "RSI2Strategy":
                    RSI2Strategy.mapFrom(optimization);
                    break;
                case "ParabolicSARStrategy":
                    ParabolicSARStrategy.mapFrom(optimization);
                    break;
                case "MovingMomentumStrategy":
                    MovingMomentumStrategy.mapFrom(optimization);
                    break;
                case "StochasticStrategy":
                    StochasticStrategy.mapFrom(optimization);
                    break;
                case "MACDStrategy":
                    MACDStrategy.mapFrom(optimization);
                    break;
                case "FXBootCampStrategy":
                    FXBootCampStrategy.mapFrom(optimization);
                    break;
                case "WinslowStrategy":
                    WinslowStrategy.mapFrom(optimization);
                    break;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private WFORecord computeResults(int iteration, BackTest selected, cl.dsoto.trading.model.Strategy strategy,
                                       String currentPeriod, String previousPeriod, String stage, int correlative) {

        try {

            TimeSeries timeSeries = new BaseTimeSeries(selected.getName());

            for (Bar bar : selected.getBars()) {
                timeSeries.addBar(bar);
            }

            String START = DateTimeFormatter.ISO_LOCAL_DATE.format(timeSeries.getFirstBar().getEndTime());
            String END = DateTimeFormatter.ISO_LOCAL_DATE.format(timeSeries.getLastBar().getEndTime());

            TimeSeriesManager seriesManager = new TimeSeriesManager(timeSeries);

            List<org.ta4j.core.Strategy> strategies = mapFrom(selected);

            MultipleStrategy multipleStrategy = new MultipleStrategy(strategies);

            TradingRecord tradingRecord = seriesManager.run(multipleStrategy.buildStrategy(timeSeries));

            System.out.println("Number of trades for our strategy: " + tradingRecord.getTradeCount());
            int NUMBER_OF_TRADES = tradingRecord.getTradeCount();

            // Analysis

            // Getting the cash flow of the resulting trades
            CashFlow cashFlow = new CashFlow(timeSeries, tradingRecord);

            // Getting the profitable trades ratio
            AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
            System.out.println("Profitable trades ratio: " + profitTradesRatio.calculate(timeSeries, tradingRecord));

            double PROFIT_TRADES_RATIO = profitTradesRatio.calculate(timeSeries, tradingRecord);

            // Getting the reward-risk ratio
            AnalysisCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
            System.out.println("Reward-risk ratio: " + rewardRiskRatio.calculate(timeSeries, tradingRecord));

            double REWARD_RISK_RATIO = rewardRiskRatio.calculate(timeSeries, tradingRecord);

            // Total profit of our strategy
            // vs total profit of a buy-and-hold strategy
            AnalysisCriterion vsBuyAndHold = new VersusBuyAndHoldCriterion(new TotalProfitCriterion());
            System.out.println("Our profit vs buy-and-hold profit: " + vsBuyAndHold.calculate(timeSeries, tradingRecord));

            double VS_BUY_AND_HOLD_RATIO = vsBuyAndHold.calculate(timeSeries, tradingRecord);

            for (int i = 0; i < tradingRecord.getTrades().size(); ++i) {
                System.out.println("Trade[" + i + "]: " + tradingRecord.getTrades().get(i).toString());
            }

            for (int i = 0; i < cashFlow.getSize(); ++i) {
                System.out.println("CashFlow[" + i + "]: " + cashFlow.getValue(i));
                //getCashFlowDetailView().append("CashFlow["+ i +"]: " + cashFlow.getValue(i));
                //getCashFlowDetailView().append(newline);
            }

            String pattern = "###.#####";
            DecimalFormat decimalFormat = new DecimalFormat(pattern);

            double CASHFLOW = cashFlow.getValue(cashFlow.getSize()-1).doubleValue();

            selected.getTimestamp().toString();

            //Chart
            /*
            JFreeChart jfreechart = BuyAndSellSignalsToChart.buildCandleStickChart(timeSeries, multipleStrategy.buildStrategy(timeSeries));
            ChartPanel panel = new ChartPanel(jfreechart);
            panel.setFillZoomRectangle(true);
            panel.setMouseWheelEnabled(true);
            panel.setPreferredSize(new Dimension(800, 200));

            BuyAndSellSignalsToChart.buildCandleStickChart(timeSeries, multipleStrategy.buildStrategy(timeSeries));
            */

            if(stage.equals("in")) {
                efficiency.put(currentPeriod, (CASHFLOW - 1)*100);
                cont++;
                return WFORecord.builder()
                        .iteration(iteration)
                        .correlative(correlative)
                        .strategy(strategy.getName())
                        .period(currentPeriod)
                        .stage(stage)
                        .numberOfTrades(NUMBER_OF_TRADES)
                        .profitableTradesRatio(PROFIT_TRADES_RATIO)
                        .rewardRiskRatio(REWARD_RISK_RATIO)
                        .vsBuyAndHoldRatio(VS_BUY_AND_HOLD_RATIO)
                        .cashflow(CASHFLOW)
                        .efficiencyIndex(-1)
                        .parameters(mapStrategiesFrom(selected).toString())
                        .build();
            }
            else {
                double efficiencyRatio = ((CASHFLOW -1)*100) / efficiency.get(previousPeriod);
                cont++;
                return WFORecord.builder()
                        .iteration(iteration)
                        .correlative(correlative)
                        .strategy(strategy.getName())
                        .period(currentPeriod)
                        .stage(stage)
                        .numberOfTrades(NUMBER_OF_TRADES)
                        .profitableTradesRatio(PROFIT_TRADES_RATIO)
                        .rewardRiskRatio(REWARD_RISK_RATIO)
                        .vsBuyAndHoldRatio(VS_BUY_AND_HOLD_RATIO)
                        .cashflow(CASHFLOW)
                        .efficiencyIndex(efficiencyRatio)
                        .parameters(mapStrategiesFrom(selected).toString())
                        .build();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<org.ta4j.core.Strategy> mapFrom(BackTest backTest) throws Exception {

        List<org.ta4j.core.Strategy> strategies = new ArrayList<>();

        TimeSeries series = new BaseTimeSeries(backTest.getName());

        for (Bar periodBar : backTest.getBars()) {
            series.addBar(periodBar);
        }

        for (Optimization optimization : backTest.getOptimizationsOfType(ProblemType.INTEGER)) {
            switch (optimization.getStrategy().getName()) {
                case GLOBAL_EXTREMA:
                    GlobalExtremaStrategy.mapFrom(optimization);
                    strategies.add(GlobalExtremaStrategy.buildStrategy(series));
                    break;
                case TUNNEL:
                    TunnelStrategy.mapFrom(optimization);
                    strategies.add(TunnelStrategy.buildStrategy(series));
                    break;
                case CCI_CORRECTION:
                    CCICorrectionStrategy.mapFrom(optimization);
                    strategies.add(CCICorrectionStrategy.buildStrategy(series));
                    break;
                case BAGOVINO:
                    BagovinoStrategy.mapFrom(optimization);
                    strategies.add(BagovinoStrategy.buildStrategy(series));
                    break;
                case MOVING_AVERAGES:
                    MovingAveragesStrategy.mapFrom(optimization);
                    strategies.add(MovingAveragesStrategy.buildStrategy(series));
                    break;
                case RSI_2:
                    RSI2Strategy.mapFrom(optimization);
                    strategies.add(RSI2Strategy.buildStrategy(series));
                    break;
                case PARABOLIC_SAR:
                    ParabolicSARStrategy.mapFrom(optimization);
                    strategies.add(ParabolicSARStrategy.buildStrategy(series));
                    break;
                case MOVING_MOMENTUM:
                    MovingMomentumStrategy.mapFrom(optimization);
                    strategies.add(MovingMomentumStrategy.buildStrategy(series));
                    break;
                case STOCHASTIC:
                    StochasticStrategy.mapFrom(optimization);
                    strategies.add(StochasticStrategy.buildStrategy(series));
                    break;
                case MACD:
                    MACDStrategy.mapFrom(optimization);
                    strategies.add(MACDStrategy.buildStrategy(series));
                    break;
                case FX_BOOTCAMP:
                    FXBootCampStrategy.mapFrom(optimization);
                    strategies.add(FXBootCampStrategy.buildStrategy(series));
                    break;
                case WINSLOW:
                    WinslowStrategy.mapFrom(optimization);
                    strategies.add(WinslowStrategy.buildStrategy(series));
                    break;
            }
        }

        for (Optimization optimization : backTest.getOptimizationsOfType(ProblemType.BINARY)) {

            strategies.clear();

            for (Solution solution : optimization.getSolutions()) {
                for (int i = 0; i < solution.getValues().size(); i++) {
                    boolean value = (Boolean) solution.getValues().get(i);

                    if (value) {

                        switch (i) {
                            case 0:
                                strategies.add(CCICorrectionStrategy.buildStrategy(series));
                                break;
                            case 1:
                                strategies.add(GlobalExtremaStrategy.buildStrategy(series));
                                break;
                            case 2:
                                strategies.add(MovingMomentumStrategy.buildStrategy(series));
                                break;
                            case 3:
                                strategies.add(RSI2Strategy.buildStrategy(series));
                                break;
                            case 4:
                                strategies.add(MACDStrategy.buildStrategy(series));
                                break;
                            case 5:
                                strategies.add(StochasticStrategy.buildStrategy(series));
                                break;
                            case 6:
                                strategies.add(ParabolicSARStrategy.buildStrategy(series));
                                break;
                            case 7:
                                strategies.add(MovingAveragesStrategy.buildStrategy(series));
                                break;
                            case 8:
                                strategies.add(BagovinoStrategy.buildStrategy(series));
                                break;
                            case 9:
                                strategies.add(FXBootCampStrategy.buildStrategy(series));
                                break;
                            case 10:
                                strategies.add(TunnelStrategy.buildStrategy(series));
                                break;
                            case 11:
                                strategies.add(WinslowStrategy.buildStrategy(series));
                                break;
                        }
                    }
                }
            }
        }

        return strategies;
    }

    public static Map<String, List<Pair<String, Integer>>> mapStrategiesFrom(BackTest period) {

        Map<String, List<Pair<String, Integer>>> parameters = new HashMap<>();

        for (Optimization optimization : period.getOptimizationsOfType(ProblemType.INTEGER)) {
            switch (optimization.getStrategy().getName()) {
                case GLOBAL_EXTREMA:
                    parameters.put(GLOBAL_EXTREMA, GlobalExtremaStrategy.getParameters());
                    break;
                case TUNNEL:
                    parameters.put(TUNNEL, TunnelStrategy.getParameters());
                    break;
                case CCI_CORRECTION:
                    parameters.put(CCI_CORRECTION, CCICorrectionStrategy.getParameters());
                    break;
                case BAGOVINO:
                    parameters.put(BAGOVINO, BagovinoStrategy.getParameters());
                    break;
                case MOVING_AVERAGES:
                    parameters.put(MOVING_AVERAGES, MovingAveragesStrategy.getParameters());
                    break;
                case RSI_2:
                    parameters.put(RSI_2, RSI2Strategy.getParameters());
                    break;
                case PARABOLIC_SAR:
                    parameters.put(PARABOLIC_SAR, ParabolicSARStrategy.getParameters());
                    break;
                case MOVING_MOMENTUM:
                    parameters.put(MOVING_MOMENTUM, MovingMomentumStrategy.getParameters());
                    break;
                case STOCHASTIC:
                    parameters.put(STOCHASTIC, StochasticStrategy.getParameters());
                    break;
                case MACD:
                    parameters.put(MACD, MACDStrategy.getParameters());
                    break;
                case FX_BOOTCAMP:
                    parameters.put(FX_BOOTCAMP, FXBootCampStrategy.getParameters());
                    break;
                case WINSLOW:
                    parameters.put(WINSLOW, WinslowStrategy.getParameters());
                    break;
            }
        }

        return parameters;
    }

    private TimeSeries mapTimeSeriesFromResponse(Response response, WFO wfo) {

        JsonArray jsonArray = response.readEntity(JsonArray.class);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        TimeSeries data = new BaseTimeSeries(wfo.getName());

        for (JsonValue jsonValue : jsonArray) {

            //public BaseBar(Duration timePeriod, ZonedDateTime endTime, Decimal openPrice, Decimal highPrice, Decimal lowPrice, Decimal closePrice, Decimal volume, Decimal amount) {

            JsonObject jsonObject = jsonValue.asJsonObject();

            Duration timePeriod = Duration.parse(jsonObject.getString("timePeriod"));
            ZonedDateTime endTime = ZonedDateTime.parse(jsonObject.getString("endTime"));
            Decimal openPrice = Decimal.valueOf(jsonObject.getJsonNumber("openPrice").bigDecimalValue());
            Decimal maxPrice = Decimal.valueOf(jsonObject.getJsonNumber("maxPrice").bigDecimalValue());
            Decimal minPrice = Decimal.valueOf(jsonObject.getJsonNumber("minPrice").bigDecimalValue());
            Decimal closePrice = Decimal.valueOf(jsonObject.getJsonNumber("closePrice").bigDecimalValue());
            Decimal volume = Decimal.valueOf(jsonObject.getJsonNumber("volume").bigDecimalValue());
            Decimal amount = Decimal.valueOf(jsonObject.getJsonNumber("amount").bigDecimalValue());

            data.addBar(new BaseBar(timePeriod, endTime, openPrice, maxPrice, minPrice, closePrice, volume, amount));
        }

        return data;
    }

}
