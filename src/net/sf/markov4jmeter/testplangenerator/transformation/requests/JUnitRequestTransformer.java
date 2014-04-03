package net.sf.markov4jmeter.testplangenerator.transformation.requests;

import java.util.List;

import m4jdsl.Assertion;
import m4jdsl.JUnitRequest;
import m4jdsl.Property;
import m4jdsl.Request;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.TransformationException;

import org.apache.jmeter.protocol.java.sampler.JUnitSampler;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Transformer class for generating Test Plan fragments for M4J-DSL JUnit
 * requests.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class JUnitRequestTransformer extends AbstractRequestTransformer {


    /* **************************  public methods  ************************** */


    /**
     * {@inheritDoc}
     * <p>This method is specialized for M4J-DSL <b>JUnit requests</b>.
     */
    @Override
    public ListedHashTree transform (
            final Request request,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

        // Test Plan fragment to be returned;
        ListedHashTree listedHashTree;

        // validate the type to ensure that the cast below will not fail;
        this.ensureValidRequestType(JUnitRequest.class, request.getClass());

        final JUnitRequest jUnitRequest = (JUnitRequest) request;

        // identifier of the JUnitSampler to be created;
        final String eId = jUnitRequest.getEId();

        // properties of the JUnitSampler to be created
        // (note that a JUnitSampler has no parameters);
        final List<Property> properties = jUnitRequest.getProperties();

        final JUnitSampler jUnitSampler = this.createJUnitSampler(
                eId,
                properties,
                testPlanElementFactory);

        final List<Assertion> assertions = request.getAssertions();

        // build tree structure to be returned;
        listedHashTree = new ListedHashTree(jUnitSampler);

        // add ResponseAssertion if and only if any Strings have been defined;
        if (assertions.size() > 0) {

            /* could be used alternatively:

            final ResponseAssertion responseAssertion =
                    this.createResponseAssertion(
                            assertions,
                            testPlanElementFactory);
             */

            final ListedHashTree responseAssertion =
                    this.transformRequestAssertions(
                            jUnitRequest,
                            testPlanElementFactory);

            listedHashTree.get(jUnitSampler).add(responseAssertion);
        }

        return listedHashTree;
    }


    /* **************************  private methods  ************************* */


    /**
     * Creates a {@link JUnitSampler} with the given properties.
     *
     * @param eId
     *     identifier of the element.
     * @param properties
     *     required properties for a request to be sent (e.g., success message).
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return  a valid instance of {@link JUnitSampler}.
     */
    private JUnitSampler createJUnitSampler (
            final String eId,
            final List<Property> properties,
            final TestPlanElementFactory testPlanElementFactory) {

        final JUnitSampler sampler =
                testPlanElementFactory.createJUnitSampler();

        sampler.setName(eId);

        for (final Property property : properties) {

            sampler.setProperty(property.getKey(), property.getValue());
        }

        return sampler;
    }
}
