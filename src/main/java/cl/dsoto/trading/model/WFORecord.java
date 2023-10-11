package cl.dsoto.trading.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by root on 07-12-21.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WFORecord {

    String id;
    int iteration;
    int correlative;
    String strategy;
    String period;
    String stage;
    int numberOfTrades;
    double profitableTradesRatio;
    double rewardRiskRatio;
    double vsBuyAndHoldRatio;
    double cashflow;
    double efficiencyIndex;
    String parameters;


}
