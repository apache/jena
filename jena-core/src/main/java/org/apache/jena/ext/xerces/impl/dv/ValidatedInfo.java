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

/**
 * Class to get the information back after content is validated. This info
 * would be filled by validate().
 * 
 * {@literal @xerces.internal} 
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 *
 * @version $Id: ValidatedInfo.java 1026362 2010-10-22 15:15:18Z sandygao $
 */
public class ValidatedInfo {

    /**
     * The normalized value of a string value
     */
    public String normalizedValue;

    /**
     * The actual value from a string value (QName, Boolean, etc.)
     * An array of Objects if the type is a list.
     */
    public Object actualValue;

    /**
     * The type of the actual value. It's one of the _DT constants
     * defined in XSConstants.java. The value is used to indicate
     * the most specific built-in type.
     * (i.e. short instead of decimal or integer).
     */
    public short actualValueType;
}
