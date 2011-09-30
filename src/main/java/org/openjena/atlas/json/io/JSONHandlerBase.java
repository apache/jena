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

package org.openjena.atlas.json.io;


public class JSONHandlerBase implements JSONHandler
{
    //@Override
    public void startParse()
    {}

    //@Override
    public void finishParse()
    {}

    //@Override
    public void startObject()
    {}

    //@Override
    public void finishObject()
    {}

    //@Override
    public void startPair()
    {}

    //@Override
    public void keyPair()
    {}

    //@Override
    public void finishPair()
    {}

    //@Override
    public void startArray()
    {}

    //@Override
    public void element()
    {}

    //@Override
    public void finishArray()
    {}

    //@Override
    public void valueString(String image)
    {}

    //@Override
    public void valueInteger(String image)
    {}

    //@Override
    public void valueDecimal(String image)
    {}

    //@Override
    public void valueDouble(String image)
    {}

    //@Override
    public void valueBoolean(boolean b)
    {}

    //@Override
    public void valueNull()
    {}
}
