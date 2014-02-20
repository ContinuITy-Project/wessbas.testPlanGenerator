package net.sf.markov4jmeter.testplangenerator.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

/**
 * This service class simplifies the reading of XMI-files as Ecore-models and
 * provides methods for writing them back.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class XmiEcoreHandler {

    /** Error message for the case that a file reading error occurred. */
    private final static String ERROR_RESOURCE_READING_FAILED =
            "Could not read resource \"%s\": %s";

    /** Error message for the case that a file writing error occurred. */
    private final static String ERROR_RESOURCE_WRITING_FAILED =
            "Could not write resource \"%s\": %s";


    /* **************************  public methods  ************************** */


    /**
     * Writes an Ecore-model to an XMI-file and optionally registers a file
     * extension in the XMI resource factory.
     *
     * <p><u>Important:</u>
     * the model-related Ecore package must have been initialized before!
     *
     * @param model
     *     The Ecore-model which shall be written to the specified XMI-file.
     * @param xmiFile
     *     XMI-file to be written.
     * @param extension
     *     File extension to be optionally registered in the XMI resource
     *     factory; this might be even <code>null</code>, if no extension shall
     *     be registered.
     *
     * @throws IOException  if any error while writing occurs.
     */
    public void ecoreToXMI (
            final EObject model,
            final String xmiFile,
            final String extension) throws IOException{

        final ResourceSet resourceSet = new ResourceSetImpl();

        // Collections.EMPTY_MAP might be used for an empty options set, too;
        final Map<String, Object> options = new HashMap<String, Object>();

        // TODO: the attribute for the "schema location" is lost when the
        // XMI-file is being saved; for example, the necessary assignment
        //
        //   xsi:schemaLocation="http://m4jdsl/1.0 m4jdsl.ecore"
        //
        // is not included in the result file for an "m4jdsl" model; therewith,
        // the resulting XMI-file cannot be loaded into the Generic EMF Form
        // Editor; the following option needs to be set therefore:
        //
        //   options.put(XMIResource.OPTION_SCHEMA_LOCATION, true);
        //
        // however, this is not enough, since the generated meta-model needs to
        // be modified additionally; this has been not been implemented yet,
        // for leaving the generated code as it is;

        if (extension != null) {

            // register extension in the XMI resource factory;
            // might throw a NullPointerException (should never happen here);
            this.registerExtension(extension);
        }

        try {

            // might throw an IllegalArgumentException;
            final URI uri = URI.createURI(xmiFile);

            // create a new resource for the output to be written;
            final Resource resource = resourceSet.createResource(uri);

            // add the given model to the content to be written;
            resource.getContents().add(model);

            // save without any options; might throw an IOException;
            resource.save(options);

        } catch (final Exception ex) {

            final String message = String.format(
                    XmiEcoreHandler.ERROR_RESOURCE_WRITING_FAILED,
                    xmiFile,
                    ex.getMessage());

            throw new IOException(message);
        }
    }

    /**
     * Writes an Ecore-model to an XMI-file.
     *
     * @param model
     *     The Ecore-model which shall be written to the specified XMI-file.
     * @param xmiFile
     *     XMI-file to be written.
     *
     * @throws IOException  if any error while writing occurs.
     */
    public void ecoreToXMI (final EObject model, final String xmiFile)
            throws IOException {

        this.ecoreToXMI(model, xmiFile, null);
    }

    /**
     * Reads an Ecore-model from an XMI-file and optionally registers a file
     * extension in the XMI resource factory.
     *
     * <p><u>Important:</u>
     * the model-related Ecore package must have been initialized before!
     *
     * @param xmiFile
     *     XMI-file to be read.
     * @param extension
     *     File extension to be optionally registered in the XMI resource
     *     factory; this might be even <code>null</code>, if no extension shall
     *     be registered.
     *
     * @return  The Ecore-model which has been read from the specified XMI-file.
     *
     * @throws IOException  if any error while reading occurs.
     */
    public EObject xmiToEcore (
            final String xmiFile, final String extension) throws IOException {

        // to be returned;
        final EObject model;

        // resource to be read;
        final Resource resource;

        if (extension != null) {

            // register extension in the XMI resource factory;
            // might throw a NullPointerException (should never happen here);
            this.registerExtension(extension);
        }

        // might throw an IOException;
        resource =  this.readResource(xmiFile);

        // first model element must be of type WorkloadModel;
        // might throw an IndexOutOfBoundsException (should never happen here);
        model = resource.getContents().get(0);

        return model;
    }

    /**
     * Reads an Ecore-model from an XMI-file.
     *
     * <p><u>Important:</u>
     * the model-related Ecore package must have been initialized before!
     *
     * @param xmiFile  XMI-file to be read.
     *
     * @return  The Ecore-model which has been read from the given XMI-file.
     *
     * @throws IOException  if any error while reading occurs.
     */
    public EObject xmiToEcore (final String xmiFile) throws IOException {

        return this.xmiToEcore(xmiFile, null);
    }


    /* **************************  private methods  ************************* */


    /**
     * Registers a file extension in the XMI resource factory.
     *
     * @param extension  File extension to be registered.
     *
     * @throws NullPointerException
     *     if <code>null</code> has been passed as <code>extension</code>.
     */
    private void registerExtension (final String extension)
            throws NullPointerException{

        final Resource.Factory.Registry registry =
                Resource.Factory.Registry.INSTANCE;

        final Map<String, Object> map = registry.getExtensionToFactoryMap();

        // might throw an UnsupportedOperation-, ClassCast-, NullPointer- or
        // IllegalArgumentException (anything else but a NullPointerException
        // should never be thrown here, since "extension" is of type String);
        map.put(extension, new XMIResourceFactoryImpl());
    }

    /**
     * Reads an XMI-file from a given location.
     *
     * @param xmiFile  Location of the XMI-file to be read.
     *
     * @return  The content which has been read from file.
     *
     * @throws IOException  if any error while reading occurs.
     */
    private Resource readResource (final String xmiFile) throws IOException {

        final Resource resource;  // to be returned;

        final ResourceSet resourceSet = new ResourceSetImpl();

        try {

            // might throw an IllegalArgumentException;
            final URI uri = URI.createURI(xmiFile);

            // read resource with "loadOnDemand" being true;
            // might throw a Runtime- or WrappedException;
            resource = resourceSet.getResource(uri, true);

        } catch (final Exception ex) {

            final String message = String.format(
                    XmiEcoreHandler.ERROR_RESOURCE_READING_FAILED,
                    xmiFile,
                    ex.getMessage());

            throw new IOException(message);
        }

        return resource;
    }
}
