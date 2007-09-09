# JRuby ignores RUBYLIB ?? and -I ??
$:<<'/home/afs/opt/cygwin/lib/ruby/1.8'

require 'test/unit'
require 'test/unit/ui/console/testrunner'

class TC_ARQ < Test::Unit::TestCase

##   def setup
##   end
##   def teardown
##   end

  def test_foo
    end
end


class TS_ARQ
  def self.suite
    suite = Test::Unit::TestSuite.new
    suite << TC_ARQ.suite
    return suite
  end
end

Test::Unit::UI::Console::TestRunner.run(TS_ARQ)
