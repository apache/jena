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
package org.apache.jena.obfuscate.transform;

import org.apache.jena.obfuscate.ObfuscationProvider;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.E_StrReplace;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 *
 */
public class ExprTransformObfuscate extends ExprTransformCopy {
    
    private final ObfuscationProvider obfuscator;
    
    public ExprTransformObfuscate(ObfuscationProvider provider) {
        if (provider == null)
            throw new NullPointerException("Obfuscation Provider cannot be null");
        this.obfuscator = provider;
    }

    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        // Annoyingly ARQ validates REGEX() and REPLACE() functions as they are
        // created which means that when we obfuscate the arguments it breaks.
        // Therefore substitute a fake function in place of those functions
        if (func instanceof E_Regex) {
            return new E_Function("sparql:regex", args);
        } else if (func instanceof E_StrReplace) {
            return new E_Function("sparql:replace", args);
        } else {
            return super.transform(func, args);
        }
    }

    @Override
    public Expr transform(NodeValue nv) {
        return NodeValue.makeNode(this.obfuscator.obfuscateNode(nv.asNode()));
    }

    @Override
    public Expr transform(ExprVar exprVar) {
        return new ExprVar(this.obfuscator.obfuscateNode(exprVar.getAsNode()));
    }
}
