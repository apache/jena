/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ext.xerces.impl.dv;

import org.apache.jena.ext.xerces.util.SymbolHash;
import org.apache.jena.ext.xerces.xs.XSObjectList;

/**
 * Defines a factory API that enables applications to <p>
 * 1. to get the instance of specified SchemaDVFactory implementation <p>
 * 2. to create/return built-in schema simple types <p>
 * 3. to create user defined simple types. <p>
 *
 * Implementations of this abstract class can be used to get built-in simple
 * types and create user-defined simle types. <p>
 *
 * The implementation should store the built-in datatypes in static data, so
 * that they can be shared by multiple parser instance, and multiple threads.
 * 
 * {@literal @xerces.internal} 
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id: SchemaDVFactory.java 558582 2007-07-23 02:05:04Z mrglavas $
 */
public abstract class SchemaDVFactory {
    
    // JENA-1670
    //private static final String DEFAULT_FACTORY_CLASS = "org.apache.jena.ext.xerces.impl.dv.xs.SchemaDVFactoryImpl";
    static SchemaDVFactory INSTANCE = new org.apache.jena.ext.xerces.impl.dv.xs.SchemaDVFactoryImpl();
    
    /**
     * Get a default instance of SchemaDVFactory implementation.
     *
     * @return  an instance of SchemaDVFactory implementation
     */
    public static final SchemaDVFactory getInstance() {
        return INSTANCE;
    }

    // can't create a new object of this class
    protected SchemaDVFactory() {}

    /**
     * Get a built-in simple type of the given name
     * REVISIT: its still not decided within the Schema WG how to define the
     *          ur-types and if all simple types should be derived from a
     *          complex type, so as of now we ignore the fact that anySimpleType
     *          is derived from anyType, and pass 'null' as the base of
     *          anySimpleType. It needs to be changed as per the decision taken.
     *
     * @param name  the name of the datatype
     * @return      the datatype validator of the given name
     */
    public abstract XSSimpleType getBuiltInType(String name);

    /**
     * get all built-in simple types, which are stored in a SymbolHash keyed by
     * the name
     *
     * @return      a SymbolHash which contains all built-in simple types
     */
    public abstract SymbolHash getBuiltInTypes();

    /**
     * Create a new simple type which is derived by restriction from another
     * simple type.
     *
     * @param name              name of the new type, could be null
     * @param targetNamespace   target namespace of the new type, could be null
     * @param finalSet          value of "final"
     * @param base              base type of the new type
     * @param annotations       set of annotations
     * @return                  the newly created simple type
     */
    public abstract XSSimpleType createTypeRestriction(String name, String targetNamespace,
                                                       short finalSet, XSSimpleType base,
                                                       XSObjectList annotations);

    /**
     * Create a new simple type which is derived by list from another simple
     * type.
     *
     * @param name              name of the new type, could be null
     * @param targetNamespace   target namespace of the new type, could be null
     * @param finalSet          value of "final"
     * @param itemType          item type of the list type
     * @param annotations       set of annotations
     * @return                  the newly created simple type
     */
    public abstract XSSimpleType createTypeList(String name, String targetNamespace,
                                                short finalSet, XSSimpleType itemType,
                                                XSObjectList annotations);

    /**
     * Create a new simple type which is derived by union from a list of other
     * simple types.
     *
     * @param name              name of the new type, could be null
     * @param targetNamespace   target namespace of the new type, could be null
     * @param finalSet          value of "final"
     * @param memberTypes       member types of the union type
     * @param annotations       set of annotations
     * @return                  the newly created simple type
     */
    public abstract XSSimpleType createTypeUnion(String name, String targetNamespace,
                                                 short finalSet, XSSimpleType[] memberTypes,
                                                 XSObjectList annotations);

}
