package cl.dsoto.trading.mappers;


import cl.dsoto.trading.entities.*;
import cl.dsoto.trading.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.Decimal;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 05-05-23.
 */
@Mapper(componentModel = "cdi")
public interface StrategyMapper {

    @Mappings({
            @Mapping(target = "problemType", source = "problemType", qualifiedByName = "toProblemTypeEntity")
    })
    StrategyEntity toEntity(Strategy domain);

    @Mappings({
            @Mapping(target = "problemType", source = "problemType", qualifiedByName = "toProblemTypeDomain")
    })
    Strategy toDomain(StrategyEntity entity);

    @Named("toProblemTypeEntity")
    default long toProblemTypeEntity(ProblemType problemType) {
        return problemType.getId();
    }

    @Named("toProblemTypeDomain")
    default ProblemType toProblemTypeDomain(long id) {
        return ProblemType.valueOf(id);
    }

}
