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
import m4jdsl.JavaRequest;
import m4jdsl.Parameter;
import m4jdsl.Property;
import m4jdsl.Request;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.TransformationException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSampler;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * Transformer class for generating Test Plan fragments for M4J-DSL Java
 * requests.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class JavaRequestTransformer extends AbstractRequestTransformer {


    /* **************************  public methods  ************************** */


    /**
     * {@inheritDoc}
     * <p>This method is specialized for M4J-DSL <b>Java requests</b>.
     */
    @Override
    public ListedHashTree transform (
            final Request request,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

        // Test Plan fragment to be returned;
        ListedHashTree listedHashTree;

        // validate the type to ensure that the cast below will not fail;
        this.ensureValidRequestType(JavaRequest.class, request.getClass());

        final JavaRequest javaRequest = (JavaRequest) request;

        // identifier of the JavaSampler to be created;
        final String eId = javaRequest.getEId();

        // properties and parameters of the JavaSampler to be created;
        final List<Property>         properties = javaRequest.getProperties();
        final List<m4jdsl.Parameter> parameters = javaRequest.getParameters();

        final JavaSampler javaSampler = this.createJavaSampler(
                eId,
                properties,
                parameters,
                testPlanElementFactory);

        final List<Assertion> assertions = request.getAssertions();

        // build tree structure to be returned;
        listedHashTree = new ListedHashTree(javaSampler);

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
                            javaRequest,
                            testPlanElementFactory);

            listedHashTree.get(javaSampler).add(responseAssertion);
        }

        return listedHashTree;
    }


    /* **************************  private methods  ************************* */


    /**
     * Creates a {@link JavaSampler} with the given properties and parameters.
     *
     * @param eId
     *     identifier of the element.
     * @param properties
     *     required properties for a request to be sent (e.g., class name).
     * @param parameters
     *     parameters to be sent (e.g., method parameters).
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return  a valid instance of {@link JavaSampler}.
     */
    private JavaSampler createJavaSampler (
            final String eId,
            final List<Property>         properties,
            final List<m4jdsl.Parameter> parameters,
            final TestPlanElementFactory testPlanElementFactory) {

        final JavaSampler sampler = testPlanElementFactory.createJavaSampler();

        sampler.setName(eId);

        for (final Property property : properties) {

            sampler.setProperty(property.getKey(), property.getValue());
        }

        final Arguments arguments = new Arguments();

        for (final Parameter parameter : parameters) {

            arguments.addArgument(parameter.getName(), parameter.getValue());
        }

        sampler.setArguments(arguments);

        return sampler;
    }
}
