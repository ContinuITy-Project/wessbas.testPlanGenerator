package net.sf.markov4jmeter.testplangenerator.transformation;

import java.util.HashSet;
import java.util.List;

import m4jdsl.ProtocolTransition;
import m4jdsl.Request;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Class for transforming M4J-DSL Protocol Layer EFSMs to Test Plan fragments.
 *
 * <p>This transformer is specialized for a <b>straight ("simple") run</b>
 * through the Protocol States of a given Protocol Layer EFSM, assuming that no
 * state has more than one outgoing transitions; if this restriction does not
 * hold for a state, the run continues with the first outgoing transition, and
 * a warning will be given.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class SimpleProtocolLayerEFSMTransformer
extends AbstractProtocolLayerEFSMTransformer {

    private final static String WARNING_AMBIGUOUS_TRANSITIONS_IN_PROTOCOL_STATE =
            "Protocol State for \"%s\" has more than one outgoing transitions; "
            + "will continue with first target state.";

    /** Log-factory for any warnings or error messages. */
    private final static Log LOG =
            LogFactory.getLog(SimpleProtocolLayerEFSMTransformer.class);


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for a Simple Protocol Layer EFSM Transformer.
     *
     * @param requestTransformer
     *     instance for transforming M4J-DSL requests to Test Plan fragments.
     */
    public SimpleProtocolLayerEFSMTransformer (
            final AbstractRequestTransformer requestTransformer) {

        super(requestTransformer);
    }


    /* *************************  protected methods  ************************ */

    /**
     * {@inheritDoc}
     *
     * <p>This method is specialized for a <b>straight ("simple") run</b>
     * through the states, assuming that no state has more than one outgoing
     * transitions; if this restriction does not hold for a state, the run
     * continues with the first outgoing transition, and a warning will be
     * given.
     */
    @Override
    protected ListedHashTree transformProtocolState (
            final m4jdsl.ProtocolState state,
            final HashSet<m4jdsl.ProtocolState> visitedStates,
            final TestPlanElementFactory testPlanElementFactory) {

        // Test Plan fragment to be returned;
        final ListedHashTree samplers = new ListedHashTree();

        final Request request = state.getRequest();

        // create a named Sampler with properties and parameters;
        ListedHashTree sampler = this.transformRequest(
                request,
                testPlanElementFactory);

        // outgoing transitions of the M4J-DSL state indicate further Samplers;
        final List<ProtocolTransition> outgoingTransitions =
                state.getOutgoingTransitions();

        // mark current state as "visited";
        visitedStates.add(state);

        samplers.add(sampler);

        final int n = outgoingTransitions.size();

        if (n >= 1) {

            // successors must be unique for sequential Samplers (requests);
            // if this restriction does not hold, a warning will be given;
            if (n > 1) {

                final String message = String.format(
                        SimpleProtocolLayerEFSMTransformer.
                        WARNING_AMBIGUOUS_TRANSITIONS_IN_PROTOCOL_STATE,
                        request.getEId());

                SimpleProtocolLayerEFSMTransformer.LOG.warn(message);
            }

            final ProtocolTransition transition = outgoingTransitions.get(0);

            // continue with the target state in the M4J-DSL model;
            final m4jdsl.ProtocolState targetState =
                    transition.getTargetState();

            if ( !visitedStates.contains(targetState) ) {

                // target state has not been visited yet -> transform it;
                final ListedHashTree targetProtocolStates =
                        this.transformProtocolState(
                                targetState,
                                visitedStates,
                                testPlanElementFactory);

                samplers.add(targetProtocolStates);
            }
        }

        return samplers;
    }
}