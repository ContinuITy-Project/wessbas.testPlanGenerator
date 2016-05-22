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


package net.sf.markov4jmeter.testplangenerator.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * This class provides methods for reading and writing comma-separated-values
 * (CSV) files. The default values separator is a single comma, an alternative
 * separator might be passed to the constructor of this class. The default
 * newline pattern to be used for line-breaks in files to be written, is a
 * Windows-specific pattern (<code>"\r\n"</code>), but may be even specified
 * individually by passing a related constant to the constructor of this class.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public class CSVHandler {

    /** Type constant for Windows-specific newline patterns
     *  (<code>"\r\n"</code>). */
    public final static int LINEBREAK_TYPE_WINDOWS = 0;

    /** Type constant for Unix-specific newline patterns
     *  (<code>"\n"</code>). */
    public final static int LINEBREAK_TYPE_UNIX = 1;

    /** Type constant for MacOS-specific newline patterns
     *  (<code>"\r"</code>). */
    public final static int LINEBREAK_TYPE_MAC = 2;

    /** The default line-break type is associated with the Windows newline
     *  pattern (<code>"\r\n"</code>). */
    protected final static int DEFAULT_LINEBREAK_TYPE =
            CSVHandler.LINEBREAK_TYPE_WINDOWS;

    /** The default separator is a comma symbol. */
    protected final static String DEFAULT_SEPARATOR = ",";


    /* *************************  global variables  ************************* */


    /** Separator to be used in between values. */
    private final String separator;

    /** OS-specific newline pattern for line-breaks. */
    private final int lineBreakType;


    /* ***************************  constructors  *************************** */


    /**
     * Constructor for an CSV-Reader with a specific separator and an
     * OS-specific line-break type.
     *
     * @param separator
     *     value separator to be used.
     * @param lineBreakType
     *     OS-specific line-break type; this must be one of the
     *     <code>LINEBREAK_TYPE</code> constants defined in class
     *     {@link CSVHandler}.
     */
    public CSVHandler (final String separator, final int lineBreakType) {

        this.separator     = separator;
        this.lineBreakType = lineBreakType;
    }


    /**
     * Constructor for a CSV Handler which uses a default separator and a
     * default line-break type (Windows).
     */
    public CSVHandler () {

        this(CSVHandler.DEFAULT_SEPARATOR, CSVHandler.DEFAULT_LINEBREAK_TYPE);
    }

    /**
     * Constructor for a CSV Handler with an OS-specific line-break type.
     *
     * @param lineBreakType
     *     OS-specific line-break type; this must be one of the
     *     <code>LINEBREAK_TYPE</code> constants defined in class
     *     {@link CSVHandler}.
     */
    public CSVHandler (final int lineBreakType) {

        this(CSVHandler.DEFAULT_SEPARATOR, lineBreakType);
    }


    /**
     * Constructor for a CSV Handler with a specific separator and a default
     * line-break type (Windows).
     *
     * @param separator
     *     value separator to be used.
     */
    public CSVHandler (final String separator) {

        this(separator, CSVHandler.DEFAULT_LINEBREAK_TYPE);
    }


    /* **************************  public methods  ************************** */


    /**
     * Returns the separator which is used in between values.
     *
     * @return
     *     a valid <code>String</code> instance which denotes the used
     *     separator.
     */
    public String getSeparator () {

        return this.separator;
    }

    /**
     * Returns the used OS-specific line-break type.
     *
     * @return
     *     one of the <code>LINEBREAK_TYPE</code> constants defined in class
     *     {@link CSVHandler}.
     */
    public int getLineBreakType () {

        return this.lineBreakType;
    }

    /**
     * Reads values from a CSV-file which is specified by its name.
     *
     * @param filename
     *     name of the CSV-file whose content shall be read.
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

            if (bufferedReader != null) {

                try {

                    bufferedReader.close();

                } catch (final IOException ex) {

                    // ignore exception, since this is the "finally" block;
                    // TODO: exception message should be written to log file;
                }
            }
        }

        return values.toArray( new String[][]{} );
    }

    /**
     * Writes values to a CSV-file which is specified by its name.
     *
     * @param filePath
     *     path to the file whose content shall be written.
     * @param values
     *     values to be written.
     *
     * @throws FileNotFoundException
     *      if the file exists but is a directory rather than a regular file,
     *      does not exist but cannot be created, or cannot be opened for any
     *      other reason.
     * @throws IOException
     *     if an I/O error occurs.
     * @throws SecurityException
     *     if a security manager exists and its <code>checkWrite</code> method
     *     denies write access to the file.
     * @throws NullPointerException
     *     if <code>null</code> is passed as filename.
     */
    public void writeValues (final String filePath, final String[][] values)
            throws FileNotFoundException,
                   IOException,
                   SecurityException,
                   NullPointerException {

        // might throw a FileNotFoundException or a SecurityException;
        final FileOutputStream fos = new FileOutputStream(filePath);

        final OutputStreamWriter osw = new OutputStreamWriter(fos);
        final BufferedWriter bufferedWriter = new BufferedWriter(osw);

        try {

            for (int i = 0, n = values.length; i < n; i++) {

                final String line = this.joinValuesToSingleLine(values[i]);

                // might throw an IOException;
                bufferedWriter.write(line);
            }

        } finally {

            if (bufferedWriter != null) {

                try {

                    bufferedWriter.close();

                } catch (final IOException ex) {

                    // ignore exception, since this is the "finally" block;
                    // TODO: exception message should be written to log file;
                }
            }
        }
    }


    /* **************************  private methods  ************************* */


    /**
     * Joins a sequence of values to a single <code>String</code>, with
     * separators in between.
     *
     * @param values  values to be joined.
     *
     * @return  a valid <code>String</code> instance.
     */
    private String joinValuesToSingleLine (final String[] values) {

        final StringBuffer stringBuffer = new StringBuffer();

        final String lineBreakPattern =
                this.getLineBreakPattern(this.lineBreakType);

        for (int i = 0, n = values.length; i < n; i++) {

            if (i > 0) {

                stringBuffer.append(this.separator);
            }

            stringBuffer.append(values[i]);
        }

        return stringBuffer.append(lineBreakPattern).toString();
    }

    /**
     * Returns the OS-related newline pattern for the given type.
     *
     * @param lineBreakType
     *     one of the <code>LINEBREAK_TYPE</code> constants defined in class
     *     {@link CSVHandler}.
     *
     * @return
     *     The OS-related newline pattern for the given type.
     */
    private String getLineBreakPattern (final int lineBreakType) {

        switch (lineBreakType) {

            case LINEBREAK_TYPE_WINDOWS :
                return "\r\n";

            case LINEBREAK_TYPE_UNIX :
                return "\n";

            case LINEBREAK_TYPE_MAC :
                return "\r";

            default:
                return this.getLineBreakPattern(DEFAULT_LINEBREAK_TYPE);
        }
    }
}
