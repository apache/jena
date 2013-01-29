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

/** An RDF Property.
 */
public interface Property extends Resource {
	
  public boolean isProperty();

  /** Returns the namespace associated with this property.
     * @return The namespace for this property.
     */
  @Override
public String getNameSpace();

  /**
      Override RDFNode.inModel() to produce a staticly-typed Property
      in the given Model.
  */
  @Override
public Property inModel( Model m );
  
    /** Returns the name of this property within its namespace.
     * @return The name of this property within its namespace.
     */
  @Override
public String getLocalName();
  
  /** Returns the ordinal value of a containment property.
     *
     * <p>RDF containers use properties of the form _1, _2, _3 etc to represent
     * the containment relationship between the container and the objects it
     * contains.  When invoked on such a containment property, this method
     * returns the integer part of the property name.  When invoked on other
     * properties, it returns 0.
     * @return The ordinal value of a containment property,
     * or 0 otherwise.
     *
     */
  public int    getOrdinal();
}
