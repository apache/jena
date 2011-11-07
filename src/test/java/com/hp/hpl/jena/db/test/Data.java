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

// This file contains binary data.
// It must be checked into CVS/SVN so that it is binary-safe.

package com.hp.hpl.jena.db.test;

public class Data
{
    // This file is binary.
    // � is UTF8 ef bf bd = OxFFFD = Unicode Character 'REPLACEMENT CHARACTER'
    public static final String strLong = "This is a huge string that repeats. αβγδε � בגדה ابةتث";
//    public static final String strLong = "This is a huge string that repeats. Some padding again. ";
    public static final String strUTF = "αβγδε � בגדה ابةتث" ;
    // Charactser that may catch databases out (particularly the quotes) 
    public static final String strSpecial = "'\"%?*" ;
}
