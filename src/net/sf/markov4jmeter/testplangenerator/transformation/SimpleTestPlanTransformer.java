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


package net.sf.markov4jmeter.testplangenerator.transformation;

import m4jdsl.SessionLayerEFSM;
import m4jdsl.WorkloadModel;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.BehaviorMixFilter;
import net.sf.markov4jmeter.testplangenerator.util.CSVHandler;
import net.voorn.markov4jmeter.control.MarkovController;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * This Test Plan transformer generates a Test Plan for a given workload model,
 * assuming that no Protocol State has more than one outgoing transition.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 *
 * @see SimpleProtocolLayerEFSMTransformer
 */
public class SimpleTestPlanTransformer extends AbstractTestPlanTransformer {

    /** Instance for transforming M4J-DSL Session Layer EFSMs to Test Plan
     *  fragments. */
    private final SessionLayerEFSMTransformer sessionLayerEFSMTransformer;


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for a Simple Test Plan Transformer.
     *
     * @param csvHandler
     *     handler which provides methods for reading and writing
     *     comma-separated-values (CSV) files.
     * @param behaviorModelsOutputPath
     *     output path for Behavior Model files.
     */
    public SimpleTestPlanTransformer (
            final CSVHandler csvHandler,
            final String behaviorModelsOutputPath) {

        super(csvHandler, behaviorModelsOutputPath);

        // build an instance for transforming M4J-DSL Protocol Layer EFSMs to
        // Test Plan fragments in a "simple" way.
        final SimpleProtocolLayerEFSMTransformer protocolLayerEFSMTransformer =
                new SimpleProtocolLayerEFSMTransformer();

        this.sessionLayerEFSMTransformer =
                new SessionLayerEFSMTransformer(protocolLayerEFSMTransformer);

    }


    /* *************************  protected methods  ************************ */


    /**
     * {@inheritDoc}
     * <p>A Test Plan created by this method has the following core structure:
     * <pre>
     * Test Plan
     *   Thread Group
     *     HTTP Header Manager
     *     HTTP Cookie Manager
     *     Markov Session Controller
     *       Markov State [1]
     *       ...
     *       Markov State [n]
     *     Regular Expression Extractor
     *     View Results Tree
     *     Response Time Graph</pre>
     *
     * <p>This method additionally writes the Behavior Models which are provided
     * through the given workload model to related CSV-files, by applying a
     * dedicated filter for Behavior Mix installation.
     *
     * @return
     *     a newly created Test Plan, structured as indicated by the regarding
     *     transformer, or <code>null</code> if any error through applying the
     *     Behavior Mix installation filter occurs.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    @Override
    protected ListedHashTree transform (
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException {

        // create required Test Plan elements;

        final TestPlan testPlan =
                testPlanElementFactory.createTestPlan();

        final SetupThreadGroup setupThreadGroup =
                testPlanElementFactory.createSetupThreadGroup(workloadModel);

        final HeaderManager headerManager =
                testPlanElementFactory.createHeaderManager();

        final CookieManager cookieManager =
                testPlanElementFactory.createCookieManager();

        final UserParameters userParameters =
                testPlanElementFactory.createUserParameterGuardsAndActions(workloadModel);

        final Arguments arguments =
                testPlanElementFactory.createArguments(workloadModel);

        final MarkovController markovController =
                testPlanElementFactory.createMarkovController();

        final RegexExtractor regexExtractor =
                testPlanElementFactory.createRegexExtractor();

        final ResultCollector viewResultsTree =
                testPlanElementFactory.createViewResultsTrue();

        final ResultCollector responseTimeGraph =
                testPlanElementFactory.createResponseTimeGraph();

        // 1st element is tree root;
        ListedHashTree testPlanTree = new ListedHashTree(testPlan);

        // add Thread Group as child of Test Plan; the Thread Group will be
        // embedded into an own Hash Tree, which will be returned; the returned
        // Hash Tree itself contains the Thread Group as first element (root);
        final ListedHashTree setupThreadGroupTree =
                (ListedHashTree) testPlanTree.add(setupThreadGroup);

        // add managers and Request Defaults as children of Thread Group;
        setupThreadGroupTree.add(headerManager);
        setupThreadGroupTree.add(cookieManager);
        setupThreadGroupTree.add(userParameters);
        setupThreadGroupTree.add(arguments);

        // add Markov Controller as child of Thread Group; the Markov Controller
        // will be embedded into an own Hash Tree, which will be returned; the
        // returned Hash Tree itself contains the Markov Controller as first
        // element (root);
        final ListedHashTree markovControllerTree =
                (ListedHashTree) setupThreadGroupTree.add(markovController);

        final SessionLayerEFSM sessionLayerEFSM =
                workloadModel.getApplicationModel().getSessionLayerEFSM();

        final ListedHashTree sessionLayerEFSMTree =
                this.sessionLayerEFSMTransformer.transform(
                        sessionLayerEFSM,
                        testPlanElementFactory);

        final String formula =
                workloadModel.getWorkloadIntensity().getFormula();

        markovController.setArrivalCtrlNumSessions(formula);

        // add Session Layer EFSM as child of Markov Controller;
        // in case the list-tree is empty, the add() method won't add any node;
        markovControllerTree.add(sessionLayerEFSMTree);

        // add Regular Expression Extractor as child of Thread Group;
        setupThreadGroupTree.add(regexExtractor);

        // add both Result Collectors as children of Thread Group;
        setupThreadGroupTree.add(viewResultsTree);
        setupThreadGroupTree.add(responseTimeGraph);

        // install all behavior-stuff by applying a dedicated filter;
        // in case any error occurs, null will be returned;
        testPlanTree = new BehaviorMixFilter(
                this.csvHandler,
                this.behaviorModelsOutputPath).modifyTestPlan(
                        testPlanTree,
                        workloadModel,
                        testPlanElementFactory);

        return testPlanTree;
    }
}
