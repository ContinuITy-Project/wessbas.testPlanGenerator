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

import org.apache.commons.cli.Option;

/**
 * This factory creates command-line options. The options which can be created
 * differ by their number of arguments; the following options are available:
 * <ul>
 *   <li> Option with infinite number of arguments.
 *   <li> Option with one argument.
 *   <li> Option without any arguments.
 * </ul>
 *
 * @author
 * <ul>
 *   <li> Andre van Hoorn (avh@informatik.uni-kiel.de)
 *   <li> Eike Schulz (esc@informatik.uni-kiel.de)
 * </ul>
 *
 * @version 1.0 (2011-06-29)
 */
public class CmdlOptionFactory {

    /* IMPLEMENTATION NOTE:
     * --------------------
     * this class has been already implemented for the AspectLegacy tool, and
     * it has been adapted with the kind permission of Andre van Hoorn;
     *
     * http://sourceforge.net/projects/dynamod/files/dynamod.aspectlegacy/
     */

    /** Default separator between an argument name and an assigned value. */
    private final static char DEFAULT_VALUE_SEPARATOR = '=';


    /**
     * Creates an option with a specified number of arguments;
     *
     * @param opt
     *     Short representation of the option (e.g. <code>"s"</code>).
     * @param longOpt
     *     Long representation of the option
     *     (e.g. <code>"source-project"</code>).
     * @param description
     *     Description of the option
     *     (e.g. <code>"Path to source project."</code>).
     * @param isRequired
     *     Flag indicating whether the option is required (as a command-line
     *     argument) or not.
     * @param argName
     *     Display name for the argument value.
     * @param argsNum
     *     Number of arguments; a negative value indicates an infinite sequence
     *     of arguments.
     * @param hasOptionalArg
     *     <code>true</code> if and only if the arguments are optional.
     * @return
     *     An instance of {@link Option} with the specified properties.
     */
    public static Option createOption(
            final String opt,
            final String longOpt,
            final String description,
            final boolean isRequired,
            final String argName,
            final int argsNum,
            final boolean hasOptionalArg) {

        final Option option =
            new Option(opt, longOpt, argsNum != 0 /* hasArg */, description);

        option.setRequired(isRequired);

        if (argsNum != 0) {  // argsNum < 0 for infinite sequence of arguments;

            if (argName != null) {
                option.setArgName(argName);
            }
            // negative number of arguments implies an infinite sequence;
            option.setArgs((argsNum < 0) ? Integer.MAX_VALUE : argsNum);

            option.setValueSeparator(CmdlOptionFactory.DEFAULT_VALUE_SEPARATOR);
            option.setOptionalArg(hasOptionalArg);
        }
        return option;
    }

    /**
     * Creates an option with one argument.
     *
     * @param opt
     *     Short representation of the option (e.g. <code>"s"</code>).
     * @param longOpt
     *     Long representation of the option
     *     (e.g. <code>"source-project"</code>).
     * @param description
     *     Description of the option
     *     (e.g. <code>"Path to source project."</code>).
     * @param isRequired
     *     Flag indicating whether the option is required (as a command-line
     *     argument) or not.
     * @param argName
     *     Display name for the argument value.
     * @param hasOptionalArg
     *     <code>true</code> if and only if the arguments are optional.
     *
     * @return
     *     An instance of {@link Option} with the specified properties.
     */
    public static Option createOption(
            final String opt,
            final String longOpt,
            final String description,
            final boolean isRequired,
            final String argName,
            final boolean hasOptionalArg) {

        final Option option = CmdlOptionFactory.createOption(
            opt,
            longOpt,
            description,
            isRequired,
            argName,
            1,  // argsNum;
            hasOptionalArg);

        return option;
    }

    /**
     * Creates an option without any argument.
     *
     * @param opt
     *     Short representation of the option (e.g. <code>"s"</code>).
     * @param longOpt
     *     Long representation of the option
     *     (e.g. <code>"source-project"</code>).
     * @param description
     *     Description of the option
     *     (e.g. <code>"Path to source project."</code>).
     * @param isRequired
     *     Flag indicating whether the option is required (as a command-line
     *     argument) or not.
    *
     * @return
     *     An instance of {@link Option} with the specified properties.
     */
    public static Option createOption(
            final String opt,
            final String longOpt,
            final String description,
            final boolean isRequired) {

        final Option option = CmdlOptionFactory.createOption(
            opt,
            longOpt,
            description,
            isRequired,
            null,    // argName;
            0,       // argsNum;
            false);  // hasOptionalArg (false is an arbitrary value here);

        return option;
    }
}
