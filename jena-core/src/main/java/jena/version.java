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

package jena;

import static jena.cmdline.CmdLineUtils.setLog4jConfiguration;

import com.hp.hpl.jena.Jena;
import java.lang.reflect.*;

/**
 * jena.version
 * Print out jena version information and exit.
 */
public class version implements Jena {

    static { setLog4jConfiguration() ; }

    /**
	 * Print out jena version information and exit.
	 * @param args
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		Field f[] = Jena.class.getDeclaredFields();
        for ( Field aF : f )
        {
            System.out.println( aF.getName() + ": " + aF.get( null ) );
        }
	}

}
