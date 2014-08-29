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

/*
 * Location.java
 *
 * Created on July 14, 2001, 11:47 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;
import org.xml.sax.Locator;

public class ARPLocation implements Locator {
    public final String inputName;
    final String publicId;
    public final int endLine;
    public final int endColumn;
    ARPLocation(Locator locator) {
    	if (locator==null){
    	  inputName = "unknown-source";
    	  publicId = "unknown-source";
    	  endLine = -1;
    	  endColumn = -1;
    	}else {
        inputName = locator.getSystemId();
        endLine = locator.getLineNumber();
        endColumn = locator.getColumnNumber();
        publicId = locator.getPublicId();
    	}
    }
    @Override
    public String toString() {
        return //"before column " + endColumn +
        "line " + endLine + " in '"
        + inputName + "'";
    }
    @Override
    public String getSystemId() {
        return inputName;
    }
    @Override
    public int getLineNumber() {
        return endLine;
    }
    @Override
    public int getColumnNumber() {
        return endColumn;
    }
    @Override
    public String getPublicId() {
        return publicId;
    }
    
}
