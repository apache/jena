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

package com.hp.hpl.jena.sparql.expr;

public enum ValueSpaceClassification {
    VSPACE_NODE,
    VSPACE_NUM, 
    VSPACE_DATETIME, 
    VSPACE_DATE,
    VSPACE_TIME,
    VSPACE_DURATION,
    
    // Collapse to VSPACE_DATETIME?
    VSPACE_G_YEAR,
    VSPACE_G_YEARMONTH,
    VSPACE_G_MONTHDAY,
    VSPACE_G_MONTH,    
    VSPACE_G_DAY,
    
    VSPACE_STRING, VSPACE_LANG,
    VSPACE_BOOLEAN,
    VSPACE_UNKNOWN,
    VSPACE_DIFFERENT
}
