package cl.dsoto.trading.mappers;


import cl.dsoto.trading.entities.*;
import cl.dsoto.trading.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.ta4j.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by root on 05-05-23.
 */
@Mapper(componentModel = "cdi")
public interface BackTestMapper {

    @Mappings({
            @Mapping(target = "bars", source = "bars", qualifiedByName = "toBarEntity"),
            @Mapping(target = "optimizations", source = "optimizations", qualifiedByName = "toOptimizationEntity"),
            @Mapping(target = "timeFrame", source = "timeFrame", qualifiedByName = "toTimeFrameEntity")
    })
    BackTestEntity toEntity(BackTest domain);

    @Mappings({
            @Mapping(target = "bars", source = "bars", qualifiedByName = "toBarDomain"),
            @Mapping(target = "optimizations", source = "optimizations", qualifiedByName = "toOptimizationDomain"),
            @Mapping(target = "timeFrame", source = "timeFrame", qualifiedByName = "toTimeFrameDomain")
    })
    BackTest toDomain(BackTestEntity entity);

    @Named("toBarEntity")
    default BarEntity toBarEntity(Bar bar) {
        return  BarEntity.builder()
                .open(bar.getOpenPrice().doubleValue())
                .close(bar.getOpenPrice().doubleValue())
                .low(bar.getMinPrice().doubleValue())
                .high(bar.getMaxPrice().doubleValue())
                .volume(bar.getVolume().doubleValue())
                .beginTime(bar.getBeginTime())
                .endTime(bar.getEndTime())
                .build();
    }

    @Named("toBarDomain")
    default Bar toBarDomain(BarEntity bar) {
        return new BaseBar(
                bar.getEndTime(),
                Decimal.valueOf(bar.getOpen()),
                Decimal.valueOf(bar.getHigh()),
                Decimal.valueOf(bar.getLow()),
                Decimal.valueOf(bar.getClose()),
                Decimal.valueOf(bar.getVolume()));
    }

    @Named("toOptimizationEntity")
    default OptimizationEntity toOptimizationEntity(Optimization optimization) {

        List<ObjectiveEntity> objectives = new ArrayList<>();

        for (Objective objective : optimization.getObjectives()) {
            ObjectiveEntity objectiveEntity = ObjectiveEntity.builder().objective(objective.getObjective()).build();
            objectives.add(objectiveEntity);
        }

        List<SolutionEntity> solutions = new ArrayList<>();

        for (Solution solution : optimization.getSolutions()) {
            SolutionEntity solutionEntity = SolutionEntity.builder()
                    .values((List<ValueEntity>) solution.getValues().stream().map(value -> {
                        return ValueEntity.builder().value(value.toString()).build();
                    }).collect(Collectors.toList()))
                    .build();
            solutions.add(solutionEntity);
        }

        StrategyEntity strategy = StrategyEntity.builder()
                .name(optimization.getStrategy().getName())
                .problemType((int) optimization.getStrategy().getProblemType().getId())
                .variables(optimization.getStrategy().getVariables())
                .build();         

        return  OptimizationEntity.builder()
                .objectives(objectives)
                .solutions(solutions)
                .strategy(strategy)
                .timestamp(optimization.getTimestamp())
                .build();         
    }

    @Named("toOptimizationDomain")
    default Optimization toOptimizationDomain(OptimizationEntity optimization) {
        List<Objective> objectives = new ArrayList<>();

        for (ObjectiveEntity objective : optimization.getObjectives()) {
            Objective objectiveDomain = Objective.builder().objective(objective.getObjective()).build();
            objectives.add(objectiveDomain);
        }

        List<Solution> solutions = new ArrayList<Solution>();

        for (SolutionEntity solution : optimization.getSolutions()) {
            Solution<Comparable> solutionDomain = Solution.builder().values(solution.getValues().stream().map(valueEntity -> {
                int problemType = optimization.getStrategy().getProblemType();

                if (problemType == ProblemType.INTEGER.getId()) {
                    return Integer.parseInt(valueEntity.getValue());
                }
                else if (problemType == ProblemType.BINARY.getId()) {
                    return Boolean.parseBoolean(valueEntity.getValue());
                }
                else {
                    return Double.parseDouble(valueEntity.getValue());
                }
            }).collect(Collectors.toList())).build();

            solutions.add(solutionDomain);
        }

        cl.dsoto.trading.model.Strategy strategy = cl.dsoto.trading.model.Strategy.builder()
                .name(optimization.getStrategy().getName())
                .problemType(ProblemType.valueOf(optimization.getStrategy().getProblemType()))
                .variables(optimization.getStrategy().getVariables())
                .build();

        return  Optimization.builder()
                .objectives(objectives)
                .solutions(solutions)
                .strategy(strategy)
                .timestamp(optimization.getTimestamp())
                .build();
    }

    @Named("toTimeFrameEntity")
    default long toTimeFrameEntity(TimeFrame timeFrame) {
        return timeFrame.getId();
    }

    @Named("toTimeFrameDomain")
    default TimeFrame toTimeFrameDomain(long id) {
        return TimeFrame.valueOf(id);
    }

}
