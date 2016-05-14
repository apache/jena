# = Module for handling JDBC Result Sets
# Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0

require 'java'
require 'format'
include_class 'java.sql.DriverManager' 

module JDBC

# == Class DB: a connection to a database / tablespace
  class DB
    attr_reader   :conn

    def initialize(connection)
      @conn = connection ;
    end

    def DB.connect(url, driver, user, password)
      Java::JavaClass.for_name(driver) ;
      c = new(DriverManager.getConnection(url, user, password))
      return c
    end

    def query(queryString)
      s = @conn.createStatement()
      return Results.new(s.executeQuery(queryString))
    end

    def query_print(queryString)
      s = @conn.createStatement()
      rs = Results.new(s.executeQuery(queryString))
      rs.dump
      rs.close
      return nil
    end

    def close
      @conn.close()
    end
  end

  class Results
  
    def initialize(jdbcResultSet)
      @rs = jdbcResultSet
    end

    def each
      while(@rs.next) 
        yield Row.new(@rs)
      end
      close
    end

    def close
      @rs.close
    end

    # All the cols (via their display name)
    def cols
      if !@columns
        md = @rs.getMetaData
        @columns=[]
        1.upto(md.getColumnCount) { |i| @columns << md.getColumnLabel(i) }
        end
      return @columns
    end
      
    # All the rows, as an array of hashes (values are strings)
    def all_rows_hash
      x = []
      columns = cols 
      each {|row| x << row.data(columns)}
      close
      return x
    end

    # All the rows, as an array of arrays
    def all_rows_array
      x = []
      each {|row| x << row.as_array }
      close
      return x
    end
    
    def dump
      # Order matters - must get columns before exhausting data and closing ResultSet
      columns = cols 
      data = all_rows_array
      Fmt.table(columns, data)
    end
  end

  class Row
    def initialize(row)
      @row = row
    end
    
    # and it works for string name or integer index
    def [](name)
      return @row.getString(name)
    end

    def next
      raise "Error: calling close on a Row object"
    end

    def each
      len = @row.getMetaData.getColumnCount
      (1..len).each { |i| yield  @row.getString(i) }
    end

    def as_array
      len = @row.getMetaData.getColumnCount
      x = []
      (1..len).each { |i| x << @row.getString(i) }
      return x
    end

    # Needs column names
    def data(cols)
      x = {}
      cols.each do |col| 
        x[col] = @row.getString(col)
        if @row.wasNull
          x[col] = nil
        end
      end
      return x 
    end

    # Direct any missing methods to the wrapped object
    def method_missing(methId, *args)
      meth = @row.method(methId)
      meth.call *args
    end 

  end
end
