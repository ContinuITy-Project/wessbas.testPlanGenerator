package net.sf.markov4jmeter.testplangenerator.transformation.filters;

import m4jdsl.WorkloadModel;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * This filter adds a  Config Test Element ("HTTP Request Defaults") to a given
 * Test Plan and explores the Test Plan for common Sampler data; the identified
 * values will be deleted in the related Samplers and stored as defaults in
 * the newly added element instead.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class HeaderDefaultsFilter extends AbstractFilter {


    /* **************************  public methods  ************************** */


    @Override
    public ListedHashTree modifyTestPlan (
            final ListedHashTree testPlan,
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory) {

        final ConfigTestElement configTestElement =
                this.createConfigElement(testPlan, testPlanElementFactory);

        this.insertConfigElement(testPlan, configTestElement);

        return testPlan;
    }


    /* **************************  private methods  ************************* */


    /**
     * Inserts a given Config Test Element into a specified Test Plan.
     *
     * @param testPlan
     *     Test Plan into which the given Config Test Element shall be inserted.
     * @param configTestElement
     *     Config Test Element to be inserted.
     */
    private void insertConfigElement (
            final ListedHashTree testPlan,
            final ConfigTestElement configTestElement) {

        // find Thread Group;
        final SetupThreadGroup setupThreadGroup =
                this.findUniqueTestPlanElement(
                        testPlan,
                        SetupThreadGroup.class);

        if (setupThreadGroup != null) {  // Thread Group found?

            // add element as first child of Thread Group;
            this.testPlanModifier.addElementAsChildAtIndex(
                    testPlan,
                    configTestElement,
                    setupThreadGroup,
                    0);  // first index position;
        }
    }

    /**
     * Creates a Config Test Element and explores the given Test Plan for common
     * Sampler data to be moved into the newly created element.
     *
     * @param testPlan                Test Plan to be explored.
     * @param testPlanElementFactory  Factory for creating Test Plan elements.
     *
     * @return  The newly created Config Test Element.
     */
    private ConfigTestElement createConfigElement (
            final ListedHashTree testPlan,
            final TestPlanElementFactory testPlanElementFactory) {

        final ConfigTestElement configTestElement =
                testPlanElementFactory.createConfigTestElement();

        // not implemented yet -> class is just for demonstration purposes;
        //
        // TODO:
        //  - find most common values in all HTTP Sampler Proxy elements;
        //  - store those values in configTestElement;
        //  - delete those values in the related HTTP Sampler Proxy elements;
        //
        // final LinkedList<HTTPSamplerProxy> requests =
        //     this.testPlanModifier.collectElementsByType(
        //              testPlan,
        //              HTTPSamplerProxy.class);
        // ...

        return configTestElement;
    }
}