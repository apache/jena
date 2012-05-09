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

package com.hp.hpl.jena.sparql.path;

import com.hp.hpl.jena.graph.Node ;

public class PathFactory
{
    public static final long UNSET = P_Mod.UNSET ;
    
    public static Path pathLink(Node property)          { return new P_Link(property) ; }

    public static Path pathInverse(Path path)           { return new P_Inverse(path) ; }
    public static Path pathMod(Path path, long min, long max)   { return new P_Mod(path, min, max) ; }
    public static Path pathFixedLength(Path path, long count)   { return new P_FixedLength(path, count) ; }
    public static Path pathDistinct(Path path)          { return new P_Distinct(path) ; }
    public static Path pathMulti(Path path)             { return new P_Multi(path) ; }
    public static Path pathShortest(Path path)          { return new P_Shortest(path) ; }

    public static Path pathAlt(Path path1, Path path2)  { return new P_Alt(path1, path2) ; }
    public static Path pathSeq(Path path1, Path path2)  { return new P_Seq(path1, path2) ; }
    
    public static Path pathZeroOrOne(Path path)         { return new P_ZeroOrOne(path) ; }

    public static Path pathZeroOrMore1(Path path)       { return new P_ZeroOrMore1(path) ; }
    public static Path pathOneOrMore1(Path path)        { return new P_OneOrMore1(path) ; }
    public static Path pathZeroOrMoreN(Path path)       { return new P_ZeroOrMoreN(path) ; }
    public static Path pathOneOrMoreN(Path path)        { return new P_OneOrMoreN(path) ; }
}
