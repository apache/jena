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

package com.hp.hpl.jena.n3;

import java.io.* ;

import com.hp.hpl.jena.JenaRuntime ;
import com.hp.hpl.jena.shared.JenaException ;

/** Simple class that provides output with moving left margin.
 *  Does not cope with tabs or newlines in output strings.
 */

// Not robust/complete enough for public use
public class N3IndentedWriter
{
	String lineSeparator = JenaRuntime.getLineSeparator() ;
	
	Writer writer ;
	int column ;
	int row ;
	int currentIndent ;
	
	public N3IndentedWriter(Writer w)
	{
		writer = w ;
		column = 0 ; 
		row = 0 ;
		currentIndent = 0 ;
	}

    public Writer getWriter() { return writer ; }

	public int getRow() { return row ; }
	public int getCol() { return column ; }
	public int getIndent() { return currentIndent ; }
	
	public void incIndent(int x) { currentIndent += x ; }
	public void decIndent(int x) { currentIndent -= x ; }
	public void setIndent(int x) { currentIndent = x ; }
	
	public void print(String s)
	{
		try { writer.write(s); column += s.length() ; }
		catch (java.io.IOException ex) { throw new JenaException(ex) ; }
	}

	public void println(String s)
	{
		try { writer.write(s);	println() ; }
		catch (java.io.IOException ex) { throw new JenaException(ex) ; }
	}
	
	public void println()
	{
		try {
			writer.write(lineSeparator); 
			writer.flush() ;
			column = 0 ;
			row++ ; 
			padTo() ;
		}
		catch (java.io.IOException ex) { throw new JenaException(ex) ; }
	}
	
	public void padTo() throws IOException
	{
		StringBuilder sBuff = new StringBuilder() ;
		for ( int i = 0 ; i < currentIndent ; i++ )
			writer.write(' ') ;
		column = column + currentIndent ;
	}
	
	public void flush() { try { writer.flush() ; } catch (IOException ioEx) { throw new JenaException(ioEx) ; } }
	public void close() { try { writer.close() ; } catch (IOException ioEx) { throw new JenaException(ioEx) ; } }

}
