/**
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

package org.apache.jena.query.text ;

import java.util.function.Function;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.util.NodeFactoryExtra;

/** Class that converts TextHits to Bindings that can be returned from query */ 
public class TextHitConverter implements Function<TextHit, Binding>
{
    private Binding binding;
    private Var match;
    private Var score;
    private Var literal;

    public TextHitConverter(Binding binding, Var match, Var score, Var literal) {
        this.binding = binding;
        this.match = match;
        this.score = score;
        this.literal = literal;
    }
    
    @Override
    public Binding apply(TextHit hit) {
        if (score == null && literal == null)
            return BindingFactory.binding(binding, match, hit.getNode());
        BindingMap bmap = BindingFactory.create(binding);
        bmap.add(match, hit.getNode());
        if (score != null)
            bmap.add(score, NodeFactoryExtra.floatToNode(hit.getScore()));
        if (literal != null)
            bmap.add(literal, hit.getLiteral());
        return bmap;
    }
}
