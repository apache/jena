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

package com.hp.hpl.jena.sparql.function.library;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionBase1 ;

import org.apache.jena.atlas.logging.Log ;

/** date(expression) => XSD dateTime 
 *  Attempt to convert an expression to an XSD dateTime.
 *  Supported conversions: Date as yyyy-mm-dd
 */ 

public class date extends FunctionBase1
{
    public date() {  }

    @Override
    public NodeValue exec(NodeValue v)
    {
        if ( ! v.isString() )
        {
            Log.warn(this, "date: argument not a string: "+v) ;
            throw new ExprEvalException("date: argument not a string: "+v) ;
        }
        
        String lexicalForm = v.getString() ;
        
        // Quite picky about format
        if ( ! lexicalForm.matches("\\d{4}-\\d{2}-\\d{2}") )
        {
            Log.warn(this, "date: argument not in date format: "+v) ;
            throw new ExprEvalException("date: argument not in date format: "+v) ;
        }
        
        lexicalForm=lexicalForm+"T00:00:00Z" ;
        
        NodeValue nv = NodeValue.makeNode(lexicalForm, XSDDatatype.XSDdateTime) ;
        return nv ;
    }
    
}
