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

package com.hp.hpl.jena.sparql.syntax;

import java.util.Collection ;
import java.util.LinkedHashSet ;

import com.hp.hpl.jena.sparql.core.Var ;

/** Get the variables potentially bound by an element.
 *  All mentioned variables except those in MINUS and FILTER (and hence NOT EXISTS)
 *  The work is done by PatternVarsVisitor.  
 */
public class PatternVars
{
    public static Collection<Var> vars(Element element) { return vars(new LinkedHashSet<Var>(), element) ; }

    public static Collection<Var> vars(Collection<Var> s, Element element)
    {
        PatternVarsVisitor v = new PatternVarsVisitor(s) ;
        vars(element, v) ;
        return s ;
    }
    
    public static void vars(Element element, PatternVarsVisitor visitor)
    {
        ElementWalker.Walker walker = new WalkerSkipMinus(visitor) ;
        ElementWalker.walk$(element, walker) ;
    }
    
    public static class WalkerSkipMinus extends ElementWalker.Walker
    {
        protected WalkerSkipMinus(ElementVisitor visitor)
        {
            super(visitor, null, null) ;
        }
        
        @Override
        public void visit(ElementMinus el)
        {
            // Don't go down the RHS of MINUS
            //if ( el.getMinusElement() != null )
            //    el.getMinusElement().visit(this) ;
            proc.visit(el) ;
        }
    }
}
