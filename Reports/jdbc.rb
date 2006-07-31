require 'java'
include_class 'java.sql.DriverManager' 

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
    def allRowHash
      x = []
      columns = cols 
      each {|row| x << row.data(columns)}
      close
      return x
    end

    # All the rows, as an array of arrays
    def allRowArray
      x = []
      each {|row| x << row.asArray }
      close
      return x
    end
    
    def dump
      columns = cols 
      data = allRowArray
      widths = calcWidths(columns, data)

      # Make lines like column names
      lines = []
      columns.each_index { |i| lines<<"-"*(widths[i]) ; }
      lines2 = []
      columns.each_index { |i| lines2<<"="*(widths[i]) ; }

      printRow(lines, widths, "-", "+", "-", "+")
      printRow(columns, widths, " ", "|", "|", "|")
      printRow(lines2, widths, "=", "|", "|", "|")
      data.each { |row| printRow(row, widths, " ", "|", "|", "|") }
      printRow(lines, widths, "-", "+", "-", "+")
    end

    def next
      raise "Error: calling next on a ResultSet object"
    end

    ## -------- Workers
    private
    def calcWidths(columns, data)
      x = []
      columns.each {|c| x << c.length }
      data.each do
        |row| row.each_index do
          |i|
          x[i] = row[i].length if row[i].length > x[i]
        end
      end
      return x
    end
    
    def printRow(items, widths, sep, left, mid, right)
      print left
      items.each_index do
        |i|
        print mid if i != 0
        print sep
        printf("%*s",widths[i],items[i])
        print sep
      end
      print right
      print "\n" 
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

    def asArray
      len = @row.getMetaData.getColumnCount
      x = []
      for i in 1..len do
        x << @row.getString(i)
      end
      return x
    end

    # Needs column names
    def data(cols)
      x = {}
      cols.each do
        |col| 
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
