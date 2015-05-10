package net.sf.markov4jmeter.testplangenerator.transformation;

import java.util.HashMap;
import java.util.List;

import m4jdsl.ApplicationTransition;
import m4jdsl.GuardActionParameterType;
import m4jdsl.SessionLayerEFSM;
import m4jdsl.SessionLayerEFSMState;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.voorn.markov4jmeter.control.ApplicationState;
import net.voorn.markov4jmeter.control.ApplicationStateTransition;
import net.voorn.markov4jmeter.control.ApplicationStateTransitions;

import org.apache.jorphan.collections.ListedHashTree;

/**
 * Class for transforming M4J-DSL Session Layer EFSMs to Test Plan fragments.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class SessionLayerEFSMTransformer {

    /** Instance for transforming M4J-DSL Protocol Layer EFSMs to Test Plan
     *  fragments. */
    private final AbstractProtocolLayerEFSMTransformer protocolLayerEFSMTransformer;


    /* ***************************  Constructors  *************************** */


    /**
     * Constructor for a Session Layer EFSM Transformer.
     *
     * @param protocolLayerEFSMTransformer
     *     Instance for transforming M4J-DSL Protocol Layer EFSMs to Test Plan
     *     fragments.
     */
    public SessionLayerEFSMTransformer (
            final AbstractProtocolLayerEFSMTransformer protocolLayerEFSMTransformer) {

        this.protocolLayerEFSMTransformer = protocolLayerEFSMTransformer;
    }


    /* **************************  public methods  ************************** */


    /**
     * Transforms the given Session Layer EFSM into a Test Plan fragment of
     * Markov States which include their protocol-related requests as children.
     *
     * <p> The transformation process is divided into two steps:
     * <ol>
     *   <li>Visit all states of the given EFSM via depth-first search (DFS)
     *   algorithm*, and transform them into corresponding Test Plan elements,
     *   namely "Markov States"; these Markov States will already contain their
     *   outgoing transitions. Furthermore, the related Protocol Layer EFSMs
     *   will be transformed in this step, indicating the list of request
     *   Samplers for being added to a Markov State. The Markov States will
     *   be stored in a Listed Hash Tree, for being simply added to the
     *   remaining Test Plan fragment.
     *   </li>
     *
     *   <li>Set the "Disabled" properties of all transitions. Each transition
     *   contains a dedicated flag which indicates whether the transition is
     *   valid or not. By default, JMeter fully completes the list of outgoing
     *   transitions for each Markov State when a Test Plan is loaded into the
     *   GUI. In particular, a transition for each available Markov State is
     *   added, possibly targeting even an invalid destination state.
     *   Therefore, <i>dummy transitions</i> are added for invalid transitions
     *   in this step, for setting the "Disabled" flag of those transitions
     *   explicitly.
     *   </li>
     * </ol>
     *
     * <p>* The depth-first search (DFS) algorithm for visiting all states of
     * the given EFSM works recursively, starting with the initial state,
     * followed by its first successor, followed by the successor of the
     * successor etc.; if an already visited state is visited again, the
     * algorithm continues -similarly to backtracking- with the related
     * predecessor.
     *
     * @param sessionLayerEFSM
     *     Session Layer EFSM to be transformed into a Test Plan fragment.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return
     *     a Listed Hash Tree which represents a Test Plan fragment
     *     corresponding to the given Session Layer EFSM.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    public ListedHashTree transform (
            final SessionLayerEFSM sessionLayerEFSM,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

        // Test Plan fragment to be returned;
        final ListedHashTree sessionLayerEFSMTree = new ListedHashTree();

        // store visited EFSM states in hash map + corresponding Markov States;
        final HashMap<m4jdsl.ApplicationState,ApplicationState> visitedStates =
                new HashMap<m4jdsl.ApplicationState,ApplicationState>();

        // transform states recursively, starting with initial state;
        final ListedHashTree markovStates = this.transformApplicationState(
                sessionLayerEFSM.getInitialState(),
                visitedStates,
                testPlanElementFactory);

        // add possible "dummy transitions" for setting their "Disabled" flags;
        this.addDummyMarkovTransitions(markovStates);

        sessionLayerEFSMTree.add(markovStates);
        return sessionLayerEFSMTree;
    }


    /* **************************  private methods  ************************* */


    /**
     * Adds for each Markov State "dummy transitions" for those transitions
     * which are not included in a state-related list of outgoing transitions;
     * this allows to set the "Disabled" property of invalid transitions
     * explicitly.
     *
     * @param markovStates
     *     set of Markov States whose outgoing transitions shall be completed.
     */
    private void addDummyMarkovTransitions (final ListedHashTree markovStates) {

        for (final Object object : markovStates.getArray()) {

            final ApplicationState markovState = (ApplicationState) object;

            // outgoing transitions of the current Markov State;
            final ApplicationStateTransitions markovTransitions =
                    markovState.getTransitions();

            // set flags for transitions to each target object (Markov State);
            for (final Object targetObject : markovStates.getArray()) {

                // target state for testing whether a transition to it exists;
                final ApplicationState targetMarkovState =
                        (ApplicationState) targetObject;

                // transitions in Markov4JMeter are identified by their ID;
                final int targetId = targetMarkovState.getId();

                ApplicationStateTransition markovTransition =
                        this.findTransitionWithTargetState(
                                markovTransitions,
                                targetId);

                if (markovTransition == null) {

                    // add a dummy transition with activated "Disabled" flag;
                    markovTransition = new ApplicationStateTransition(targetId);
                    markovTransition.setDisabled(true);
                    markovTransitions.addTransition(markovTransition);

                } else {

                    // transition is valid, just confirm its setting;
                    markovTransition.setDisabled(false);
                }
            }
        }
    }

    /**
     * Searches for a transition with a specific ID of its target state in a
     * set of outgoing Markov State Transitions.
     *
     * @param markovTransitions
     *     Markov State Transitions to be searched through.
     * @param targetId
     *     ID of the target state to be searched for.
     *
     * @return
     *     a matching transition, or <code>null</code> if no matching transition
     *     can be found.
     */
    private ApplicationStateTransition findTransitionWithTargetState (
            final ApplicationStateTransitions markovTransitions,
            final int targetId) {

        for (final ApplicationStateTransition markovTransition :
            markovTransitions.getTransitionsAsList()) {

            if (markovTransition.getDstStateId() == targetId) {

                return markovTransition;
            }
        }

        return null;  // no match found;
    }

    /**
     * Transforms a given M4J-DSL Application State into a Markov4JMeter Markov
     * State and recursively transforms all of the successor states, too. The
     * outgoing transitions of a Markov State are initialized as valid
     * transitions including guards and actions, indicated through the outgoing
     * transitions of the given Application State.
     *
     * @param state
     *     state to be transformed.
     * @param visitedStates
     *     set of states which have been already visited, including their
     *     corresponding Markov States.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return
     *     a Listed Hash Tree with all Markov States which have been visited
     *     recursively. The order of Markov States is the reverse order of the
     *     corresponding states being visited.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    private ListedHashTree transformApplicationState (
            final m4jdsl.ApplicationState state,
            final HashMap<m4jdsl.ApplicationState,ApplicationState> visitedStates,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

        // Test Plan fragment to be returned;
        final ListedHashTree markovStates = new ListedHashTree();

        // create a named Markov State without transitions;
        final ApplicationState markovState =
                this.createMarkovStateForM4JApplicationState(
                        state,
                        testPlanElementFactory);

        // outgoing transitions of the M4J-DSL state provide target states;
        final List<ApplicationTransition> outgoingTransitions =
                state.getOutgoingTransitions();

        // mark current state as "visited", assign corresponding Markov State;
        visitedStates.put(state, markovState);

        for (final ApplicationTransition transition : outgoingTransitions) {

            // continue with the target state in the M4J-DSL model;
            final SessionLayerEFSMState targetState =
                    transition.getTargetState();

            // just consider Markov States, ignore the exit state;
            if (targetState instanceof m4jdsl.ApplicationState) {

                if ( !visitedStates.containsKey(targetState) ) {

                    // target state has not been visited yet -> transform it;
                    final ListedHashTree targetMarkovStates =
                            this.transformApplicationState(
                                    (m4jdsl.ApplicationState) targetState,
                                    visitedStates,
                                    testPlanElementFactory);

                    markovStates.add(targetMarkovStates);
                }

                // after the target Markov State has been visited, register the
                // related outgoing transition in the current Markov State;

                final ApplicationStateTransition markovTransition =
                        this.createMarkovTransitionForM4JApplicationTransition(
                                transition,
                                visitedStates.get(targetState).getId(),
                                testPlanElementFactory);

                markovState.getTransitions().addTransition(markovTransition);
            }
        }

        final ListedHashTree samplers =
                this.protocolLayerEFSMTransformer.transform(
                        state.getProtocolDetails(),
                        testPlanElementFactory);

        // add the Markov State, and get its embedding Listed Hash Tree for
        // adding the Samplers; note that the following lines do not work
        // alternatively, since a ClassCastException might be thrown when the
        // stored Markov States are requested and casts are conducted on them
        // in method addDummyMarkovTransitions():
        //
        //   final ListedHashTree markovStateNode =
        //           new ListedHashTree(markovState);
        //
        //   markovStateNode.add(new ListedHashTree(samplers));
        //   markovStates.add(markovStateNode);

        markovStates.add(markovState);
        markovStates.get(markovState).add(samplers);

        return markovStates;
    }

    /**
     * Creates a Test Plan element (ApplicationStateTransition) for a given
     * M4J-DSL transition; additionally, the ID of the target Markov State which
     * corresponds to the target state of the M4J-DSL transition must be passed.
     *
     * @param transition
     *     M4J-DSL transition for which a Markov4JMeter transition shall be
     *     created.
     * @param targetStateId
     *     ID of the Markov State which denotes the transition target.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return
     *     a Markov4JMeter transition which includes guards and actions as they
     *     are indicated by the given M4J-DSL transition.
     */
    private ApplicationStateTransition
    createMarkovTransitionForM4JApplicationTransition (
            final m4jdsl.ApplicationTransition transition,
            final int targetStateId,
            final TestPlanElementFactory testPlanElementFactory) {

        final String guard  = getGuardString(transition);
        final String action = getActionString(transition);

        final ApplicationStateTransition markovTransition =
                new ApplicationStateTransition(targetStateId, guard, action);

        // this has not effect, since the default setting is already false;
        // however, it should be left for clarification purposes;
        markovTransition.setDisabled(false);

        return markovTransition;
    }

    /**
     * @param transition
     * @return
     */
    private String getGuardString(final m4jdsl.ApplicationTransition transition) {
    	String returnString = "";
    	for (int i = 0; i < transition.getGuard().size(); i++) {
    		if (transition.getGuard().get(i).getGuardParameter().getParameterType() == GuardActionParameterType.BOOLEAN) {
    			if (transition.getGuard().get(i).isNegate()) {
    				returnString += "${" + transition.getGuard().get(i).getGuardParameter().getGuardActionParameterName() + "}";
    			} else {
    				returnString += "!${" + transition.getGuard().get(i).getGuardParameter().getGuardActionParameterName() + "}";
    			}
    		} else if (transition.getGuard().get(i).getGuardParameter().getParameterType() == GuardActionParameterType.INTEGER) {
    			returnString += "${" + transition.getGuard().get(i).getGuardParameter().getGuardActionParameterName() + "} > 0";
    		}
    		if (i != transition.getGuard().size()-1) {
    			returnString += " && ";
    		}
    	}
    	return returnString;
    }

    /**
     * @param transition
     * @return
     */
    private String getActionString(final m4jdsl.ApplicationTransition transition) {
    	String returnString = "";
    	for (int i = 0; i < transition.getAction().size(); i++) {
    		if (transition.getAction().get(i).getActionParameter().getParameterType() == GuardActionParameterType.BOOLEAN) {
    			returnString += transition.getAction().get(i).getActionParameter().getGuardActionParameterName() + " = true;";
    		} else if (transition.getAction().get(i).getActionParameter().getParameterType() == GuardActionParameterType.INTEGER) {
    			String target = ((m4jdsl.ApplicationState) transition.getTargetState()).getService().getName();
    			if (target.equals(transition.getAction().get(i).getActionParameter().getTargetName())) {
    				returnString += transition.getAction().get(i).getActionParameter().getGuardActionParameterName() +
							" = ${" + transition.getAction().get(i).getActionParameter().getGuardActionParameterName() + "} - 1;";
    			} else if (target.equals(transition.getAction().get(i).getActionParameter().getSourceName())) {
    				returnString += transition.getAction().get(i).getActionParameter().getGuardActionParameterName() +
							" = ${" + transition.getAction().get(i).getActionParameter().getGuardActionParameterName() + "} + 1;";
    			}
    		}
    	}
    	return returnString;
    }

    /**
     * Creates a Test Plan element (Markov State) for a given M4J-DSL state.
     *
     * @param state
     *     M4J-DSL state for which a Markov State shall be created.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return
     *     a Markov State named like the service which is associated with the
     *     given M4J-DSL state.
     */
    private ApplicationState createMarkovStateForM4JApplicationState (
            final m4jdsl.ApplicationState state,
            final TestPlanElementFactory testPlanElementFactory) {

        // name of the Markov State to be created;
        final String name = state.getService().getName();

        // create a Markov state with a generated ID;
        final ApplicationState markovState =
                testPlanElementFactory.createApplicationState();

        markovState.setName(name);

        return markovState;
    }
}
