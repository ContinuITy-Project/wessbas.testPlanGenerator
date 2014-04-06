package net.sf.markov4jmeter.testplangenerator.transformation;

import m4jdsl.WorkloadModel;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.AbstractFilter;
import net.sf.markov4jmeter.testplangenerator.util.CSVHandler;

import org.apache.jorphan.collections.ListedHashTree;

/**
 * Base class of all Test Plan transformer; such a transformer defines the core
 * structure of a Test Plan to be generated from a given M4J-DSL workload model.
 *
 * <p>This class is implemented as <i>builder</i> pattern:
 *
 * <p>Each subclass must implement the abstract <code>transform()</code> method
 * which returns a newly created Test Plan of a certain core structure. The
 * second, non-abstract <code>transform()</code> method invokes that method and
 * applies a sequence of filters to the newly created Test Plan additionally.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public abstract class AbstractTestPlanTransformer {

    /** Handler which provides methods for reading and writing
     *  comma-separated-values (CSV) files. */
    protected final CSVHandler csvHandler;

    /** Output path for Behavior Model files. */
    protected final String behaviorModelsOutputPath;


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for an <code>abstract</code> Test Plan Transformer with a
     * specified CSV-Handler.
     *
     * @param csvHandler
     *     Handler which provides methods for reading and writing
     *     comma-separated-values (CSV) files.
     * @param behaviorModelsOutputPath
     *     output path for Behavior Model files.
     */
    public AbstractTestPlanTransformer (
            final CSVHandler csvHandler,
            final String behaviorModelsOutputPath) {

        this.csvHandler               = csvHandler;
        this.behaviorModelsOutputPath = behaviorModelsOutputPath;
    }


    /* **************************  public methods  ************************** */


    /**
     * Builds a new Test Plan for the given M4J-DSL workload model and applies
     * a sequence of modification filters on it.
     *
     * @param workloadModel
     *     workload model providing the information which is required for
     *     building a related Test Plan.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     * @param filters
     *     a sequence of filters to be applied on the newly created Test Plan;
     *     might be even <code>null</code>, if no filters shall be applied.
     *
     * @return
     *     a newly created Test Plan, possibly modified through the specified
     *     filters.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    public ListedHashTree transform (
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory,
            final AbstractFilter[] filters) throws TransformationException {

        ListedHashTree testPlan = this.transform(
                workloadModel,
                testPlanElementFactory);

        if (filters != null) {

            for (final AbstractFilter filter : filters) {

                testPlan = filter.modifyTestPlan(
                        testPlan,
                        workloadModel,
                        testPlanElementFactory);
            }
        }

        return testPlan;
    }


    /* *************************  protected methods  ************************ */


    /**
     * Builds a new Test Plan for the given M4J-DSL workload model.
     *
     * @param workloadModel
     *     workload model providing the information which is required for
     *     building a related Test Plan.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return
     *     a newly created Test Plan, structured as indicated by the regarding
     *     transformer.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    protected abstract ListedHashTree transform (
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException;
}
