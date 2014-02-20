package net.sf.markov4jmeter.testplangenerator.transformation;

import java.util.List;

import m4jdsl.Assertion;
import m4jdsl.HTTPRequest;
import m4jdsl.Parameter;
import m4jdsl.Property;
import m4jdsl.Request;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Transformer class for generating Test Plan fragments for M4J-DSL HTTP
 * requests.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class HTTPRequestTransformer extends AbstractRequestTransformer {


    /* **************************  public methods  ************************** */


    /**
     * {@inheritDoc}
     * <p>This method is specialized for M4J-DSL <b>HTTP requests</b>.
     */
    @Override
    public ListedHashTree transform (
            final Request request,
            final TestPlanElementFactory testPlanElementFactory) {

        // Test Plan fragment to be returned;
        ListedHashTree listedHashTree;

        final HTTPRequest httpRequest = (HTTPRequest) request;

        // identifier of the HTTPSamplerProxy to be created;
        final String eId = httpRequest.getEId();

        // properties and parameters of the HTTPSamplerProxy to be created;
        final List<Property>         properties = httpRequest.getProperties();
        final List<m4jdsl.Parameter> parameters = httpRequest.getParameters();

        final HTTPSamplerProxy httpSamplerProxy = this.createHttpSamplerProxy(
                eId,
                properties,
                parameters,
                testPlanElementFactory);

        final List<Assertion> assertions = request.getAssertions();

        // build tree structure to be returned;
        listedHashTree = new ListedHashTree(httpSamplerProxy);

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
                            httpRequest,
                            testPlanElementFactory);

            listedHashTree.get(httpSamplerProxy).add(responseAssertion);
        }

        return listedHashTree;
    }


    /* **************************  private methods  ************************* */


    /**
     * Creates a {@link HttpSamplerProxy} with the given properties and
     * parameters.
     *
     * @param eId
     *     Identifier of the element.
     * @param properties
     *     Required properties for a request to be sent (e.g., port, domain).
     * @param parameters
     *     Parameters to be sent (e.g., form input data).
     * @param testPlanElementFactory
     *     Factory for creating Test Plan elements.
     *
     * @return  A valid instance of {@link HTTPSamplerProxy}.
     */
    private HTTPSamplerProxy createHttpSamplerProxy (
            final String eId,
            final List<Property>         properties,
            final List<m4jdsl.Parameter> parameters,
            final TestPlanElementFactory testPlanElementFactory) {

        // create a Markov state with an automatically assigned ID;
        final HTTPSamplerProxy sampler =
                testPlanElementFactory.createHTTPSamplerProxy();

        sampler.setName(eId);

        for (final Property property : properties) {

            sampler.setProperty(property.getKey(), property.getValue());
        }

        for (final Parameter parameter : parameters) {

            sampler.addArgument(parameter.getName(), parameter.getValue());
        }

        return sampler;
    }
}
