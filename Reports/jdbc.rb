# = Module for handling JDBC Result Sets

require 'java'
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
      columns = cols 
      data = all_rows_array
      widths = calc_widths(columns, data)

      # Make lines like column names
      lines = []
      columns.each_index { |i| lines<<"-"*(widths[i]) ; }
      lines2 = []
      columns.each_index { |i| lines2<<"="*(widths[i]) ; }

      print_row(lines,   widths, "-", "+", "-", "+")
      print_row(columns, widths, " ", "|", "|", "|")
      print_row(lines2,  widths, "=", "|", "|", "|")
      data.each { |row| print_row(row, widths, " ", "|", "|", "|") }
      print_row(lines, widths, "-", "+", "-", "+")
    end

    def next
      raise "Error: calling next on a ResultSet object"
    end

    ## -------- Workers
    private
    def calc_widths(columns, data)
      x = []
      columns.each { |c| x << c.length }
      data.each do |row|
        row.each_index { |i|  x[i] = row[i].length if row[i].length > x[i] }
      end
      return x
    end
    
    def print_row(items, widths, sep, left, mid, right)
      print left
      items.each_index do |i|
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
