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

package org.apache.jena.datatypes.xsd;

import org.apache.jena.shared.* ;

/**
 * Exception thrown when attempting to access a field of an XSDDateTime 
 * object that is not legal for the current date/time type. For example,
 * accessing the day from a gYearMonth object.
 */
public class IllegalDateTimeFieldException extends JenaException {
    
    /** Constructor */
    public IllegalDateTimeFieldException(String msg) {
        super(msg);
    }
}
