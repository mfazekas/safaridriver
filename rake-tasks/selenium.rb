class Selenium < BaseGenerator
  def selenium_test(args)
    create_deps_(args[:name], args)
    
    classpath = JavaGen.new().build_classpath_(args[:name]).collect do |c|
      c.to_s =~ /\.jar/ ? c : nil
    end
    classpath.uniq!

    test_root = "http://localhost:4444/selenium-server/tests/"
    
    file "#{args[:name]}_never_there" do
      debug = ENV['debug'] ? "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 " : "" 

      cmd = "java #{debug} -cp #{classpath.join(classpath_separator?)} org.openqa.selenium.server.htmlrunner.HTMLLauncher "
      cmd += "build #{test_root}TestSuite.html #{test_root} true #{args[:browser]}"
      sh cmd, :verbose => false
    end
    
    task args[:name] => "#{args[:name]}_never_there"
  end
end

def selenium_test(args)
  Selenium.new().selenium_test(args)
  end