/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
*/

package com.hp.hpl.jena.db;

import java.sql.*;

import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
* Encapsulate the specification of a jdbc connection, mostly used to
* simplify the calling pattern for ModelRDB factory methods.
*
* @author csayers (based on earlier code by der)
* @version $Revision: 1.5 $ on $Date: 2005-02-21 12:02:39 $
*/

public interface IDBConnection {

	/**
	 * Return the jdbc connection or null if we no longer have access to a connection.
	 */
    public Connection getConnection() throws SQLException;

    /**
     * Close the jdbc connection
     */
    public void close() throws SQLException;

    /**
     * Clear all RDF information from the database.
     * 
     * This wipes all the information stored by Jena from the database.
     * Obviously should be used with care.
     */
    public void cleanDB() throws SQLException;

    /**
     * Return true if the database seems to be formated for RDF storage.
     * This is <em>not</em> an integrity check this is simply a flag
     * recording that a base level table exists.
     * Any access errors are treated as the database not being formated.
     */
    public boolean isFormatOK() throws RDFRDBException;

	/** 
	 * Sets database-specific properties.
	 * 
	 * <p>
	 * These properties may only be set before the first Model has been
	 * stored in the database.  After that point, the database structure
	 * is frozen.</p>
	 * 
	 * <p>
	 * Use these properties to optionally customize the database - this
	 * won't change the results you see when using the graph interface,
	 * but it may alter the speed with which you get them or the space
	 * required by the database.</p>
	 *
	 * <p>
	 * The properties must form a complete and consistent set.
	 * The easist way to get a complete and consistent set is to call
	 * getDatabaseProperties, modify the returned model, and then use 
	 * that as an argument in the call to setDatabaseProperties.</p>
	 * 
	 * <p>
	 * Note that some implementations may choose to delay actually peforming
	 * the formatting operation until at least one Graph is constructed in
	 * the database.  Consequently, a successful return from this call
	 * does not necessarily guarantee that the database properties
	 * were set correctly.</p>
	 * 
	 * @param propertyModel is a Model describing the database parameters
	 * @since Jena 2.0
	 * 
	 */
	public void setDatabaseProperties(Model propertyModel) throws RDFRDBException;
	
	/** 
	 * Returns a Jena Model containing database properties.
	 * <p>
	 * These describe the optimization/layout for the database.</p>
	 * 
	 * <p>
	 * If the database has not been formatted, then a default
	 * set of properties is returned.  Otherwise the actual properties
	 * are returned.</p>
	 * 
	 * <p>
	 * The returned Model is a copy, modifying it will have no
	 * effect on the database.  (Use setDatabaseProperties to
	 * make changes).</p>
	 * 
	 * @since Jena 2.0
	 */
	public Model getDatabaseProperties() throws RDFRDBException; 
	
	/** Set the database type manually.
	 * <p>
	 * This is not for public use (it is preferable to
	 * specify it in the constructor) - included here to handle
	 * older code, which didn't use the new constructor.</p>
	 * 
	 * @since Jena 2.0
	 */

	public void setDatabaseType( String databaseType );
	
	/**
	 * Retrieve a default set of model customization properties.
	 * 
	 * The returned default set of properties is suitable for use in a call to
	 * ModelRDB.create(..., modelProperties);
	 * 
     * @return Model containing default properties
     */
	
	public Model getDefaultModelProperties() throws RDFRDBException;

	/** Get the database type.
	 * @return String database type, or null if unset
	 * 
	 * @since Jena 2.0
	 */
	public String getDatabaseType();
	
    /*
     * Returns a property value for the database at the end of the
     * connection. This is used to retrieve appropriate formating and
     * driver information from a standard RDF_LAYOUT_INFO table.
     * For a database that has not been formatted all calls will
     * return null.
     * Any access errors are treated as the database not being formated.
     * Throws an exception if the database is not even openable.
     * 
     * This is no longer supported in Jena2 - use the property model instead.
     * (The database implementation in Jena 2 is quite different and there
     * are no analogous properties for most of the Jena 1 properties.)
     */
    //public String getProperty(String propname) throws SQLException;

    /*
     * Returns a set of property values for the database at the end of the
     * connection. This is used to retrieve appropriate formating and
     * driver information from a standard RDF_LAYOUT_INFO table.
     * For a database that has not been formatted all calls will
     * return null.
     * Any access errors are treated as the database not being formated.
	 *
     * This is no longer supported in Jena2 - use the property model instead.
     * (The database implementation in Jena 2 is quite different and there
     * are no analogous properties for most of the Jena 1 properties.)
     */
    //public Properties getProperties() throws SQLException;

    /*
     * Add a new property value to both RDF_LAYOUT_INFO table.
	 *
     * This is no longer supported in Jena2 - use the property model instead.
     * (The database implementation in Jena 2 is quite different and there
     * are no analogous properties for most of the Jena 1 properties.)
     */
    //public void addProperty(String propname, String value) throws SQLException;

	/** Retrieve a list of all models in the database
	 *
	 * @return Iterator over String names for graphs.
	 * @throws RDFDBException
	 * @since Jena 2.0
	 */
	public ExtendedIterator getAllModelNames() throws RDFRDBException;

	/**
	 * Test if a given model is contained in the database.
	 * 
	 * @param name the name of a model which may be in the database
	 * @return Boolean true if the model is contained in the database
	 * @throws RDFDBException
	 * @since Jena 2.0
	 */     
	 public boolean containsModel(String name) throws RDFRDBException;
	 
	/**
	 * Test if a default model is contained in the database.
	 * 
	 * A default model is a model for which no specific name was specified.
	 * (One that was created by calling ModelRDB.createModel without specifying
	 * a name).
	 * 
	 * @return Boolean true if the model is contained in the database
	 * @throws RDFDBException
	 * @since Jena 2.0
	 */     
	 public boolean containsDefaultModel() throws RDFRDBException;

	/** Get the database-specific driver 
	 *
	 * For this to work, it needs to know the type of database being used.
	 * That may be specified in the constructor (preferred) or done later
	 * by using the setDatabaseType method (for backward compatability).
	 */
	
	public IRDBDriver getDriver() throws RDFRDBException;
	
    /**
     * Set the IRDBDriver to use for this connection.
     * Useful to enable external drivers to be registered outside of the
     * standard driver package.
     */
    public void setDriver(IRDBDriver driver);

    /**
     * Helper function to locate and instantiate the driver class corresponding
     * to a given layout and database name
     * Throws an RDFRDBexception if the driver can't be instantiated
     * @deprecated As of Jena 2.0 this call should not be used.  Instead specify the database type
     * when constructing a DBConnection and then pass that connection to the GraphRDB.  There is
     * no longer any need for applications to interact with the IRDBDriver.  To customize the
     * database configuration/layout use the formatDB(propertyModel) call.
     */
    public IRDBDriver getDriver(String layout, String database) throws RDFRDBException;

}

/*
 *  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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