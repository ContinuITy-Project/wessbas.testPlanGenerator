package net.sf.markov4jmeter.testplangenerator.transformation.filters;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import m4jdsl.ApplicationState;
import m4jdsl.BehaviorMix;
import m4jdsl.BehaviorModel;
import m4jdsl.BehaviorModelExitState;
import m4jdsl.BehaviorModelState;
import m4jdsl.MarkovState;
import m4jdsl.RelativeFrequency;
import m4jdsl.Service;
import m4jdsl.SessionLayerEFSM;
import m4jdsl.ThinkTime;
import m4jdsl.Transition;
import m4jdsl.WorkloadModel;
import m4jdsl.impl.NormallyDistributedThinkTimeImpl;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.TransformationException;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.thinktimes.AbstractThinkTimeFormatter;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.thinktimes.NormallyDistributedThinkTimeFormatter;
import net.sf.markov4jmeter.testplangenerator.util.CSVHandler;
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

    /** Name of the exit state = "<code>$</code>". */
    private final static String EXIT_STATE_NAME = "$";

    /** Appendix for the name of an initial state = "<code>*</code>". */
    private final static String INITIAL_STATE_APPENDIX = "*";

    /** Informational message for the case that a Behavior Model has been
     *  written to file. */
    private final static String INFO_BEHAVIOR_MODEL_WRITTEN_TO_FILE =
            "Behavior Model \"%s\" has been written to file \"%s\".";

    /** Error message for the case that a Behavior Model could not be written
     *  to file successfully. */
    private final static String ERROR_BEHAVIOR_MODEL_WRITE_FAILURE =
            "Behavior Model \"%s\" could not be written to file \"%s\" (%s)";

    private final static String ERROR_UNKNOWN_THINK_TIME_TYPE =
            "unknown think time type detected: %s";

    /** <code>ThinkTime</code> Formatters registry. */
    private final static
    HashMap<Class<? extends ThinkTime>, AbstractThinkTimeFormatter>
    THINK_TIME_FORMATTERS;

    static {

        // register all available ThinkTime Formatters;
        THINK_TIME_FORMATTERS = new HashMap<Class<? extends ThinkTime>, AbstractThinkTimeFormatter>();

        THINK_TIME_FORMATTERS.put(NormallyDistributedThinkTimeImpl.class,
                                  new NormallyDistributedThinkTimeFormatter());
    }


    /* *************************  global variables  ************************* */


    /** Instance for reading and writing CSV files. */
    private final CSVHandler csvHandler;

    /** Output path for Behavior Model files. */
    private final String behaviorModelsOutputPath;


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for a Behavior Mix Filter.
     *
     * @param csvHandler
     *     instance for reading and writing CSV files.
     * @param behaviorModelsOutputPath
     *     output path for Behavior Model files.
     */
    public BehaviorMixFilter (
            final CSVHandler csvHandler,
            final String behaviorModelsOutputPath) {

        this.csvHandler = csvHandler;
        this.behaviorModelsOutputPath = behaviorModelsOutputPath;
    }


    /* **************************  public methods  ************************** */


    @Override
    public ListedHashTree modifyTestPlan (
            final ListedHashTree testPlan,
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

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
                workloadModel);

        return testPlan;
    }


    /* **************************  private methods  ************************* */


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
     * @param workloadModel
     *     Workload Model which provides the Behavior Mix including the Behavior
     *     Models to be stored as files and installed in the Markov Controller;
     *     furthermore, it provides the Session Layer EFSM including all
     *     services which need to be included to each Markov chain.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    private void installBehaviorMix (
            final MarkovController markovController,
            final WorkloadModel workloadModel) throws TransformationException {

        final net.voorn.markov4jmeter.control.BehaviorMix mcBehaviorMix =
                new net.voorn.markov4jmeter.control.BehaviorMix();

        final BehaviorMix behaviorMix = workloadModel.getBehaviorMix();

        // model has been successfully validated -> BM with initial state exists
        // -> BM has at least one transition (possibly to the exit state);
        final Class<? extends ThinkTime> thinkTimeType =
                behaviorMix.getRelativeFrequencies().get(0).getBehaviorModel().
                getInitialState().getOutgoingTransitions().get(0).
                getThinkTime().getClass();

        final AbstractThinkTimeFormatter thinkTimeFormatter =
                BehaviorMixFilter.THINK_TIME_FORMATTERS.get(
                        thinkTimeType);

        if (thinkTimeFormatter == null) {

            final String message = String.format(
                    BehaviorMixFilter.ERROR_UNKNOWN_THINK_TIME_TYPE,
                    thinkTimeType);

            throw new TransformationException(message);
        }

        for (final RelativeFrequency relativeFrequency :
            behaviorMix.getRelativeFrequencies()) {

            final BehaviorModel behaviorModel =
                    relativeFrequency.getBehaviorModel();

            final String fullFilePath = this.getFullFilePath(
                    this.behaviorModelsOutputPath,
                    behaviorModel.getFilename());

            net.voorn.markov4jmeter.control.BehaviorMixEntry behaviorMixEntry =
                    new net.voorn.markov4jmeter.control.BehaviorMixEntry(
                            behaviorModel.getName(),
                            relativeFrequency.getValue(),
                            fullFilePath);

            this.writeBehaviorModel(
                    behaviorModel,
                    workloadModel.getApplicationModel().getSessionLayerEFSM(),
                    thinkTimeFormatter);

            mcBehaviorMix.addBehaviorEntry(behaviorMixEntry);
        }

        markovController.setBehaviorMix(mcBehaviorMix);
    }

    /**
     * Returns the full file path for a parent/child pair.
     *
     * <p> The file path is either canonical or, if not available, absolute.
     *
     * @param parent
     *     the parent file path to the given child (file).
     * @param child
     *     the child (file) of the full path to be returned.
     *
     * @return
     *     a valid <code>String</code> denoting a full file path for the given
     *     parent/child pair.
     */
    private String getFullFilePath (final String parent, final String child) {

        String fullPath;  // to be returned;

        try {

            // might throw a NullPointerException;
            final File file = new File(parent, child);

            try {

                // might throw an IO- or SecurityException;
                fullPath = file.getCanonicalPath();

            } catch (final IOException|SecurityException ex) {

                fullPath = file.getAbsolutePath();
            }

        } catch (final NullPointerException ex) {

            fullPath = child;
        }

        return fullPath;
    }

    /**
     * Write a given Behavior Model into a (CSV) file.
     *
     * @param behaviorModel
     *     Behavior Model to be written into a (CSV) file.
     * @param sessionLayerEFSM
     *     Session Layer EFSM providing all services which need to be included
     *     to the Markov chain of the Behavior Model.
     * @param thinkTimeFormatter
     *     instance for formatting <code>ThinkTime</code> instances.
     */
    private void writeBehaviorModel (
            final BehaviorModel behaviorModel,
            final SessionLayerEFSM sessionLayerEFSM,
            final AbstractThinkTimeFormatter thinkTimeFormatter) {

        final String name     = behaviorModel.getName();
        final String filename = behaviorModel.getFilename();

        final String[][] values = this.buildMatrix(
                behaviorModel,
                sessionLayerEFSM,
                thinkTimeFormatter);

        try {

            final String fullFilePath = this.getFullFilePath(
                    this.behaviorModelsOutputPath,
                    filename);

            // might throw a FileNotFound-, IO-, Security- or
            // NullPointerException;
            this.csvHandler.writeValues(fullFilePath, values);

            // print confirmation message for successful writing; might throw
            // an IllegalFormat- or NullPointerException (never happens here);
            final String message = String.format(
                    BehaviorMixFilter.INFO_BEHAVIOR_MODEL_WRITTEN_TO_FILE,
                    name,
                    fullFilePath);

            System.out.println(message);

        } catch (final Exception ex) {

            // print failure message; might throw an IllegalFormat- or
            // NullPointerException (never happens here);
            final String message = String.format(
                    BehaviorMixFilter.ERROR_BEHAVIOR_MODEL_WRITE_FAILURE,
                    name,
                    filename,
                    ex.getMessage());

            System.err.println(message);
        }
    }

    /**
     * Builds the probability matrix of a given Behavior Model.
     *
     * @param behaviorModel
     *     Behavior Model whose probability matrix shall be built.
     * @param sessionLayerEFSM
     *     Session Layer EFSM providing all services which need to be included
     *     to the Markov chain of the Behavior Model.
     * @param thinkTimeFormatter
     *     instance for formatting <code>ThinkTime</code> instances.
     *
     * @return
     *     a valid <code>String</code> array representing a probability matrix.
     */
    private String[][] buildMatrix (
            final BehaviorModel behaviorModel,
            final SessionLayerEFSM sessionLayerEFSM,
            final AbstractThinkTimeFormatter thinkTimeFormatter) {

        final LinkedList<Service> services =
                this.collectServices(sessionLayerEFSM.getApplicationStates());

        final String initialName =
                sessionLayerEFSM.getInitialState().getService().getName();

        final int n = services.size();
        final String[][] values = new String[n + 1][];

        values[0] = this.buildHeaderRow(services);

        int i = 1;
        for (final Service service : services) {

            final String[] row = this.buildRow(
                    service,
                    services,
                    behaviorModel.getMarkovStates(),
                    initialName,
                    thinkTimeFormatter);

            values[i++] = row;
        }

        return values;
    }

    /**
     * Collects all services which are associated with application states.
     *
     * @param applicationStates
     *     application states providing associated services.
     *
     * @return
     *     all services which are associated with the given application states.
     */
    private LinkedList<Service> collectServices (
            final List<ApplicationState> applicationStates) {

        // to be returned;
        final LinkedList<Service> services = new LinkedList<Service>();

        for (final ApplicationState applicationState : applicationStates) {

            services.add(applicationState.getService());
        }

        return services;
    }

    /**
     * Builds the header row of a probability matrix.
     *
     * @param services
     *     services whose names will be put into the header row.
     *
     * @return
     *     a matrix header row with the names of the given services and the
     *     exit state.
     */
    private String[] buildHeaderRow (final List<Service> services) {

        final int n = services.size();

        // reserve two slots for first column (empty) and exit state ($);
        final String[] header = new String[n + 2];

        // first column is empty;
        header[0] = "";

        int i = 1;
        for (final Service service : services) {

            header[i++] = service.getName();
        }

        // last column contains the exit state ($);
        header[i] = BehaviorMixFilter.EXIT_STATE_NAME;

        return header;
    }

    /**
     * Builds the row of a probability matrix.
     *
     * @param service
     *     service which is associated with the row to be built.
     * @param services
     *     services of the matrix headers.
     * @param markovStates
     *     Markov States with their outgoing transitions providing the
     *     probabilities and think times.
     * @param initialName
     *     name of the service which is associated with the initial state of
     *     the Session Layer EFSM.
     * @param thinkTimeFormatter
     *     instance for formatting <code>ThinkTime</code> instances.
     *
     * @return
     *     the matrix row which is associated with the given service.
     */
    private String[] buildRow (
            final Service service,
            final List<Service> services,
            final List<MarkovState> markovStates,
            final String initialName,
            final AbstractThinkTimeFormatter thinkTimeFormatter) {

        final int n = services.size();

        // reserve two slots for first column (header) and exit state ($);
        final String[] row = new String[n + 2];

        row[0] = this.getHeaderName(service, initialName);

        int i = 1;
        for (final Service serviceColumn : services) {

            row[i++] = this.getValue(
                    service,
                    serviceColumn,
                    markovStates,
                    thinkTimeFormatter);
        }

        // parameter "null"  ->  target state = exit state;
        row[i] = this.getValue(service, null, markovStates, thinkTimeFormatter);

        return row;
    }

    /**
     * Returns the name of a given service, possibly marked as "initial name"
     * if the name matches the name of a service which is associated with an
     * initial state.
     *
     * @see #INITIAL_STATE_APPENDIX
     *
     * @param service
     *     service whose name shall be returned.
     * @param initialName
     *     name of a service which is associated with an initial state.
     *
     * @return
     *     the name of the given service, possibly marked as "initial name".
     */
    private String getHeaderName (
            final Service service,
            final String initialName) {

        String name = service.getName();

        if ( name.equals(initialName) ) {

            name += BehaviorMixFilter.INITIAL_STATE_APPENDIX;
        }

        return name;
    }

    /**
     * Returns the entry of a probability matrix for a given row/column
     * position.
     *
     * @param serviceRow
     *     service of the current matrix row.
     * @param serviceColumn
     *     service of the current matrix column.
     * @param markovStates
     *     Markov States with their outgoing transitions providing the entry
     *     to be returned.
     * @param thinkTimeFormatter
     *     instance for formatting <code>ThinkTime</code> instances.
     *
     * @return
     *     the matrix entry for the given row/column position.
     */
    private String getValue (
            final Service serviceRow,
            final Service serviceColumn,
            final List<MarkovState> markovStates,
            final AbstractThinkTimeFormatter thinkTimeFormatter) {

        String value;  // to be returned;

        final MarkovState markovState =
                this.findMarkovStateByService(markovStates, serviceRow);

        final Transition transition = this.findTransitionByTargetService(
                markovState.getOutgoingTransitions(),
                serviceColumn);

        if (transition != null) {

            final String thinkTimeString =
                    thinkTimeFormatter.getThinkTimeString(
                            transition.getThinkTime());

            value = "" + transition.getProbability();

            if (thinkTimeString != null) {

                value += ("; " + thinkTimeString);
            }

        } else {

            value = 0.0 + "; " + thinkTimeFormatter.getThinkTimeString();
        }

        return value;
    }

    /**
     * Searches for a Markov State by a given <code>Service</code> instance.
     *
     * @param markovStates
     *     list of Markov States to be searched through.
     * @param service
     *     service of a matching Markov State.
     *
     * @return
     *     a valid Markov State if a match exists, or <code>null</code>
     *     otherwise.
     */
    private MarkovState findMarkovStateByService (
            final List<MarkovState> markovStates,
            final Service service) {

        for (final MarkovState markovState : markovStates) {

            if (markovState.getService() == service) {

                return markovState;
            }
        }

        return null;  // no match;
    }

    /**
     * Searches for a transition whose target state is indicated by a given
     * <code>Service</code> instance.
     *
     * @param transitions
     *     list of transitions to be searched through.
     * @param targetService
     *     service which indicates the target state of a matching transition;
     *     this might be even <code>null</code>, if a matching transition shall
     *     lead to the exit state.
     *
     * @return
     *     a valid transition if a match exists, or <code>null</code> otherwise.
     */
    private Transition findTransitionByTargetService (
            final List<Transition> transitions,
            final Service targetService) {

        for (final Transition transition : transitions) {

            final BehaviorModelState behaviorModelState =
                    transition.getTargetState();

            if (targetService == null) {  // searching for exit state?

                if (behaviorModelState instanceof BehaviorModelExitState) {

                    return transition;
                }

            } else {

                if (behaviorModelState instanceof MarkovState) {

                    final MarkovState markovState =
                            (MarkovState) behaviorModelState;

                    if (markovState.getService() == targetService) {

                        return transition;
                    }
                }
            }
        }

        return null;  // no match;
    }
}
