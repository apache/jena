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

package com.hp.hpl.jena.rdf.model;

/** An RDF Bag container.
 *
 * <p>This interface defines methods for accessing RDF Bag resources.
 * These methods operate on the RDF statements contained in a model.  The
 * Bag implementation may cache state from the underlying model, so
 * objects should not be added to or removed a the Bag by directly
 * manipulating its properties, whilst the Bag is being
 * accessed through this interface.</p>
 *
 * <p>When a member is deleted from a Bag using this interface, or an
 * iterator returned through this interface, all the other members with
 * higher ordinals are renumbered using an implementation dependent
 * algorithm.</p>
 */


public interface Bag extends Container {

    /** Remove a value from the container.
     *
     * <p>The predicate of the statement <CODE>s</CODE> identifies the
     * ordinal of the value to be removed.  Once removed, the values in the
     * container with a higher ordinal value are renumbered.  The renumbering
     * algorithm is implementation dependent.<p>
     * @param s The statement to be removed from the model.
     * @return this container to enable cascading calls.
     */
    @Override
    public Container remove(Statement s);

    /** Remove a value from the container.
     *
     * <p>Any statement with an ordinal predicate and object <CODE>v</CODE>
     * may be selected and removed.  Once removed, the values in the
     * container with a higher ordinal value are renumbered.  The renumbering
     * algorithm is implementation dependent.<p>
     * @param v The value to be removed from the bag.
     * @return this container to enable cascading calls.
     */
//TODO    public Container remove(String v) ;
}
