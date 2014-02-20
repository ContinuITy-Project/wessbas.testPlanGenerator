package net.sf.markov4jmeter.testplangenerator.transformation;

import java.util.HashSet;

import m4jdsl.ProtocolLayerEFSM;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;

import org.apache.jorphan.collections.ListedHashTree;

/**
 * Base class of all transformers which generate Test Plan fragments for
 * M4J-DSL Protocol Layer EFSMs.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public abstract class AbstractProtocolLayerEFSMTransformer {


    /* **************************  public methods  ************************** */


    /**
     * Transforms the given Protocol Layer EFSM into a Test Plan fragment of
     * request Samplers which include possible assertions as children.
     *
     * @param protocolLayerEFSM
     *     Protocol Layer EFSM to be transformed into a Test Plan fragment.
     * @param testPlanElementFactory
     *     Factory for creating Test Plan elements.
     *
     * @return
     *     A Listed Hash Tree which represents a Test Plan fragment
     *     corresponding to the given Protocol Layer EFSM.
     */
    public ListedHashTree transform (
            final ProtocolLayerEFSM protocolLayerEFSM,
            final TestPlanElementFactory testPlanElementFactory) {

        // Test Plan fragment to be returned;
        final ListedHashTree protocolLayerEFSMTree = new ListedHashTree();

        // store visited EFSM states in hash map + corresponding Samplers;
        final HashSet<m4jdsl.ProtocolState> visitedStates =
                new HashSet<m4jdsl.ProtocolState>();

        // transform states recursively, starting with initial state;
        final ListedHashTree protocolStates = this.transformProtocolState(
                protocolLayerEFSM.getInitialState(),
                visitedStates,
                testPlanElementFactory);

        protocolLayerEFSMTree.add(protocolStates);
        return protocolLayerEFSMTree;
    }


    /* *************************  protected methods  ************************ */


    /**
     * Transforms a given M4J-DSL Protocol State into a Test Plan fragment
     * including all of the successor states, too. Therewith, the resulting
     * Listed Hash Tree structure represents a Test Plan fragment corresponding
     * to the indicated EFSM structure with the given state as initial state.
     *
     * @param state
     *     State to be transformed.
     * @param visitedStates
     *     Set of states which have been already visited.
     * @param testPlanElementFactory
     *     Factory for creating Test Plan elements.
     *
     * @return
     *     A Listed Hash Tree which represents a Test Plan fragment.
     */
    protected abstract ListedHashTree transformProtocolState (
            final m4jdsl.ProtocolState state,
            final HashSet<m4jdsl.ProtocolState> visitedStates,
            final TestPlanElementFactory testPlanElementFactory);
}
