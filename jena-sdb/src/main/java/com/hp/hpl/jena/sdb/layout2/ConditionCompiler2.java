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

package com.hp.hpl.jena.sdb.layout2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.expr.Expr;

import com.hp.hpl.jena.sdb.compiler.ConditionCompiler;
import com.hp.hpl.jena.sdb.compiler.SDBConstraint;
import com.hp.hpl.jena.sdb.layout2.expr.RegexCompiler;
import com.hp.hpl.jena.sdb.layout2.expr.StringExprCompiler;

class ConditionCompiler2 implements ConditionCompiler
{
    private static Logger log = LoggerFactory.getLogger(ConditionCompiler2.class) ;
    
    public ConditionCompiler2() {}
    
    static ConditionCompiler reg[] = { 
        new RegexCompiler() ,
        new StringExprCompiler() ,
    } ;

    @Override
    public SDBConstraint recognize(Expr expr)
    {
        for ( ConditionCompiler aReg : reg )
        {
            SDBConstraint c = aReg.recognize( expr );
            if ( c != null )
            {
                return c;
            }
        }
        return null ;
    }
}
