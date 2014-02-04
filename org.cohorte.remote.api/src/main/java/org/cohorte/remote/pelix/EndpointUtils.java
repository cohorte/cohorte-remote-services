/**
 * Copyright 2013 isandlaTech
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
 */
package org.cohorte.remote.pelix;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Utility methods for the endpoints
 * 
 * @author Thomas Calmant
 */
public class EndpointUtils {

    /** Default specification language */
    private static final String DEFAULT_LANGUAGE = "java";

    /**
     * Escapes the interface string: replaces '%2F' by slashes '/'
     * 
     * @param aSpecification
     *            Specification name
     * @return The escaped name
     */
    private static String escape(final String aSpecification) {

        return aSpecification.replaceAll("/", "%2F");
    }

    /**
     * Extract the language and the interface from a "language:/interface"
     * interface name
     * 
     * @param aSpec
     *            The formatted interface name
     * @return A [language, interface name] array
     */
    public static String[] extractSpecificationParts(final String aSpec) {

        // Parse the URI-like string
        final URI parsed = URI.create(aSpec);

        // Extract the interface name
        String specification = parsed.getPath();

        // Extract the language, if given
        String language = parsed.getScheme();
        if (language == null) {
            // Default language: Java
            language = DEFAULT_LANGUAGE;

        } else {
            // We got a real URI, so the name was escaped
            specification = unescape(specification.substring(1));
        }

        // Return an array
        return new String[] { language, specification };
    }

    /**
     * Converts "python:/name" specifications to "name". Keeps the other
     * specifications as is.
     * 
     * @param aSpecifications
     *            The specifications found in a remote registration
     * @return The filtered specifications
     */
    public static String[] extractSpecifications(final String[] aSpecifications) {

        final Collection<String> filteredSpecs = new LinkedHashSet<String>();

        for (final String spec : aSpecifications) {
            // Extract information
            final String[] parts = extractSpecificationParts(spec);
            if (DEFAULT_LANGUAGE.equals(parts[0])) {
                // Only keep Java specifications
                filteredSpecs.add(parts[1]);
            }
        }

        return filteredSpecs.toArray(new String[filteredSpecs.size()]);
    }

    /**
     * Formats a "language://interface" string
     * 
     * @param aLanguage
     *            Specification language
     * @param aSpecification
     *            Specification name
     * @return A formatted string
     */
    private static String formatSpecification(final String aLanguage,
            final String aSpecification) {

        // Escape the specification
        final String escapedSpec = escape(aSpecification);

        try {
            // Make a URI
            return new URI(aLanguage, null, escapedSpec, null).toString();

        } catch (final URISyntaxException ex) {
            // Shoudln't happen, but give a chance to the Python code to work
            return aLanguage + ":/" + escapedSpec;
        }
    }

    /**
     * Transforms interfaces names into URI strings, with the interface
     * implementation language as a scheme.
     * 
     * @param aFilteredSpecs
     *            Specifications to transform
     * @return The transformed names
     */
    public static Collection<String> formatSpecifications(
            final Collection<String> aFilteredSpecs) {

        final Collection<String> transformed = new LinkedList<String>();

        for (final String spec : aFilteredSpecs) {
            final String[] parts = extractSpecificationParts(spec);
            transformed.add(formatSpecification(parts[0], parts[1]));
        }

        return transformed;
    }

    /**
     * Unescapes the interface string: replaces '%2F' by slashes '/'
     * 
     * @param aSpecification
     *            Specification name
     * @return The unescaped name
     */
    private static String unescape(final String aSpecification) {

        return aSpecification.replaceAll("%2F", "/");
    }

    /**
     * Hidden constructor
     */
    private EndpointUtils() {

    }
}
