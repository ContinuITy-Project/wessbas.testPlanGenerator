package net.sf.markov4jmeter.testplangenerator.transformation.filters;

import m4jdsl.BehaviorMix;
import m4jdsl.BehaviorModel;
import m4jdsl.RelativeFrequency;
import m4jdsl.WorkloadModel;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.voorn.markov4jmeter.control.MarkovController;

import org.apache.jorphan.collections.ListedHashTree;

/**
 * This filter installs the Behavior Mix provided by a workload model in a
 * given Test Plan. The related values will be written into the Markov
 * Controller of the given Test Plan, including the name, relative frequency
 * and filename of each Behavior Model. Furthermore, each Behavior Model will
 * be written into a (CSV) file which is specified through the model-related
 * filename.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class BehaviorMixFilter extends AbstractFilter {

    /** Informational message for the case that a Behavior Model has been
     *  written to file. */
    private final static String INFO_BEHAVIOR_MODEL_WRITTEN_TO_FILE =
            "Behavior Model \"%s\" written to file \"%s\".";


    @Override
    public ListedHashTree modifyTestPlan (
            final ListedHashTree testPlan,
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory) {

        // findUniqueTestPlanElement() returns null if no controller is
        // available, and it gives a warning in case of ambiguous controllers;
        final MarkovController markovController =
                this.findUniqueTestPlanElement(
                        testPlan,
                        MarkovController.class);

        if (markovController == null) {

            return null;
        }

        this.installBehaviorMix(
                markovController,
                workloadModel.getBehaviorMix());

        return testPlan;
    }

    /**
     * Installs the Behavior Mix values in a given Markov Controller.
     *
     * <p>The Markov Controller retrieves the name, relative frequency and
     * filename of each Behavior Model. Additionally, each Behavior Model matrix
     * will be written into the (CSV) file which is specified through the
     * model-related filename.
     *
     * @param markovController
     *     Markov Controller which retrieves the name, relative frequency and
     *     filename of each Behavior Model.
     * @param behaviorMix
     *     Behavior Mix which provides the information to be stored in the
     *     Markov Controller.
     */
    private void installBehaviorMix (
            final MarkovController markovController,
            final BehaviorMix behaviorMix) {

        final net.voorn.markov4jmeter.control.BehaviorMix mcBehaviorMix =
                new net.voorn.markov4jmeter.control.BehaviorMix();

        for (final RelativeFrequency relativeFrequency :
            behaviorMix.getRelativeFrequencies()) {

            final BehaviorModel behaviorModel =
                    relativeFrequency.getBehaviorModel();

            net.voorn.markov4jmeter.control.BehaviorMixEntry behaviorMixEntry =
                    new net.voorn.markov4jmeter.control.BehaviorMixEntry(
                            behaviorModel.getName(),
                            relativeFrequency.getValue(),
                            behaviorModel.getFilename());

            this.writeBehaviorModel(behaviorModel);

            mcBehaviorMix.addBehaviorEntry(behaviorMixEntry);
        }

        markovController.setBehaviorMix(mcBehaviorMix);
    }

    /**
     * Write a given Behavior Model into a (CSV) file.
     *
     * @param behaviorModel  Behavior Model to be written into a (CSV) file.
     */
    private void writeBehaviorModel (final BehaviorModel behaviorModel) {

        final String name = behaviorModel.getName();
        final String filename = behaviorModel.getFilename();


        // TODO: the writing routine needs to be taken from the Behavior Model Extraction Tool.


        // print confirmation message for successful writing;
        final String message = String.format(
                BehaviorMixFilter.INFO_BEHAVIOR_MODEL_WRITTEN_TO_FILE,
                name,
                filename);
        // "0    [main] INFO  ansformation.filters.AbstractFilter " ?
        //        BehaviorMixFilter.LOG.info(message);
        System.out.println(message);
    }
}
