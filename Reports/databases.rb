# JRUBY

require 'java'
#include_class 'java.lang.Class'

include_class 'java.sql.DriverManager' 
include_class 'com.mysql.jdbc.jdbc2.optional.MysqlDataSource'

require 'jdbc'

puts "Going .."


## ds = MysqlDataSource.new()
## ds.setUser(ENV['SDB_USER'])
## ds.setPassword(ENV['SDB_PASSWORD'])
## ds.setURL("jdbc:mysql://localhost/information_schema")
## ds.getConnection()
## 
## c = JDBC::DB.new(ds.getConnection())

c = JDBC::DB.connect("jdbc:mysql://localhost/information_schema",
                     "com.mysql.jdbc.Driver",
                     ENV['SDB_USER'],
                     ENV['SDB_PASSWORD'])

q = <<EOQ
   SELECT DISTINCT table_schema, engine
   FROM TABLES
   WHERE table_schema != 'information_schema' 
   AND table_schema != 'mysql'
EOQ

rs = c.query(q)

rs.each do
  |row|
  case row['table_schema'].downcase
  when 'information_schema':
  when 'mysql':
  else
    printf("%-10s => %-10s\n",
           row['table_schema'],
           row['ENGINE'])
  end
end
rs.close

c.close

## conn = DriverManager.getConnection("jdbc:mysql://localhost/SDB2", "user", "password")
## s = conn.createStatement()
## rs = s.executeQuery("SELECT lex FROM Nodes")
## rs.next()
## puts rs.getString('lex')
