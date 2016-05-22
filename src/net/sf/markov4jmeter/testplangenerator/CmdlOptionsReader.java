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



package net.sf.markov4jmeter.testplangenerator;

import org.apache.commons.cli.CommandLine;

/**
 * This class provides methods for reading option-values from a certain instance
 * of {@link CommandLine}. The <code>getOptionValuesAs...()</code>-methods of
 * this class can be used for requesting option-values of certain types.
 *
 * @author
 * <ul>
 *   <li> Andre van Hoorn (avh@informatik.uni-kiel.de)
 *   <li> Eike Schulz (esc@informatik.uni-kiel.de)
 * </ul>
 *
 * @version 1.0 (2011-06-29)
 */
public class CmdlOptionsReader {

    /* IMPLEMENTATION NOTE:
     * --------------------
     * this class has been already implemented for the AspectLegacy tool, and
     * it has been adapted with the kind permission of Andre van Hoorn;
     *
     * http://sourceforge.net/projects/dynamod/files/dynamod.aspectlegacy/
     */

    /** Command-line to be read from. */
    private final CommandLine commandLine;


    /**
     * Constructor of a command-line options reader.
     *
     * @param commandLine  Command-line to be read from.
     */
    public CmdlOptionsReader (final CommandLine commandLine) {

        this.commandLine = commandLine;
    }


    /**
     * Reads the value of an option from the command-line as a {@link String}.
     *
     * @param opt
     *     Short or long representation of the option
     *     (e.g. <code>"mo"</code> or <code>"my-option"</code>).
     *
     * @return
     *     The value of the specified option as a <code>String</code>.
     *
     * @throws NullPointerException
     *     if <code>null</code> has been passed as parameter, or if the value
     *     of the specified option is undefined (<code>null</code>).
     * @throws IllegalArgumentException
     *     if the value of the specified option is an empty <code>String</code>
     *     (<code>""</code>).
     */
    public String readOptionValueAsString (final String opt)
        throws NullPointerException, IllegalArgumentException {

		final String stringValue = this.commandLine.getOptionValue(opt);

		// value should be defined and should not be empty;
		if (stringValue == null) {

	        throw new NullPointerException(
                "Value of option '" + opt + "' is undefined (null)");
		}
		if ( stringValue.isEmpty() ) {

		    throw new IllegalArgumentException(
		        "Value of option '" + opt + "' is an empty String");
		}
		return stringValue;
	}

    /**
     * Reads the sequence of values of an option from the command-line as
     * {@link String}s.
     *
     * @param opt
     *     Short or long representation of the option
     *     (e.g. <code>"mo"</code> or <code>"my-option"</code>).
     *
     * @return
     *     The values of the specified option as an array of <code>String</code>
     *     instances.
     *
     * @throws NullPointerException
     *     if <code>null</code> has been passed as parameter, or if the values
     *     array of the specified option is undefined (<code>null</code>).
     * @throws IllegalArgumentException
     *     if the values of the specified option are unavailable (empty array).
     */
	public String[] readOptionValuesAsStrings (final String opt)
        throws NullPointerException, IllegalArgumentException {

		final String[] values = this.commandLine.getOptionValues(opt);

		// array of values should be not null and should not be empty;
		if (values == null) {

		    throw new NullPointerException(
	            "Values of option '" + opt + "' is null");
		}
		if (values.length == 0) {

		    throw new IllegalArgumentException(
	            "Values of option '" + opt + "' are unavailable (empty array)");
		}
		return values;
	}

	/**
     * Reads the value of an option from the command-line as a
     * <code>boolean</code> value.
     *
     * @param opt
     *     Short or long representation of the option
     *     (e.g. <code>"mo"</code> or <code>"my-option"</code>).
     *
     * @return
     *     The value of the specified option as a <code>boolean</code> value.
     *
     * @throws NullPointerException
     *     if <code>null</code> has been passed as parameter, or if the value
     *     of the specified option is undefined (<code>null</code>).
	 */
	public boolean readOptionValueAsBoolean (final String opt)
        throws NullPointerException {

		final String stringValue = this.commandLine.getOptionValue(opt);

		// ensure that value is defined;
		if (stringValue == null) {

		    throw new NullPointerException(
		        "Value of option '" + opt + "' is null");
		}
		return Boolean.parseBoolean(stringValue); // result is always valid;
	}

    /**
     * Reads the value of an option from the command-line as a
     * <code>boolean</code> value and returns a default value, if reading fails.
     *
     * @param opt
     *     Short or long representation of the option
     *     (e.g. <code>"mo"</code> or <code>"my-option"</code>).
     * @param defaultValue
     *     Default value to be returned, if reading fails.
     *
     * @return
     *     The value of the specified option as a <code>boolean</code> value;
     *     if reading fails, the passed default value will be returned.
     */
	public boolean readOptionValueAsBoolean (final String opt,
	                                         final boolean defaultValue) {
	    final String stringValue =
			commandLine.getOptionValue(opt, Boolean.toString(defaultValue));

		return Boolean.parseBoolean(stringValue);
	}

    /**
     * Reads the value of an option from the command-line as an
     * <code>int</code> value.
     *
     * @param opt
     *     Short or long representation of the option
     *     (e.g. <code>"mo"</code> or <code>"my-option"</code>).
     *
     * @return
     *     The value of the specified option as an <code>int</code> value.
     *
     * @throws NullPointerException
     *     if <code>null</code> has been passed as parameter, or if the value
     *     of the specified option is undefined (<code>null</code>).
     * @throws NumberFormatException
     *     in case parsing of the option value as an <code>int</code> fails.
     */
	public int readOptionValueAsInt (final String opt)
        throws NullPointerException, NumberFormatException {

		final String stringValue = this.commandLine.getOptionValue(opt);

		// ensure that value is defined;
		if (stringValue == null) {

		    throw new NullPointerException(
	            "Value of option '" + opt + "' is null");
		}
		return Integer.parseInt(stringValue);
	}

    /**
     * Reads the value of an option from the command-line as an
     * <code>int</code> value and returns a default value, if reading fails.
     *
     * @param opt
     *     Short or long representation of the option
     *     (e.g. <code>"mo"</code> or <code>"my-option"</code>).
     * @param defaultValue
     *     Default value to be returned, if reading fails.
     *
     * @return
     *     The value of the specified option as an <code>int</code> value;
     *     if reading fails, the passed default value will be returned.
     *
     * @throws NumberFormatException
     *     in case parsing of the option value as an <code>int</code> fails.
     */
	public int readOptionValueAsInt(
			final String opt,
			final int defaultValue) throws NumberFormatException {

		final String stringValue =
            this.commandLine.getOptionValue(opt, Integer.toString(defaultValue));

		return Integer.parseInt(stringValue);
	}
}
