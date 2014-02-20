package net.sf.markov4jmeter.testplangenerator.transformation;

import m4jdsl.SessionLayerEFSM;
import m4jdsl.WorkloadModel;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.BehaviorMixFilter;
import net.voorn.markov4jmeter.control.MarkovController;

import org.apache.jmeter.extractor.RegexExtractor;
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

    /** Instance for transforming M4J-DSL Protocol Layer EFSMs to Test Plan
     *  fragments. */
    private final SimpleProtocolLayerEFSMTransformer protocolLayerEFSMTransformer;

    /** Instance for transforming M4J-DSL Session Layer EFSMs to Test Plan
     *  fragments. */
    private final SessionLayerEFSMTransformer sessionLayerEFSMTransformer;


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for a Simple Test Plan Transformer.
     *
     * @param requestTransformer
     *     Request Transformer which generates Test Plan fragments for M4J-DSL
     *     requests.
     */
    public SimpleTestPlanTransformer (
            final AbstractRequestTransformer requestTransformer) {

        this.protocolLayerEFSMTransformer =
                new SimpleProtocolLayerEFSMTransformer(requestTransformer);

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
     *     A newly created Test Plan, structured as indicated by the regarding
     *     transformer, or <code>null</code> if any error through applying the
     *     Behavior Mix installation filter occurs.
     */
    @Override
    protected ListedHashTree transform (
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory) {

        // create required Test Plan elements;

        final TestPlan testPlan =
                testPlanElementFactory.createTestPlan();

        final SetupThreadGroup setupThreadGroup =
                testPlanElementFactory.createSetupThreadGroup();

        final HeaderManager headerManager =
                testPlanElementFactory.createHeaderManager();

        final CookieManager cookieManager =
                testPlanElementFactory.createCookieManager();

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
        testPlanTree = new BehaviorMixFilter().modifyTestPlan(
                testPlanTree,
                workloadModel,
                testPlanElementFactory);

        return testPlanTree;
    }
}