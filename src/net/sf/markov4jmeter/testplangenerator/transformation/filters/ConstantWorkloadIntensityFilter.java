package net.sf.markov4jmeter.testplangenerator.transformation.filters;

import m4jdsl.WorkloadModel;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;

import org.apache.jorphan.collections.ListedHashTree;

/**
 * This filter initializes a constant number of users as workload intensity in
 * the Markov Controller of a given Test Plan; the number of users is taken from
 * the M4J-DSL workload model to be provided.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class ConstantWorkloadIntensityFilter
extends AbstractWorkloadIntensityFilter {

    @Override
    public ListedHashTree modifyTestPlan(
            final ListedHashTree testPlan,
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory) {

        final String formula =
                workloadModel.getWorkloadIntensity().getFormula();

        // ignore returned success flag;
        this.setFormulaInMarkovController(testPlan, formula);

        return testPlan;
    }
}
