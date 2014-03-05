package net.sf.markov4jmeter.testplangenerator.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class provides methods for reading comma-separated-values (CSV) files.
 * The default separator is a single comma, an alternative separator might be
 * passed to the constructor of this class.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class CSVReader {

    /**
     * The default separator is a comma symbol.
     */
    protected final static String DEFAULT_SEPARATOR = ",";

    /**
     * Separator to be used.
     */
    private final String separator;


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for an CSV-Reader which uses the default separator.
     */
    public CSVReader () {

        this(CSVReader.DEFAULT_SEPARATOR);
    }

    /**
     * Constructor for an CSV-Reader which uses a specific separator.
     *
     * @param separator  separator to be used.
     */
    public CSVReader (final String separator) {

        this.separator = separator;
    }


    /* **************************  public methods  ************************** */


    /**
     * Returns the used separator.
     *
     * @return
     *     a valid <code>String</code> instance which denotes the used
     *     separator.
     */
    public String getSeparator () {

        return this.separator;
    }

    /**
     * Reads values from a CSV-file, which is specified by its name.
     *
     * @param filename
     *     name of the CSV-file to be read.
     *
     * @return
     *     the values which have been read, as an array of lines; each line
     *     might contain an individual amount of values.
     *
     * @throws FileNotFoundException
     *     in case the denoted file does not exist.
     * @throws IOException
     *     if any error while reading occurs.
     * @throws NullPointerException
     *     if <code>null</code> is passed as filename.
     */
    public String[][] readValues (final String filename)
            throws FileNotFoundException, IOException, NullPointerException {

        final ArrayList<String[]> values = new ArrayList<String[]>();

        BufferedReader bufferedReader = null;

        // might throw a FileNotFoundException or NullPointerException;
        final FileReader fileReader = new FileReader(filename);

        bufferedReader = new BufferedReader(fileReader);

        try {

            String line;

            // readLine() might throw an IOException;
            while ((line = bufferedReader.readLine()) != null) {

                final String[] tokens = (this.separator == null) ?
                        new String[]{line} : line.split(this.separator);

                for (int i = 0, n = tokens.length; i < n; i++) {

                    tokens[i] = tokens[i].trim();
                }

                values.add(tokens);
            }

        } finally {

            bufferedReader.close();
        }

        return values.toArray( new String[][]{} );
    }
}
