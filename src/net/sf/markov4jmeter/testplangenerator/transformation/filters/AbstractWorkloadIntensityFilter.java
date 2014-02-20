package net.sf.markov4jmeter.testplangenerator.transformation.filters;

import net.voorn.markov4jmeter.control.MarkovController;

import org.apache.jorphan.collections.ListedHashTree;

/**
 * This is the base class of all filters which modify a given Test Plan for
 * initializing the workload intensity.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public abstract class AbstractWorkloadIntensityFilter extends AbstractFilter {

    /**
     * Sets a given formula in the (unique) Markov Controller of a Test Plan,
     * for specifying the workload intensity to be emulated by JMeter; if no
     * Markov Controller is available, nothing will be changed.
     *
     * <p>This method gives a warning message, if several Markov Controllers
     * are included in the specified Test Plan; it gives an error message, if
     * no Markov Controller exists.
     *
     * @param testPlan  Test Plan to be modified.
     * @param formula   Formula to be set.
     */
    protected boolean setFormulaInMarkovController (
            final ListedHashTree testPlan,
            final String formula) {

        // to be returned;
        boolean success = false;

        // get (unique) element of specified type;
        // -> gives warning message, if several Markov Controllers exist;
        // -> gives error message, if no Markov Controller exists;
        final MarkovController markovController =
                this.findUniqueTestPlanElement(
                        testPlan,
                        MarkovController.class);

        if (markovController != null) {  // any Markov Controller found?

            markovController.setArrivalCtrlEnabled(true);
            markovController.setArrivalCtrlNumSessions(formula);

            success = true;
        }

        return success;
    }
}
