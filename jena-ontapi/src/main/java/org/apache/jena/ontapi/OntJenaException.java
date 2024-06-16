/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ontapi;

import org.apache.jena.shared.JenaException;

/**
 * A base ONT-API jena exception.
 * The class also contains some exception-related utilities.
 */
public class OntJenaException extends JenaException {

    public OntJenaException() {
        super();
    }

    public OntJenaException(String message) {
        super(message);
    }

    public OntJenaException(Throwable cause) {
        super(cause);
    }

    public OntJenaException(String message, Throwable cause) {
        super(message, cause);
    }

    public static <T> T notNull(T obj) {
        return notNull(obj, null);
    }

    public static <T> T notNull(T obj, String message) {
        if (obj == null)
            throw message == null ? new IllegalArgument() : new IllegalArgument(message);
        return obj;
    }

    /**
     * Stub for TO-DO.
     *
     * @param message error description
     * @param <X>     any
     * @return X
     */
    public static <X> X TODO(String message) {
        throw new RuntimeException(message);
    }

    /**
     * @param object any
     * @param <X>    any
     * @return the specified {@code object}
     * @throws IllegalStateException if {@code X} is {@code null}
     * @see java.util.Objects#requireNonNull(Object)
     */
    public static <X> X checkNotNull(X object) {
        if (object == null) {
            throw new IllegalStateException();
        }
        return object;
    }

    /**
     * @param object  any
     * @param message error description
     * @param <X>     any
     * @return the specified {@code object}
     * @throws IllegalStateException if {@code X} is {@code null}
     * @see java.util.Objects#requireNonNull(Object)
     */
    public static <X> X checkNotNull(X object, String message) {
        checkTrue(object != null, message);
        return object;
    }

    /**
     * @param mustBeFalse condition, if {@code true} and exception is expected
     * @param message     error description
     * @throws IllegalStateException if parameter condition is {@code true}
     */
    public static void checkFalse(Boolean mustBeFalse, String message) {
        if (mustBeFalse) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * @param mustBeTrue condition, if {@code false} and exception is expected
     * @param message    error description
     * @throws IllegalStateException if parameter condition is {@code false}
     */
    public static void checkTrue(Boolean mustBeTrue, String message) {
        if (!mustBeTrue) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * @param feature boolean
     * @throws Unsupported if {@code feature} is {@code false}
     */
    public static void checkSupported(boolean feature) {
        checkSupported(feature, null);
    }

    /**
     * @param feature boolean
     * @param message error description
     * @throws Unsupported if {@code feature} is {@code false}
     */
    public static void checkSupported(boolean feature, String message) {
        if (!feature) {
            throw new OntJenaException.Unsupported(message);
        }
    }

    /**
     * Exception that is thrown when an ontology resource is converted to another facet,
     * usually using {@link org.apache.jena.rdf.model.RDFNode#as as()},
     * and the requested conversion is not possible.
     */
    public static class Conversion extends OntJenaException {
        public Conversion(String message, Throwable cause) {
            super(message, cause);
        }

        public Conversion(String message) {
            super(message);
        }
    }

    /**
     * Exception, which may happen while creation of ont-object.
     */
    public static class Creation extends OntJenaException {
        public Creation(String message, Throwable cause) {
            super(message, cause);
        }

        public Creation(String message) {
            super(message);
        }
    }

    /**
     * An exception to indicate that a feature is not supported right now
     * or by design for current conditions.
     */
    public static class IllegalCall extends OntJenaException {
        public IllegalCall() {
            super();
        }

        public IllegalCall(String message) {
            super(message);
        }
    }

    /**
     * An exception that is thrown if a recursion is found in the graph.
     * Example of such a graph recursion:
     * <pre>{@code  _:b0 a owl:Class ; owl:complementOf  _:b0 .}</pre>
     */
    public static class Recursion extends OntJenaException {

        public Recursion(String message) {
            super(message);
        }
    }

    /**
     * A Jena exception that indicates wrong input.
     */
    public static class IllegalArgument extends OntJenaException {
        public IllegalArgument() {
            super();
        }

        public IllegalArgument(String message) {
            super(message);
        }

        public IllegalArgument(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * A Jena exception that indicates that Jena-object state is broken,
     * which may happen in multithreading or in other uncommon situations.
     */
    public static class IllegalState extends OntJenaException {
        public IllegalState() {
            super();
        }

        public IllegalState(String message) {
            super(message);
        }

        public IllegalState(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Exception that is raised when an ontology operation is attempted that is
     * not present in the configuration (e.g. language profile) for the current ontology model.
     */
    public static class Unsupported extends OntJenaException {
        public Unsupported(String message) {
            super(message);
        }
    }


}
