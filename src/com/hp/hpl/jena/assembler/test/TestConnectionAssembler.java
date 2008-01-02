/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestConnectionAssembler.java,v 1.8 2008-01-02 12:05:57 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.test;

import java.util.StringTokenizer;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.ConnectionAssembler;
import com.hp.hpl.jena.assembler.exceptions.CannotLoadClassException;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;

public class TestConnectionAssembler extends AssemblerTestBase
    {
    public TestConnectionAssembler( String name )
        { super( name ); }  

    protected Class getAssemblerClass()
        { return ConnectionAssembler.class; }

    public void testConnectionAssemblerType()
        { testDemandsMinimalType( new ConnectionAssembler(), JA.Connection );  }
    
    public void testConnectionVocabulary()
        {
        assertRange( JA.Connection, JA.connection );
        assertDomain( JA.Connection, JA.dbClass );
        assertDomain( JA.Connection, JA.dbUser );
        assertDomain( JA.Connection, JA.dbPassword );
        assertDomain( JA.Connection, JA.dbType );
        assertDomain( JA.Connection, JA.dbURL );
        assertDomain( JA.Connection, JA.dbClassProperty );
        assertDomain( JA.Connection, JA.dbUserProperty );
        assertDomain( JA.Connection, JA.dbPasswordProperty );
        assertDomain( JA.Connection, JA.dbTypeProperty );
        assertDomain( JA.Connection, JA.dbURLProperty );
        }
    
    public void testConnectionDescriptionFailsOnMissingURL()
        {
        ConnectionDescription c = new ConnectionDescription( "eh:/subject", null, null, null, "myType" );
        try { c.getConnection(); fail( "should trap null URL" ); }
        catch (JenaException e) { assertTrue( e.getMessage().endsWith( "cannot be opened because no dbURL or dbType was specified" ) ); }
        }
    
    public void testConnectionDescriptionFailsOnMissingType()
        {
        ConnectionDescription c = new ConnectionDescription( "eh:/subject", "URL", null, null, null );
        try { c.getConnection(); fail( "should trap null type" ); }
        catch (JenaException e) { assertTrue( e.getMessage().endsWith( "cannot be opened because no dbURL or dbType was specified" ) ); }
        }
    
    public void testConnectionInitDefaults()
        {
        Resource init = resourceInModel( "x ja:dbUser 'USER'; x ja:dbPassword 'PASS'; x ja:dbURL URL:url; x ja:dbType 'TYPE'" );
        ConnectionAssembler c = new ConnectionAssembler( init );
        assertEquals( "USER", c.defaultUser );
        assertEquals( "PASS", c.defaultPassword );
        assertEquals( "URL:url", c.defaultURL );
        assertEquals( "TYPE", c.defaultType );
        }

    public void testCannotLoadClass()
        {
        Assembler a = new ConnectionAssembler();
        Resource root = resourceInModel( "x rdf:type ja:Connection; x ja:dbClass 'no.such.class'" );
        try 
            { a.open( root ); fail( "should catch class load failure" ); }
        catch (CannotLoadClassException e)
            {
            assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( "no.such.class", e.getClassName() );
            assertInstanceOf( ClassNotFoundException.class, e.getCause() );
            }
        }
    
    public void testDefaultUser()
        {
        Resource init = resourceInModel( "x ja:dbUser 'test'" );
        Resource root = resourceInModel( "x rdf:type JA.Connection" );
        assertEquals( "test", new ConnectionAssembler( init ).getUser( root ) );
        }

    public void testDefaultPassword()
        {
        Resource init = resourceInModel( "x ja:dbPassword 'byzantium'" );
        Resource root = resourceInModel( "x rdf:type JA.Connection" );
        assertEquals( "byzantium", new ConnectionAssembler( init ).getPassword( root ) );
        }

    public void testDefaultURL()
        {
        Resource init = resourceInModel( "x ja:dbURL URL:database" );
        Resource root = resourceInModel( "x rdf:type ja:Connection" );
        assertEquals( "URL:database", new ConnectionAssembler( init ).getURL( root ) );
        }

    public void testDefaultType()
        {
        Resource init = resourceInModel( "x ja:dbType 'bodacious'" );
        Resource root = resourceInModel( "x rdf:type ja:Connection" );
        assertEquals( "bodacious", new ConnectionAssembler( init ).getType( root ) );
        }
    
    public void testFullySpecifiedConnection()
        {
        Resource root = resourceInModel( "x rdf:type ja:Connection; x ja:dbUser 'test'; x ja:dbPassword ''; x ja:dbURL jdbc:mysql://localhost/test; x ja:dbType 'MySQL'" );
        assertEquals( "test", new ConnectionAssembler().getUser( root ) );
        assertEquals( "", new ConnectionAssembler().getPassword( root ) );
        assertEquals( "jdbc:mysql://localhost/test", new ConnectionAssembler().getURL( root ) );
        assertEquals( "MySQL", new ConnectionAssembler().getType( root ) );
        }
    
    public void testTrapsNonStringObjects()
        {
        testTrapsNonStringObjects( "ja:dbClass", "aResource" );
        testTrapsNonStringObjects( "ja:dbClass", "17" );
        testTrapsNonStringObjects( "ja:dbClass", "'tag'de" );
        testTrapsNonStringObjects( "ja:dbClassProperty", "aResource" );
        testTrapsNonStringObjects( "ja:dbClassProperty", "17" );
        testTrapsNonStringObjects( "ja:dbClassProperty", "'tag'de" );
        }
    
    private void testTrapsNonStringObjects( String property, String value )
        {
        Resource root = resourceInModel( "x rdf:type ja:Connection; x <property> <value>".replaceAll( "<property>", property ).replaceAll( "<value>", value ) );
        try 
            { new ConnectionAssembler().open( root );
            fail( "should trap bad object " + value + " for property " + property ); }
        catch (BadObjectException e)
            { assertEquals( resource( "x" ), e.getRoot() );
            assertEquals( rdfNode( empty, value ), e.getObject() ); }
        }
    
    public void testOpenConnectionWIthLabels()
        {
        Resource root = resourceInModel( "x rdf:type ja:Connection; x ja:dbUser 'X'; x ja:dbPassword 'P'; x ja:dbURL U:RL; x ja:dbType 'T'" );
        final ConnectionDescription fake = ConnectionDescription.create( "eh:/x", "DD", "TT", "UU", "PP" );
        CheckingConnectionAssembler x = new CheckingConnectionAssembler( fake, "eh:/x U:RL X P T" );
        assertSame( fake, x.open( root ) );
        assertTrue( "mock createConnection should have been called", x.called );
        }
    
    public void testConnection()
        {
        Assembler a = new ConnectionAssembler();
        Resource root = resourceInModel( "x rdf:type ja:Connection; x ja:dbUser 'test'; x ja:dbPassword ''; x ja:dbURL jdbc:mysql://localhost/test; x ja:dbType 'MySQL'" );
        Object x = a.open( root );
        assertInstanceOf( ConnectionDescription.class, x );
        ConnectionDescription d = (ConnectionDescription) x;
        assertEquals( "test", d.dbUser );
        assertEquals( "", d.dbPassword );
        assertEquals( "MySQL", d.dbType );
        assertEquals( "jdbc:mysql://localhost/test", d.dbURL );
        }
    
    public void testIndirectURLConnection()
        {
        System.setProperty( "test.url", "bbb" );
        Resource root = resourceInModel( "x rdf:type ja:Connection; x ja:dbURLProperty 'test.url'" );
        Assembler a = new ConnectionAssembler();
        ConnectionDescription d = (ConnectionDescription) a.open( root );
        assertEquals( "bbb", d.dbURL );
        }
    
    public void testIndirectUserConnection()
        {
        System.setProperty( "test.user", "blenkinsop" );
        Resource root = resourceInModel( "x rdf:type ja:Connection; x ja:dbUserProperty 'test.user'" );
        Assembler a = new ConnectionAssembler();
        ConnectionDescription d = (ConnectionDescription) a.open( root );
        assertEquals( "blenkinsop", d.dbUser );
        }
    
    public void testIndirectPasswordConnection()
        {
        System.setProperty( "test.password", "Top/Secret" );
        Resource root = resourceInModel( "x rdf:type ja:Connection; x ja:dbPasswordProperty 'test.password'" );
        Assembler a = new ConnectionAssembler();
        ConnectionDescription d = (ConnectionDescription) a.open( root );
        assertEquals( "Top/Secret", d.dbPassword );
        }
    
    public void testIndirectTypeConnection()
        {
        System.setProperty( "test.type", "HisSQL" );
        Resource root = resourceInModel( "x rdf:type ja:Connection; x ja:dbTypeProperty 'test.type'" );
        Assembler a = new ConnectionAssembler();
        ConnectionDescription d = (ConnectionDescription) a.open( root );
        assertEquals( "HisSQL", d.dbType );
        }
    
    private static final class CheckingConnectionAssembler extends ConnectionAssembler
        {
        private final ConnectionDescription result;
        private final String expectSubject;
        private final String expectURL;
        private final String expectUser;
        private final String expectPassword;
        private final String expectType;
        
        private boolean called;

        private CheckingConnectionAssembler( ConnectionDescription result, String expected )
            {
            super();
            StringTokenizer st = new StringTokenizer( expected );
            expectSubject = st.nextToken();
            expectURL = st.nextToken();
            expectUser = st.nextToken();
            expectPassword = st.nextToken();
            expectType = st.nextToken();
            this.result = result;
            }

        public ConnectionDescription createConnection
            ( String subject, String url, String type, String user, String pass )
            {
            assertEquals( expectSubject, subject );
            assertEquals( expectURL, url );
            assertEquals( expectUser, user );
            assertEquals( expectPassword, pass );
            assertEquals( expectType, type );
            called = true;
            return result;
            }
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/