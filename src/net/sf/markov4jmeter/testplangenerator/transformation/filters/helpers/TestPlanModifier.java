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


package net.sf.markov4jmeter.testplangenerator.transformation.filters.helpers;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;

/**
 * This helper class provides high-level operations for modifying
 * {@link TestPlan} instances. In particular, it simplifies requesting, adding,
 * replacing and removing elements of Test Plans.
 *
 * <p>It is assumed that occurrences of Test Plan elements are <u>unique</u> for
 * identifying them by reference.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class TestPlanModifier {

    /* IMPLEMENTATION NOTES:
     * ---------------------
     * Test Plans are stored as HashTree instances, with keys being associated
     * with Test Plan elements. Hence, most of the following methods use the
     * term "element" as synonym for "key"; both terms denote (Test Plan)
     * elements and (hash) keys as well.
     *
     * The insertion of elements into Test Plans at certain positions is done
     * with the use of the clone() method of the HashTree class; this is not an
     * efficient solution, since sub-trees are cloned before they are copied
     * back; however, the cloning is not recursive and only done on current
     * top-level elements; the HashTree class offers just a small low-level set
     * of HashTree modification methods for those purposes, so that this way
     * has been selected as appropriate solution.
     */

    /* **************************  public methods  ************************** */


    /**
     * Adds an element as child of the specified parent element to the given
     * Test Plan. The child element will be simply added at the last position
     * of the child elements list.
     *
     * @param testPlan
     *     Test Plan to be modified.
     * @param childElement
     *     element to be added as child.
     * @param parentElement
     *     element to become the parent of the added child element.
     *
     * @return
     *     a newly created {@link HashTree} node which embeds the added element;
     *     in case the element could not be added, <code>null</code> will be
     *     returned.
     */
    public HashTree addElementAsChild (
            final ListedHashTree testPlan,
            final AbstractTestElement childElement,
            final AbstractTestElement parentElement) {

        final HashTree subTree = testPlan.search(parentElement);

        if (subTree != null) {

            return subTree.add(childElement);
        }

        return null;
    }

    /**
     * Adds an element as child of the specified parent element to the given
     * Test Plan. The child element will be added at the specified index
     * position in the children list.
     *
     * @param testPlan
     *     Test Plan to be modified.
     * @param childElement
     *     element to be added as child.
     * @param parentElement
     *     element to become the parent of the added child element.
     * @param index
     *     index position of the new child in the parent's children list,
     *     starting with index 0; if the index equals or exceeds the number of
     *     existing children, the new child will be added at the last position;
     *     a negative index will be treated as 0.
     *
     * @return
     *     a newly created {@link HashTree} node which embeds the added element;
     *     in case the element could not be added, <code>null</code> will be
     *     returned.
     */
    public HashTree addElementAsChildAtIndex (
            final ListedHashTree testPlan,
            final AbstractTestElement childElement,
            final AbstractTestElement parentElement,
            final int index) {

        // to be returned;
        HashTree insertedHashTree = null;

        final HashTree parentTree = testPlan.search(parentElement);

        if (parentTree != null) {

            final int n = parentTree.list().size();

            // if the specified index exceeds the index of the last existing
            // parent-tree element or if no elements exist yet, the new element
            // will simply be appended;
            if (n <= index || (n == 0)) {

                insertedHashTree = parentTree.add(childElement);

            } else {

                // clone the sub-tree of parent node, and clear the parent-node
                // itself (see implementation note in the header of this class);
                final HashTree clonedParentTree = (HashTree) parentTree.clone();

                parentTree.clear();

                // re-insert all children into the parent node, by using the
                // cloned sub-tree as source; modifications will be added
                // accordingly;

                // if index is negative, initialize i for adding on first match;
                int i = (index < 0) ? index : 0;

                for (final Object element : clonedParentTree.list()) {

                    if (index == i++) {

                        insertedHashTree = parentTree.add(childElement);
                    }

                    parentTree.set(
                            element,
                            clonedParentTree.getTree(element));
                }
            }
        }

        return insertedHashTree;
    }

    /**
     * Returns the index position of the specified child element in its parent
     * element's children list within the given Test Plan.
     *
     * @param testPlan
     *     Test Plan to be explored.
     * @param childElement
     *     element whose index position shall be determined.
     *
     * @return
     *     an index position >=  0; in case no parent element for the specified
     *     element can be determined, e.g., if it denotes a root node of the
     *     given Test Plan and is no child therewith, -1 will be returned.
     */
    public int getChildIndexOfElement (
            final ListedHashTree testPlan,
            final AbstractTestElement childElement) {

        // to be returned (-1 indicates "indeterminable");
        int index = -1;

        try {

            // might throw a ModificationException;
            final HashTree parentTree =
                    this.findParentTree(testPlan, childElement);

            if (parentTree != null) {

                final Iterator<Object> iterator = parentTree.list().iterator();

                for (int i = 0; iterator.hasNext(); i++) {

                    if (iterator.next() == childElement) {

                        index = i;
                        break;
                    }
                }
            }

        } catch (final ModificationException ex) {

            // ignore exception message, indicate error by returning -1;
        }

        return index;
    }

    /**
     * Removes an element from the given Test Plan.
     *
     * @param testPlan  Test Plan to be modified.
     * @param element   element to be removed.
     *
     * @return
     *     the {@link HashTree} node which embeds the removed element; in case
     *     the element could not be removed, <code>null</code> will be returned.
     */
    public HashTree removeElement (
            final ListedHashTree testPlan,
            final AbstractTestElement element) {

        // to be returned (null indicates an error);
        HashTree removedHashTree = null;

        try {

            // might throw a ModificationException;
            final HashTree parentTree = this.findParentTree(testPlan, element);

            if (parentTree != null) {

                removedHashTree = parentTree.remove(element);
            }

        } catch (final ModificationException ex) {

            // ignore exception message, indicate error by returning null;
        }

        return removedHashTree;
    }

    /**
     * Replaces a current element in the given Test Plan with a new element.
     *
     * @param testPlan
     *     Test Plan to be modified.
     * @param currentElement
     *     element to be replaced, not denoting the root of a Test Plan
     *     (trivial case).
     * @param newElement
     *     element to be inserted.
     *
     * @return
     *     <code>true</code> if and only if the replacement was successful;
     *     errors might occur through a <code>null</code> value for
     *     <code>newElement</code> or a passed element for
     *     <code>currentElement</code> whose parent element cannot be
     *     determined in the given Test Plan.
     */
    public boolean replaceElement (
            final ListedHashTree testPlan,
            final AbstractTestElement currentElement,
            final AbstractTestElement newElement) {

        if (newElement != null) {

            // NOTE: since the replace() method in the HashTree class replaces
            // any tree structure found -under- the current element with the new
            // element, the parent element of the current element needs to be
            // determined at first; the root element of the Test Plan is not
            // suitable as parent for general use, since the replace() method
            // is not recursive and replaces only elements one level deeper;

            try {

                // might throw a ModificationException;
                final HashTree parentTree =
                        this.findParentTree(testPlan, currentElement);

                if (parentTree != null) {

                    parentTree.replace(currentElement, newElement);
                    return true;
                }

            } catch (final ModificationException ex) {

                // ignore exception message, indicate error by returning false;
            }
        }

        return false;
    }

    /**
     * Adds an element as sibling of the specified relative element to the given
     * Test Plan. The sibling element will be added just above the relative
     * element.
     *
     * @param testPlan
     *     Test Plan to be modified.
     * @param siblingElement
     *     element to be added as sibling.
     * @param relativeElement
     *     element to become the sibling of the added element.
     *
     * @return
     *     a newly created {@link HashTree} node which embeds the added child
     *     element; in case the element could not be added, <code>null</code>
     *     will be returned.
     */
    public HashTree addElementAsPrecedingSibling (
            final ListedHashTree testPlan,
            final AbstractTestElement siblingElement,
            final AbstractTestElement relativeElement) {

        return this.addElementAsSibling(
                testPlan,
                siblingElement,
                relativeElement,
                false);  // !isSubsequent;
    }

    /**
     * Adds an element as sibling of the specified relative element to the given
     * Test Plan. The sibling element will be added just below the relative
     * element.
     *
     * @param testPlan
     *     Test Plan to be modified.
     * @param siblingElement
     *     element to be added as sibling.
     * @param relativeElement
     *     element to become the sibling of the added element.
     *
     * @return
     *     a newly created {@link HashTree} node which embeds the added element;
     *     in case the element could not be added, <code>null</code> will be
     *     returned.
     */
    public HashTree addElementAsSubsequentSibling (
            final ListedHashTree testPlan,
            final AbstractTestElement siblingElement,
            final AbstractTestElement relativeElement) {

        return this.addElementAsSibling(
                testPlan,
                siblingElement,
                relativeElement,
                true);  // isSubsequent;
    }

    /**
     * Collects all elements of a certain name in the given Test Plan.
     *
     * @param testPlan
     *     Test Plan to be explored.
     * @param name
     *     name which must be matched by elements for being included into the
     *     result list; <code>null</code> will be ignored.
     *
     * @return
     *     a list of all matching elements.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractTestElement> LinkedList<T> collectElementsByName (
            final ListedHashTree testPlan,
            final String name) {

        // to be returned;
        final LinkedList<T> matchingTestPlanElements = new LinkedList<T>();

        if (name != null) {  // no matches, if name is null;

            // the HashTree might contain elements of arbitrary type, e.g., if
            // it does not denote a valid Test Plan; for finding specially named
            // Test Plan elements anyway, instances of type TestElement need to
            // be filtered out at first;
            final LinkedList<AbstractTestElement> testElements =
                    this.collectElementsByType(
                            testPlan,
                            AbstractTestElement.class);

            for (final AbstractTestElement testElement : testElements) {

                if (name != null && name.equals(testElement.getName())) {

                    // the cast does not need to be checked here, since
                    // testElement is surely of type T
                    // ->  @SuppressWarnings("unchecked");
                    matchingTestPlanElements.add((T) testElement);
                }
            }
        }

        return matchingTestPlanElements;
    }

    /**
     * Collects all elements of a certain type in the given Test Plan.
     *
     * @param testPlan
     *     Test Plan to be explored.
     * @param type
     *     type which must be matched by elements for being included into the
     *     result list; <code>null</code> will be ignored.
     *
     * @return
     *     a list of all matching elements.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractTestElement> LinkedList<T> collectElementsByType (
            final ListedHashTree testPlan,
            final Class<T> type) {

        // to be returned;
        final LinkedList<T> matchingElements = new LinkedList<T>();

        // get all elements contained in the given Test Plan;
        final LinkedList<Object> elements = this.collectAllKeys(testPlan);

        for (final Object element : elements) {

            if ( type.equals(element.getClass()) ) {

                // the cast does not need to be checked here, since the element
                // is surely of type T  ->  @SuppressWarnings("unchecked");
                matchingElements.add((T) element);
            }
        }

        return matchingElements;
    }


    /* *************************  protected methods  ************************ */


    /**
     * Searches for the parent node of the specified element in the given
     * sub-tree.
     *
     * @param subTree  sub-tree to be explored.
     * @param element  element whose parent node shall be found.
     *
     * @return
     *     the parent node of the specified element or <code>null</code>, if
     *     the parent node could not be found.
     *
     * @throws ModificationException
     *     if the traversal on the given sub-tree reports an internal error.
     */
    protected HashTree findParentTree (
            final HashTree subTree,
            final Object element) throws ModificationException {

        final FindParentTreeTraverser findParentKeyTraverser =
                new FindParentTreeTraverser(element);

        try {

            subTree.traverse(findParentKeyTraverser);

        } catch (final RuntimeException ex) {

            if (findParentKeyTraverser.hasThrown(ex)) {

                throw new ModificationException(ex.getMessage());
            }
        }

        return findParentKeyTraverser.getParent();
    }


    /* ****************  private methods (general purposes)  **************** */


    /**
     * Adds an element as sibling of the specified relative element to the given
     * Test Plan. The sibling element will be added alternatively just above or
     * just below the relative element.
     *
     * @param testPlan
     *     Test Plan to be modified.
     * @param siblingElement
     *     element to be added as sibling.
     * @param relativeElement
     *     element to become the sibling of the added element.
     * @param isSubsequent
     *     <code>true</code> if and only if the sibling element shall be
     *     inserted just below the relative element; otherwise the sibling
     *     element will be inserted just above the relative element.
     * @return
     *     a newly created {@link HashTree} node which embeds the added element;
     *     in case the element could not be added, <code>null</code> will be
     *     returned.
     */
    private HashTree addElementAsSibling (
            final ListedHashTree testPlan,
            final AbstractTestElement siblingElement,
            final AbstractTestElement relativeElement,
            final boolean isSubsequent) {

        // to be returned (null indicates that no element has been inserted);
        HashTree insertedHashTree = null;

        HashTree parentTree = null;

        try {

            // might throw a ModificationException;
            parentTree = this.findParentTree(testPlan, relativeElement);

        } catch (final ModificationException ex) {

            // ignore exception message, indicate error by returning null;
            insertedHashTree = null;
        }

        if (parentTree != null) {

            // clone sub-tree of parent node, and clear the parent-node itself;

            final HashTree clonedParentTree = (HashTree) parentTree.clone();

            parentTree.clear();

            // re-insert all children into the parent node, by using the cloned
            // sub-tree as source; modifications will be added accordingly;

            for (final Object element : clonedParentTree.list()) {

                if (!isSubsequent && element.equals(relativeElement)) {

                    insertedHashTree = parentTree.add(siblingElement);
                }

                parentTree.set(
                        element,
                        clonedParentTree.getTree(element));

                if (isSubsequent && element.equals(relativeElement)) {

                    insertedHashTree = parentTree.add(siblingElement);
                }
            }
        }

        return insertedHashTree;
    }

    /**
     * Collects all elements in the given sub-tree.
     *
     * @param subTree  sub-tree to be explored.
     *
     * @return  a valid list of all elements.
     */
    private LinkedList<Object> collectAllKeys (final ListedHashTree subTree) {

        final CollectKeysTraverser collectKeysTraverser =
                new CollectKeysTraverser();

        subTree.traverse(collectKeysTraverser);

        return collectKeysTraverser.getKeys();
    }


    /* *************************  internal classes  ************************* */


    /**
     * (Internal) traverser for collecting all keys in a HashTree.
     *
     * @author   Eike Schulz (esc@informatik.uni-kiel.de)
     * @version  1.0
     */
    private class CollectKeysTraverser implements HashTreeTraverser {

        /** List of collected keys. */
        private final LinkedList<Object> keys = new LinkedList<Object>();

        /**
         * Returns the list of collected keys.
         *
         * @return
         *     a valid list of collected keys; might be empty, if no keys are
         *     available.
         */
        public LinkedList<Object> getKeys() {

            return this.keys;
        }

        @Override
        public void addNode (Object node, HashTree subTree) {

            keys.add(node);
        }

        @Override
        public void processPath () { }

        @Override
        public void subtractNode () { }

    }

    /**
     * (Internal) traverser for finding a key's parent node in a HashTree.
     *
     * @author   Eike Schulz (esc@informatik.uni-kiel.de)
     * @version  1.0
     */
    private class FindParentTreeTraverser implements HashTreeTraverser {

        /** <code>String</code> which serves as source identifier when a
         *  traversal is stopped via {@link RuntimeException}.
         *  @see #hasThrown(RuntimeException) */
        private final static String SUCCESS = "success";

        /** Reference to a detected parent sub-tree, initialized through a
         *  successful traversal.
         *  @see #getParent() */
        private HashTree parentTree = null;

        /** Key whose parent shall be found. */
        private final Object key;


        /**
         * Constructor for a find-parent-node traverser.
         *
         * @param key  key whose parent node shall be detected.
         */
        public FindParentTreeTraverser (final Object key) {

            this.key = key;
        }


        /**
         * Returns the reference to a detected parent node.
         *
         * @return
         *     a valid node, or <code>null</code> if no parent node is
         *     available.
         */
        public HashTree getParent() {

            return this.parentTree;
        }

        @Override
        public void addNode (Object node, HashTree subTree)
                throws RuntimeException {

            if (subTree.containsKey(this.key)) {

                this.parentTree = subTree;

                // stop the traversal when parent is found;
                throw new RuntimeException(FindParentTreeTraverser.SUCCESS);
            }
        }

        /**
         * Checks whether a given instance of {@link RuntimeException} has been
         * thrown by a {@link FindParentTreeTraverser} instance.
         *
         * @param ex  exception to be checked.
         *
         * @return
         *     <code>true</code> if and only if the given Exception results
         *     from an instance of this class.
         */
        public boolean hasThrown (final RuntimeException ex) {

            return !ex.getMessage().equals(FindParentTreeTraverser.SUCCESS);
        }

        @Override
        public void processPath () { }

        @Override
        public void subtractNode () { }

    }
}
