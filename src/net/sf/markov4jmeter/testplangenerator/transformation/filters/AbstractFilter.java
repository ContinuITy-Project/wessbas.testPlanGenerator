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


package net.sf.markov4jmeter.testplangenerator.transformation.filters;

import java.util.LinkedList;

import m4jdsl.WorkloadModel;
import net.sf.markov4jmeter.testplangenerator.TestPlanElementFactory;
import net.sf.markov4jmeter.testplangenerator.transformation.TransformationException;
import net.sf.markov4jmeter.testplangenerator.transformation.filters.helpers.TestPlanModifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * This is the base class of all filters which might modify the core structure
 * of a Test Plan, for offering additional functionality. A filter might even be
 * used for optimizing an existing Test Plan structure, e.g., identifying and
 * collecting common Sampler values for setting them as default values through
 * an appropriate configuration element.
 *
 * <p>Furthermore, this class provides helper methods for miscellaneous
 * purposes, e.g., finding unique elements of certain type in a given Test Plan.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public abstract class AbstractFilter {

    /** Error message for the case that an element of certain type cannot be
     *  found in a given Test Plan. */
    private final static String ERROR_ELEMENT_NOT_FOUND =
            "Could not find element of type %s in Test Plan - "
            + "implementation of additional functionality failed.";

    /** Warning message for the case that several elements of a certain type
     *  are found in a given Test Plan. */
    private final static String WARNING_ELEMENT_NOT_UNIQUE =
            "Could not identify unique element of type %s - ambiguous "
            + "elements available, will use the first match.";

    /** Log-factory for any warnings or error messages. */
    protected final static Log LOG = LogFactory.getLog(AbstractFilter.class);

    /** Helper unit which provides methods for requesting, adding, replacing or
     *  deleting Test Plan elements. */
    protected TestPlanModifier testPlanModifier = new TestPlanModifier();


    /* **************************  public methods  ************************** */


    /**
     * Implements the associated functionality in the given Test Plan.
     *
     * @param testPlan
     *     Test Plan to be modified.
     * @param workloadModel
     *     workload model which provides required information.
     * @param testPlanElementFactory
     *     factory for creating Test Plan elements.
     *
     * @return  the modified Test Plan.
     *
     * @throws TransformationException
     *     if any critical error in the transformation process occurs.
     */
    public abstract ListedHashTree modifyTestPlan (
            final ListedHashTree testPlan,
            final WorkloadModel workloadModel,
            final TestPlanElementFactory testPlanElementFactory)
                    throws TransformationException;


    /* *************************  protected methods  ************************ */


    /**
     * Searches for a (unique) occurrence of an element of certain type in a
     * given Test Plan. If no matching element is available, an error message
     * will be given; in case more than one element is available, the first
     * match will be used, and a warning message will be given.
     *
     * @param testPlan  Test Plan to be explored.
     * @param type      type of element to be searched for.
     *
     * @return
     *     a valid element, or <code>null</code> if no element is available.
     */
    protected <T extends AbstractTestElement> T findUniqueTestPlanElement (
            final ListedHashTree testPlan,
            final Class<T> type) {

        final LinkedList<T> elements =
                this.testPlanModifier.collectElementsByType(testPlan, type);

        final int n = elements.size();

        T element = null;

        if (n == 0) {

            final String message = String.format(
                    AbstractFilter.ERROR_ELEMENT_NOT_FOUND,
                    type);

            AbstractFilter.LOG.error(message);

        } else {

            if (n > 1) {

                final String message = String.format(
                        AbstractFilter.WARNING_ELEMENT_NOT_UNIQUE,
                        type);

                AbstractFilter.LOG.warn(message);
            }

            element = elements.getFirst();
        }

        return element;
    }
}
