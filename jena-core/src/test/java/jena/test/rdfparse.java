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

package jena.test;

//import junit.swingui.TestRunner;

import org.apache.jena.rdfxml.xmlinput.ARPTests ;
import org.apache.jena.shared.Command ;

public class rdfparse implements Command
    {
    protected boolean internetTest;
    
    public rdfparse( boolean internetTest )
        { this.internetTest = internetTest; }
    
    @Override
    public Object execute()
        { ARPTests.internet = internetTest;
//        TestRunner.main( new String[] { "-noloading", ARPTests.class.getName()});
        return null; }
    }
