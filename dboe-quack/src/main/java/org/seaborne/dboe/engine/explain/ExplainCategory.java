/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.explain;

import org.apache.jena.sparql.util.Symbol ;

// "Open enum"
public class ExplainCategory extends Symbol {
    private static final String baseName = "Explain/" ;
    static public ExplainCategory create(String symbolStr) { return new ExplainCategory(symbolStr) ; }

    private final String label ;
    
    protected ExplainCategory(String label) {
        super(baseName+label) ;
        this.label = label ;
    }
    public String getlabel() { return getSymbol(); }
}