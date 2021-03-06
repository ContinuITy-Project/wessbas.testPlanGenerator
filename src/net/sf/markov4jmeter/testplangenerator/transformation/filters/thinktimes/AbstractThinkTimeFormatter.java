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


package net.sf.markov4jmeter.testplangenerator.transformation.filters.thinktimes;

import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.IllegalFormatException;
import java.util.Locale;

import m4jdsl.ThinkTime;

/**
 * Base class for all <code>ThinkTime</code> formatters.
 *
 * <p> A <code>ThinkTime</code> formatter provides methods for formatting
 * <code>ThinkTime</code> instances, returning representative
 * <code>String</code>s for the related <code>ThinkTime</code> type.
 *
 * @author   Eike Schulz (esc@informatik.uni-kiel.de)
 * @version  1.0
 */
public abstract class AbstractThinkTimeFormatter {


    /* **************************  public methods  ************************** */


    /**
     * Returns the default <code>String</code> representation for undefined
     * think time values.
     *
     * @return  a valid <code>String</code> instance.
     */
    public abstract String getThinkTimeString ();

    /**
     * Returns the <code>String</code> representation for a given
     * <code>ThinkTime</code> instance.
     *
     * @param thinkTime
     *     a <code>ThinkTime</code> instance for which a representative
     *     <code>String</code> shall be returned.
     *
     * @return
     *     a valid <code>String</code> instance.
     */
    public abstract String getThinkTimeString (final ThinkTime thinkTime);


    /* *************************  protected methods  ************************ */


    /**
     * Returns a <code>String</code> representation for a given
     * <code>double</code> value; the value will be formatted with a dot as
     * separator.
     *
     * @param value
     *     value to be formatted.
     *
     * @return
     *     a formatted <code>String</code> representing the given
     *     <code>double</code> value.
     */
    protected String formatDouble (final double value) {

        String doubleStr = null;  // to be returned;
        final Formatter doubleFormatter = new Formatter(Locale.US);

        try {

            // might throw an IllegalFormat- or FormatterClosedException;
            doubleStr = doubleFormatter.format("%.2f", value).toString();

        } catch (final IllegalFormatException|FormatterClosedException ex) {

            // exceptions will never be thrown here;

        } finally {

            doubleFormatter.close();
        }

        return doubleStr;
    }
}
