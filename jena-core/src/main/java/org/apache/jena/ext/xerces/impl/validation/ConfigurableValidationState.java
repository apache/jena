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

/**
 * <p>An extension of ValidationState which can be configured to turn 
 * off checking for ID/IDREF errors and unparsed entity errors.</p>
 * 
 * {@literal @xerces.internal}
 *
 * @author Peter McCracken, IBM
 * @version $Id: ConfigurableValidationState.java 449320 2006-09-23 22:37:56Z mrglavas $
 */
@SuppressWarnings("all")
public final class ConfigurableValidationState extends ValidationState {
    
    /**
     * Whether to check for ID/IDREF errors
     */
    private boolean fIdIdrefChecking;
    
    /**
     * Whether to check for unparsed entity errors
     */
    private boolean fUnparsedEntityChecking;
    
    /**
     * Creates a new ConfigurableValidationState.
     * By default, error checking for both ID/IDREFs 
     * and unparsed entities are turned on.
     */
    public ConfigurableValidationState() {
        super();
        fIdIdrefChecking = true;
        fUnparsedEntityChecking = true;
    }
    
    /**
     * Turns checking for ID/IDREF errors on and off.
     * @param setting true to turn on error checking,
     *                 false to turn off error checking
     */
    public void setIdIdrefChecking(boolean setting) {
        fIdIdrefChecking = setting;
    }
    
    /**
     * Turns checking for unparsed entity errors on and off.
     * @param setting true to turn on error checking,
     *                 false to turn off error checking
     */
    public void setUnparsedEntityChecking(boolean setting) {
        fUnparsedEntityChecking = setting;
    }
    
    /**
     * Checks if all IDREFs have a corresponding ID.
     * @return null, if ID/IDREF checking is turned off
     *         otherwise, returns the value of the super implementation
     */
    public String checkIDRefID() {
        return (fIdIdrefChecking) ? super.checkIDRefID() : null;
    }
    
    /**
     * Checks if an ID has already been declared.
     * @return false, if ID/IDREF checking is turned off
     *         otherwise, returns the value of the super implementation
     */
    public boolean isIdDeclared(String name) {
        return (fIdIdrefChecking) ? super.isIdDeclared(name) : false;
    }
    
    /**
     * Checks if an entity is declared.
     * @return true, if unparsed entity checking is turned off
     *         otherwise, returns the value of the super implementation
     */
    public boolean isEntityDeclared(String name) {
        return (fUnparsedEntityChecking) ? super.isEntityDeclared(name) : true;
    }
    
    /**
     * Checks if an entity is unparsed.
     * @return true, if unparsed entity checking is turned off
     *         otherwise, returns the value of the super implementation
     */
    public boolean isEntityUnparsed(String name) {
        return (fUnparsedEntityChecking) ? super.isEntityUnparsed(name) : true;
    }
    
    /**
     * Adds the ID, if ID/IDREF checking is enabled.
     * @param name the ID to add
     */
    public void addId(String name) {
        if (fIdIdrefChecking) {
            super.addId(name);
        }
    }
    
    /**
     * Adds the IDREF, if ID/IDREF checking is enabled.
     * @param name the IDREF to add
     */
    public void addIdRef(String name) {
        if (fIdIdrefChecking) {
            super.addIdRef(name);
        }
    }
}
