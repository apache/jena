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

package org.apache.jena.iri;

/**
 * Information concerning a
 * violation of some specification concerning IRIs.
 * This may be wrapped in 
 * an {@link IRIException} and thrown, 
 * or may be returned by
 * {@link IRI#violations(boolean)}. Which conditions
 * result in errors and warnings
 * depends on the setting of the related
 * {@link IRIFactory}.
 */
public abstract class  Violation implements ViolationCodes, IRIComponents {


    // TODO e-mail about dot-segments
    
    // TODO single script in a component
    /**
     * The value from {@link ViolationCodes}
     * corresponding to this condition.
     * @return An error code.
     */
    public abstract int getViolationCode();
    /**
     * The IRI that triggered this condition.
     * If an IRI has been constructed by
     * resolving a relative reference
     * against a base IRI then exceptions
     * associated with that IRI will have the
     * most informative value here, which can
     * be any of the three IRIs involved (the base IRI, the
     * relative IRI or the resolved IRI).
     * @return The IRI that triggered the error.
     */
    public abstract IRI getIRI();
    /**
     * A value from {@link IRIComponents}
     * indicating which component of  the IRI
     * is involved with this error.
     * @return A code indicating the IRI component in which the error occurred.
     */
    abstract public int getComponent();
    /**
     * A string version of the code number,
     * corresponding to the name of the java identifier.
     * @return The name of the java identifier of the error code for this error.
     */
    abstract public String codeName();
    /**
     * A short description of the error condition.
     * (Short is in comparison with {@link #getLongMessage()},
     * not an absolute value).
     * @return The error message.
     */
    abstract public String getShortMessage();
    
    /**
     * A long description of the error condition,
     * typically including the 
     * @return The error message.
     */
    abstract public String getLongMessage();
    
    
    /**
     * The URL of the 
     * section of the specification which has been violated.
     * @return The error message.
     */
    abstract public String getSpecificationURL();
    
    /**
     * Using the settings of the factory associated
     * with the IRI associated with this violation,
     * is this condition intended as an error (or as
     * a warning)?
     * @return true if this condition is an error, false if it is a warning.
     */
    public abstract boolean isError();
    /**
     * The name of the component in which the problem occurred.
     * @return A component name.
     */
    abstract public String component();
}
