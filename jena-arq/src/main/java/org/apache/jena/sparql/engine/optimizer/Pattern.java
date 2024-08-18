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

package org.apache.jena.sparql.engine.optimizer;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.io.Printable;
import org.apache.jena.sparql.sse.Item;

public class Pattern implements Printable {
    Item subjItem;
    Item predItem;
    Item objItem;
    double weight;

    public Pattern(double w, Item subj, Item pred, Item obj) {
        weight = w;
        subjItem = subj;
        predItem = pred;
        objItem = obj;
    }

    @Override
    public String toString() {
        return Printable.toString(this);
    }

    @Override
    public void output(IndentedWriter out) {
        out.print("(");
        out.print("(");
        out.print(subjItem.toString());
        out.print(" ");
        out.print(predItem.toString());
        out.print(" ");
        out.print(objItem.toString());
        out.print(")");
        out.print(" ");
        out.print(Double.toString(weight));
        out.print(")");
    }
}
