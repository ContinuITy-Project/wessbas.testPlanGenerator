package net.sf.markov4jmeter.testplangenerator.util;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.QueryDelegate;
import org.eclipse.ocl.examples.pivot.delegate.OCLDelegateDomain;
import org.eclipse.ocl.examples.pivot.delegate.OCLInvocationDelegateFactory;
import org.eclipse.ocl.examples.pivot.delegate.OCLQueryDelegateFactory;
import org.eclipse.ocl.examples.pivot.delegate.OCLSettingDelegateFactory;
import org.eclipse.ocl.examples.pivot.delegate.OCLValidationDelegateFactory;
import org.eclipse.ocl.examples.pivot.model.OCLstdlib;
import org.eclipse.ocl.examples.xtext.completeocl.CompleteOCLStandaloneSetup;
import org.eclipse.ocl.examples.xtext.essentialocl.EssentialOCLStandaloneSetup;
import org.eclipse.ocl.examples.xtext.oclinecore.OCLinEcoreStandaloneSetup;
import org.eclipse.ocl.examples.xtext.oclstdlib.OCLstdlibStandaloneSetup;


/**
 * Stand-alone validator for Ecore objects, to be used for validating EMF
 * features (e.g., missing children) and OCL constraints as well.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class EcoreObjectValidator {

    /** Tabulator to be used for line indent. */
    private final static String TAB = "    ";


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for an Ecore Object Validator.
     */
    public EcoreObjectValidator () {

        this.init();
    }


    /* **************************  public methods  ************************** */


    /**
     * Validates a given Ecore object and prints the diagnostic results.
     *
     * @param eObject  Ecore object to be validated.
     *
     * @return  <code>true</code> if and only if the validation was successful.
     */
    public boolean validateAndPrintResult (final EObject eObject) {

        final Diagnostic diagnostic = this.validate(eObject);

        final boolean success = (diagnostic.getSeverity() == Diagnostic.OK);

        if (!success) {

            printDiagnostic(diagnostic);
        }

        return success;
    }

    /**
     * Validates a given Ecore object.
     *
     * @param eObject  Ecore object to be validated.
     *
     * @return  The diagnostic results.
     */
    public Diagnostic validate (final EObject eObject) {

        return Diagnostician.INSTANCE.validate(eObject);
    }

    /* **************************  private methods  ************************* */


    /**
     * Prints a given diagnostic result.
     *
     * @param diagnostic  The diagnostic result to be printed.
     */
    private void printDiagnostic (final Diagnostic diagnostic) {

        this.printDiagnostic(diagnostic, "");
    }

    /**
     * Prints a given diagnostic result instance with a leading indent.
     *
     * @param diagnostic  The diagnostic result to be printed.
     * @param indent      Indent to be inserted.
     */
    private void printDiagnostic (
            final Diagnostic diagnostic,
            final String indent) {

        System.out.println(indent + diagnostic.getMessage());

        for (final Diagnostic child : diagnostic.getChildren()) {

                this.printDiagnostic(child, indent + EcoreObjectValidator.TAB);
        }
    }

    /**
     * Initializes the stand-alone OCL validation.
     */
    private void init () {

        OCLstdlib.install();
        OCLstdlibStandaloneSetup.doSetup();
        OCLinEcoreStandaloneSetup.doSetup();
        CompleteOCLStandaloneSetup.doSetup();
        EssentialOCLStandaloneSetup.doSetup();

        String oclDelegateURI = OCLDelegateDomain.OCL_DELEGATE_URI_PIVOT;

        EOperation.Internal.InvocationDelegate.Factory.Registry.INSTANCE.put(
                oclDelegateURI, new OCLInvocationDelegateFactory.Global());

        EStructuralFeature.Internal.SettingDelegate.Factory.Registry.INSTANCE.put(
                oclDelegateURI, new OCLSettingDelegateFactory.Global());

        EValidator.ValidationDelegate.Registry.INSTANCE.put(
                oclDelegateURI, new OCLValidationDelegateFactory.Global());

        QueryDelegate.Factory.Registry.INSTANCE.put(
                oclDelegateURI, new OCLQueryDelegateFactory.Global());
    }
}