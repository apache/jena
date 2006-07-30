require 'java'
include_class 'java.sql.DriverManager' 
include_class 'java.sql.ResultSet' 

module JDBC

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

    def close
      @conn.close()
    end
  end

  # Uses class Row to add a method and remove a method
  # Adding can also be done by "def rs.[]" 
  # Similar for .each above.
  class Results
    def initialize(jdbcResultSet)
      @rs = jdbcResultSet
    end

    def each(&block)
      while(@rs.next)
        block.call(Row.new(@rs))
      end
      close
    end

    def close
      @rs.close
    end
  end

  class Row
    def initialize(row)
      @row = row
    end
    def [](name)
      return getString(name)
    end

    def next
      raise "Error: calling close on a Row object"
    end

    # Direct any missing methods to the wrapped object
    def method_missing(methId, *args)
      meth = @row.method(methId)
      meth.call *args
    end 
  end

end
