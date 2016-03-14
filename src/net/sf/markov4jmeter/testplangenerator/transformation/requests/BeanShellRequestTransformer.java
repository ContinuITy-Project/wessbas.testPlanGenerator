/***************************************************************************
 * Copyright (c) 2016 the WESSBAS project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/


package net.sf.markov4jmeter.testplangenerator.transformation.requests;

import java.util.List;

import m4jdsl.Assertion;
import m4jdsl.BeanShellRequest;
import m4jdsl.Property;
import m4jdsl.Request;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.TransformationException;

import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Transformer class for generating Test Plan fragments for M4J-DSL BeanShell
 * requests.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class BeanShellRequestTransformer extends AbstractRequestTransformer {


    /* **************************  public methods  ************************** */


    /**
     * {@inheritDoc}
     * <p>This method is specialized for M4J-DSL <b>BeanShell requests</b>.
     */
    @Override
    public ListedHashTree transform (
            final Request request,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

        // Test Plan fragment to be returned;
        ListedHashTree listedHashTree;

        // validate the type to ensure that the cast below will not fail;
        this.ensureValidRequestType(BeanShellRequest.class, request.getClass());

        final BeanShellRequest beanShellRequest = (BeanShellRequest) request;

        // identifier of the BeanShellSampler to be created;
        final String eId = beanShellRequest.getEId();

        // properties of the BeanShellSampler to be created
        // (note that a BeanShellSampler has no parameters);
        final List<Property> properties = beanShellRequest.getProperties();

        final BeanShellSampler beanShellSampler = this.createBeanShellSampler(
                eId,
                properties,
                testPlanElementFactory);

        final List<Assertion> assertions = request.getAssertions();

        // build tree structure to be returned;
        listedHashTree = new ListedHashTree(beanShellSampler);

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
                            beanShellRequest,
                            testPlanElementFactory);

            listedHashTree.get(beanShellSampler).add(responseAssertion);
        }

        return listedHashTree;
    }


    /* **************************  private methods  ************************* */


    /**
     * Creates a {@link BeanShellSampler} with the given properties.
     *
     * @param eId
     *     identifier of the element.
     * @param properties
     *     required properties for a request to be sent (e.g., script content).
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return  a valid instance of {@link BeanShellSampler}.
     */
    private BeanShellSampler createBeanShellSampler (
            final String eId,
            final List<Property> properties,
            final TestPlanElementFactory testPlanElementFactory) {

        final BeanShellSampler sampler =
                testPlanElementFactory.createBeanShellSampler();

        sampler.setName(eId);

        for (final Property property : properties) {

            sampler.setProperty(property.getKey(), property.getValue());
        }

        return sampler;
    }
}
