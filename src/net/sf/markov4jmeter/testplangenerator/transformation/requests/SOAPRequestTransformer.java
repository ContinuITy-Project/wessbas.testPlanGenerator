package net.sf.markov4jmeter.testplangenerator.transformation.requests;

import java.util.List;

import m4jdsl.Assertion;
import m4jdsl.Property;
import m4jdsl.Request;
import m4jdsl.SOAPRequest;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.TransformationException;

import org.apache.jmeter.protocol.http.sampler.SoapSampler;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Transformer class for generating Test Plan fragments for M4J-DSL SOAP
 * requests.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class SOAPRequestTransformer extends AbstractRequestTransformer {


    /* **************************  public methods  ************************** */


    /**
     * {@inheritDoc}
     * <p>This method is specialized for M4J-DSL <b>SOAP requests</b>.
     */
    @Override
    public ListedHashTree transform (
            final Request request,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

        // Test Plan fragment to be returned;
        ListedHashTree listedHashTree;

        // validate the type to ensure that the cast below will not fail;
        this.ensureValidRequestType(SOAPRequest.class, request.getClass());

        final SOAPRequest soapRequest = (SOAPRequest) request;

        // identifier of the SoapSampler to be created;
        final String eId = soapRequest.getEId();

        // properties of the SOAPSampler to be created
        // (note that a SOAPSampler has no parameters);
        final List<Property> properties = soapRequest.getProperties();

        final SoapSampler soapSampler = this.createSoapSampler(
                eId,
                properties,
                testPlanElementFactory);

        final List<Assertion> assertions = request.getAssertions();

        // build tree structure to be returned;
        listedHashTree = new ListedHashTree(soapSampler);

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
                            soapRequest,
                            testPlanElementFactory);

            listedHashTree.get(soapSampler).add(responseAssertion);
        }

        return listedHashTree;
    }


    /* **************************  private methods  ************************* */


    /**
     * Creates a {@link SoapSampler} with the given properties.
     *
     * @param eId
     *     identifier of the element.
     * @param properties
     *     required properties for a request to be sent (e.g., URL).
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return  a valid instance of {@link SoapSampler}.
     */
    private SoapSampler createSoapSampler (
            final String eId,
            final List<Property> properties,
            final TestPlanElementFactory testPlanElementFactory) {

        final SoapSampler sampler = testPlanElementFactory.createSoapSampler();

        sampler.setName(eId);

        for (final Property property : properties) {

            sampler.setProperty(property.getKey(), property.getValue());
        }

        return sampler;
    }
}
