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
 * LanguageTagSyntaxException.java
 *
 * Created on July 25, 2001, 9:32 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.lang;

/**
 * A LanguageTag did not conform to RFC3066.
  This exception is for the
 * syntactic rules of RFC3066 section 2.1.
 */
public class LanguageTagSyntaxException extends java.lang.Exception {


    /**
     * 
     */
    private static final long serialVersionUID = 5425207434895448094L;

    /**
 * Constructs an <code>LanguageTagSyntaxException</code> with the specified detail message.
     * @param msg the detail message.
     */
    LanguageTagSyntaxException(String msg) {
        super(msg);
    }
}
