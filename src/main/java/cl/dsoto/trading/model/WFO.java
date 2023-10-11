package cl.dsoto.trading.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 03-10-23.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WFO {

    /** El identificador único de la entidad, inicialmente fijado en <code>NON_PERSISTED_ID</code>. */
    private Long id;

    String name;

    TimeFrame timeFrame;

    TimeFrame step;

    Integer iterations;

    LocalDate start;
    LocalDate end;

    Double inSample;
    Double outSample;

    Status status;

    List<WFORecord> wfoRecords = new ArrayList();

    public int calculateOffset() throws Exception {

        int offSet = 0;
        Duration duration = Duration.between(start, end);
        int daysPerYear = 365;
        long range = duration.toDays() / daysPerYear;

        switch (step) {
            case YEAR:
                switch (timeFrame) {
                    case DAY:
                        offSet = 5*4*12;
                        break;
                    case HOUR:
                        offSet = 5*4*12*24;
                        break;
                    default:
                        throw new Exception("Unsupported combination of step/timeframe!");
                }
                break;
            case MONTH:
                switch (timeFrame) {
                    case DAY:
                        offSet = 5*4;
                        break;
                    case HOUR:
                        offSet = 5*4*24;
                        break;
                    default:
                        throw new Exception("Unsupported combination of step/timeframe!");
                }
                break;
            case WEEK:
                switch (timeFrame) {
                    case DAY:
                        offSet = 5;
                        break;
                    case HOUR:
                        offSet = 5*24;
                        break;
                    default:
                        throw new Exception("Unsupported combination of step/timeframe!");
                }
                break;
            case DAY:
                switch (timeFrame) {
                    case DAY:
                        offSet = 1;
                        break;
                    case HOUR:
                        offSet = 24;
                        break;
                    default:
                        throw new Exception("Unsupported combination of step/timeframe!");
                }
                break;

        }

        return (int) (offSet * range * inSample);
    }


    public int calculateTrainRange() throws Exception {
        Duration duration = Duration.between(start, end);
        int daysPerYear = 365;
        long range = duration.toDays() / daysPerYear;
        return (int) (range * inSample);
    }

    public int calculateTestRange() throws Exception {
        Duration duration = Duration.between(start, end);
        int daysPerYear = 365;
        long range = duration.toDays() / daysPerYear;
        return (int) (range * outSample);
    }

    public int calculateIterations() {
        Duration duration = Duration.between(start, end);
        int daysPerYear = 365;
        long range = duration.toDays() / daysPerYear;
        return (int) ((1 - (inSample + outSample)) * range);
    }

}
