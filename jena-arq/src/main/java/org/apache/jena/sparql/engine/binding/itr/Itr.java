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

package org.apache.jena.sparql.engine.binding.itr;

import java.util.Iterator;

public class Itr {

    public static <X> Iterator<X> iter0() {
        return new Itr0<>();
    }

    public static <X> Iterator<X> iter1(X x1) {
        return new Itr1<>(x1);
    }

    public static <X> Iterator<X> iter2(X x1, X x2) {
        return new Itr2<>(x1, x2);
    }

    public static <X> Iterator<X> iter3(X x1, X x2, X x3) {
        return new Itr3<>(x1, x2, x3);
    }

    public static <X> Iterator<X> iter4(X x1, X x2, X x3, X x4) {
        return new Itr4<>(x1, x2, x3, x4);
    }
}
