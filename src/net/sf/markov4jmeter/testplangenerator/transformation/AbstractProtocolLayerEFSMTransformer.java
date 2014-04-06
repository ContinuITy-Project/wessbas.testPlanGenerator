package net.sf.markov4jmeter.testplangenerator.transformation;

import java.util.HashMap;
import java.util.HashSet;

import m4jdsl.ProtocolLayerEFSM;
import m4jdsl.Request;
import m4jdsl.impl.BeanShellRequestImpl;
import m4jdsl.impl.HTTPRequestImpl;
import m4jdsl.impl.JUnitRequestImpl;
import m4jdsl.impl.JavaRequestImpl;
import m4jdsl.impl.SOAPRequestImpl;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.AbstractRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.BeanShellRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.HTTPRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.JUnitRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.JavaRequestTransformer;
import net.sf.markov4jmeter.testplangenerator.transformation.requests.SOAPRequestTransformer;

import org.apache.jorphan.collections.ListedHashTree;

/**
 * Base class of all transformers which generate Test Plan fragments for
 * M4J-DSL Protocol Layer EFSMs.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public abstract class AbstractProtocolLayerEFSMTransformer {

    /** Error message for the case that an unknown request type has been
     *  detected. */
    private final static String ERROR_UNKNOWN_REQUEST_TYPE =
            "unknown request type detected for request with id %s";

    /** Request Transformers registry. */
    private final static
    HashMap<Class<? extends Request>, AbstractRequestTransformer>
    REQUEST_TRANSFORMERS;

    static {

        // register all available Request Transformers;
        REQUEST_TRANSFORMERS = new HashMap<Class<? extends Request>, AbstractRequestTransformer>();

        REQUEST_TRANSFORMERS.put(HTTPRequestImpl.class     , new HTTPRequestTransformer());
        REQUEST_TRANSFORMERS.put(JavaRequestImpl.class     , new JavaRequestTransformer());
        REQUEST_TRANSFORMERS.put(BeanShellRequestImpl.class, new BeanShellRequestTransformer());
        REQUEST_TRANSFORMERS.put(JUnitRequestImpl.class    , new JUnitRequestTransformer());
        REQUEST_TRANSFORMERS.put(SOAPRequestImpl.class     , new SOAPRequestTransformer());
    }


    /* **************************  public methods  ************************** */


    /**
     * Transforms the given Protocol Layer EFSM into a Test Plan fragment of
     * request Samplers which include possible assertions as children.
     *
     * @param protocolLayerEFSM
     *     Protocol Layer EFSM to be transformed into a Test Plan fragment.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return
     *     a Listed Hash Tree which represents a Test Plan fragment
     *     corresponding to the given Protocol Layer EFSM.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    public ListedHashTree transform (
            final ProtocolLayerEFSM protocolLayerEFSM,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

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

    /**
     * Transforms the given request into a Test Plan fragment which includes a
     * corresponding Sampler.
     *
     * @param request
     *     request to be transformed into a Test Plan fragment.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return
     *     a Listed Hash Tree which represents a Test Plan fragment
     *     corresponding to the given request.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    public ListedHashTree transformRequest (
            final Request request,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

        // create a named Sampler with properties and parameters;
        final ListedHashTree sampler;

        final AbstractRequestTransformer requestTransformer =
                AbstractProtocolLayerEFSMTransformer.REQUEST_TRANSFORMERS.get(
                        request.getClass());

        if (requestTransformer == null) {

            final String message = String.format(
                    AbstractProtocolLayerEFSMTransformer.ERROR_UNKNOWN_REQUEST_TYPE,
                    request.getEId());

            throw new TransformationException(message);
        }

        sampler = requestTransformer.transform(
                request,
                testPlanElementFactory);

        return sampler;
    }


    /* *************************  protected methods  ************************ */


    /**
     * Transforms a given M4J-DSL Protocol State into a Test Plan fragment
     * including all of the successor states, too. Therewith, the resulting
     * Listed Hash Tree structure represents a Test Plan fragment corresponding
     * to the indicated EFSM structure with the given state as initial state.
     *
     * @param state
     *     state to be transformed.
     * @param visitedStates
     *     set of states which have been already visited.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return
     *     a Listed Hash Tree which represents a Test Plan fragment.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    protected abstract ListedHashTree transformProtocolState (
            final m4jdsl.ProtocolState state,
            final HashSet<m4jdsl.ProtocolState> visitedStates,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException;
}
