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

package org.apache.jena.fuseki.mgt;

public class PageNames
{
    //public static final String pageControlPanel     = "/control-panel.tpl" ;
    
    /** The dispatch URL for the place to go after login (0.2.1 - login = choose dataset) */
    public static final String pageAfterLogin      = "/sparql.tpl" ;
    
    /** This is the full web dispatch URL: control-panel.tpl knowns 
     * the name indirectly because it gets it via velocity
     * calling mgt.actionDataset.
     */
    public static final String actionDatasetNames   = "/$/datasets" ;
}
