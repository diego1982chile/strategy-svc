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
public interface WFOMapper {

    @Mappings({
            @Mapping(target = "timeFrame", source = "timeFrame", qualifiedByName = "toTimeFrameEntity"),
            @Mapping(target = "step", source = "step", qualifiedByName = "toStepEntity"),
            @Mapping(target = "status", source = "status", qualifiedByName = "toStatusEntity")
    })
    WFOEntity toEntity(WFO domain);

    @Mappings({
            @Mapping(target = "timeFrame", source = "timeFrame", qualifiedByName = "toTimeFrameDomain"),
            @Mapping(target = "step", source = "step", qualifiedByName = "toStepDomain"),
            @Mapping(target = "status", source = "status", qualifiedByName = "toStatusDomain")
    })
    WFO toDomain(WFOEntity entity);

    @Named("toTimeFrameEntity")
    default long toTimeFrameEntity(TimeFrame timeFrame) {
        return timeFrame.getId();
    }

    @Named("toTimeFrameDomain")
    default TimeFrame toTimeFrameDomain(long id) {
        return TimeFrame.valueOf(id);
    }

    @Named("toStepEntity")
    default long toStepEntity(TimeFrame step) {
        return step.getId();
    }

    @Named("toStepDomain")
    default TimeFrame toStepDomain(long id) {
        return TimeFrame.valueOf(id);
    }

    @Named("toStatusEntity")
    default long toStatusEntity(Status status) {
        return status.getId();
    }

    @Named("toStatusDomain")
    default Status toStatusDomain(long id) {
        return Status.valueOf(id);
    }



}
