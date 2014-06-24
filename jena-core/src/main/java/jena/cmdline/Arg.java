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

package jena.cmdline;

import java.util.* ;

/** A command line argument that has been foundspecification.
 */
public class Arg
{
    String name ;
    String value ;
    List<String> values = new ArrayList<>() ;
    
    Arg() { name = null ; value = null ; }
    
    Arg(String _name) { this() ; setName(_name) ; }
    
    Arg(String _name, String _value) { this() ; setName(_name) ; setValue(_value) ; }
    
    void setName(String n) { name = n ; }
    
    void setValue(String v) { value = v ; }
    void addValue(String v) { values.add(v) ; }
    
    public String getName() { return name ; }
    public String getValue() { return value; }
    public List<String> getValues() { return values; }
    
    public boolean hasValue() { return value != null ; }
    
    public boolean matches(ArgDecl decl)
    {
        return decl.getNames().contains(name) ;
    }
    
        
}
