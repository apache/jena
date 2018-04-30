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

package org.apache.jena.ext.xerces.impl.validation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.jena.ext.xerces.impl.dv.ValidationContext;
import org.apache.jena.ext.xerces.util.SymbolTable;
import org.apache.jena.ext.xerces.xni.NamespaceContext;

/**
 * Implementation of the ValidationContext interface. Used to establish an
 * environment for simple type validation.
 * 
 * @xerces.internal
 *
 * @author Elena Litani, IBM
 * @version $Id: ValidationState.java 713638 2008-11-13 04:42:18Z mrglavas $
 */
@SuppressWarnings("all")
public class ValidationState implements ValidationContext {

    //
    // private data
    //
    private boolean fExtraChecking              = true;
    private boolean fFacetChecking              = true;
    private boolean fNormalize                  = true;
    private boolean fNamespaces                 = true;

    private EntityState fEntityState            = null;
    private NamespaceContext fNamespaceContext  = null;
    private SymbolTable fSymbolTable            = null;
    private Locale fLocale                      = null;

    //REVISIT: Should replace with a lighter structure.
    private final HashMap fIdTable    = new HashMap();
    private final HashMap fIdRefTable = new HashMap();
    private final static Object fNullValue = new Object();

    //
    // public methods
    //
    public void setExtraChecking(boolean newValue) {
        fExtraChecking = newValue;
    }

    public void setFacetChecking(boolean newValue) {
        fFacetChecking = newValue;
    }

    public void setNormalizationRequired (boolean newValue) {
          fNormalize = newValue;
    }

    public void setUsingNamespaces (boolean newValue) {
          fNamespaces = newValue;
    }

    public void setEntityState(EntityState state) {
        fEntityState = state;
    }

    public void setNamespaceSupport(NamespaceContext namespace) {
        fNamespaceContext = namespace;
    }

    public void setSymbolTable(SymbolTable sTable) {
        fSymbolTable = sTable;
    }

    /**
     * return null if all IDREF values have a corresponding ID value;
     * otherwise return the first IDREF value without a matching ID value.
     */
    public String checkIDRefID () {
        Iterator iter = fIdRefTable.keySet().iterator();
        String key;
        while (iter.hasNext()) {
            key = (String) iter.next();
            if (!fIdTable.containsKey(key)) {
                  return key;
            }
        }
        return null;
    }

    public void reset () {
        fExtraChecking = true;
        fFacetChecking = true;
        fNamespaces = true;
        fIdTable.clear();
        fIdRefTable.clear();
        fEntityState = null;
        fNamespaceContext = null;
        fSymbolTable = null;
    }

    /**
     * The same validation state can be used to validate more than one (schema)
     * validation roots. Entity/Namespace/Symbol are shared, but each validation
     * root needs its own id/idref tables. So we need this method to reset only
     * the two tables.
     */
    public void resetIDTables() {
        fIdTable.clear();
        fIdRefTable.clear();
    }

    //
    // implementation of ValidationContext methods
    //

    // whether to do extra id/idref/entity checking
    @Override
    public boolean needExtraChecking() {
        return fExtraChecking;
    }

    // whether to validate against facets
    @Override
    public boolean needFacetChecking() {
        return fFacetChecking;
    }

    @Override
    public boolean needToNormalize (){
        return fNormalize;
    }

    @Override
    public boolean useNamespaces() {
        return fNamespaces;
    }

    // entity
    @Override
    public boolean isEntityDeclared (String name) {
        if (fEntityState !=null) {
            return fEntityState.isEntityDeclared(getSymbol(name));
        }
        return false;
    }
    @Override
    public boolean isEntityUnparsed (String name) {
        if (fEntityState !=null) {
            return fEntityState.isEntityUnparsed(getSymbol(name));
        }
        return false;
    }

    // id
    @Override
    public boolean isIdDeclared(String name) {
        return fIdTable.containsKey(name);
    }
    @Override
    public void addId(String name) {
        fIdTable.put(name, fNullValue);
    }

    // idref
    @Override
    public void addIdRef(String name) {
        fIdRefTable.put(name, fNullValue);
    }
    // get symbols

    @Override
    public String getSymbol (String symbol) {
        if (fSymbolTable != null)
            return fSymbolTable.addSymbol(symbol);
        // if there is no symbol table, we return java-internalized string,
        // because symbol table strings are also java-internalzied.
        // this guarantees that the returned string from this method can be
        // compared by reference with other symbol table string. -SG
        return symbol.intern();
    }
    // qname, notation
    @Override
    public String getURI(String prefix) {
        if (fNamespaceContext !=null) {
            return fNamespaceContext.getURI(prefix);
        }
        return null;
    }
    
    // Locale
    
    public void setLocale(Locale locale) {
        fLocale = locale;
    }
    
    @Override
    public Locale getLocale() {
        return fLocale;
    }
}
