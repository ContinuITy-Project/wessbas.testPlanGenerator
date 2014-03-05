package net.sf.markov4jmeter.testplangenerator.transformation;

import java.util.List;

import m4jdsl.Assertion;
import m4jdsl.Request;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Base class of all transformers which generate Test Plan fragments for
 * M4J-DSL requests.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public abstract class AbstractRequestTransformer {


    /* **************************  public methods  ************************** */


    /**
     * Transforms a given M4J-DSL request into a corresponding Test Plan
     * fragment.
     *
     * @param request                 the request to be transformed.
     * @param testPlanElementFactory  factory for creating Test Plan elements.
     *
     * @return a Test Plan fragment which represents the given request.
     */
    public abstract ListedHashTree transform (
            final m4jdsl.Request request,
            final TestPlanElementFactory testPlanElementFactory);


    /* *************************  protected methods  ************************ */


    /**
     * Transforms the assertions of a given M4J-DSL request into a corresponding
     * Test Plan fragment.
     *
     * @param request
     *     the request whose assertions shall be transformed.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return  a Test Plan fragment which represents the given assertions.
     */
    protected ListedHashTree transformRequestAssertions (
            final Request request,
            final TestPlanElementFactory testPlanElementFactory) {

        // Test Plan fragment to be returned;
        ListedHashTree listedHashTree;

        // assertions with patterns to be tested;
        final List<Assertion> assertions = request.getAssertions();

        final ResponseAssertion responseAssertion =
                this.createResponseAssertion(
                        assertions,
                        testPlanElementFactory);

        listedHashTree = new ListedHashTree(responseAssertion);

        return listedHashTree;
    }


    /* **************************  private methods  ************************* */


    /**
     * Creates a {@link ResponseAssertion} Test Plan element which contains the
     * patterns to be tested, provided by a given list of M4J-DSL assertions.
     *
     * @param assertions
     *     list of M4J-DSL assertions, providing the patterns to be tested.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return  a valid instance of {@link ResponseAssertion}.
     */
    private ResponseAssertion createResponseAssertion (
            final List<m4jdsl.Assertion> assertions,
            final TestPlanElementFactory testPlanElementFactory) {

        // to be returned;
        final ResponseAssertion responseAssertion =
                testPlanElementFactory.createResponseAssertion();

        for (final Assertion assertion : assertions) {

            responseAssertion.addTestString(assertion.getPatternToTest());
        }

        return responseAssertion;
    }
}