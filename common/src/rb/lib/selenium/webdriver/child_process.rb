module Selenium
  module WebDriver

    #
    # Cross platform child process launcher
    #

    class ChildProcess
      attr_reader :pid

      def initialize(*args)
        @args = args

        if Platform.jruby?
          extend JRubyProcess
        elsif Platform.ironruby?
          extend IronRubyProcess
        elsif Platform.os == :windows
          extend WindowsProcess
        end
      end

      def ugly_death?
        code = exit_value()
        # if exit_val is nil, the process is still alive
        code && code != 0
      end

      def exit_value
        pid, status = Process.waitpid2(@pid, Process::WNOHANG)
        status.exitstatus if pid
      end

      def start
        @pid = fork do
          unless $DEBUG
            [STDOUT, STDERR].each { |io| io.reopen("/dev/null") }
          end

          exec(*@args)
        end

        self
      end

      def wait
        assert_started
        Process.waitpid2 @pid
      rescue Errno::ECHILD
        nil
      end

      def kill
        assert_started
        Process.kill('TERM', @pid)
      end

      def kill!
        assert_started
        Process.kill('KILL', @pid)
      end

      def assert_started
        raise Error::WebDriverError, "process not started" unless @pid
      end

      module WindowsProcess
        def start
          require "win32/process" # adds a dependency on windows - perhaps we could just use FFI instead?
          @pid = Process.create(
            :app_name        => @args.join(" "),
            :inherit         => false # don't inherit open file handles
          ).process_id

          self
        end

        def kill
          kill!
        end
      end

      module JRubyProcess
        def start
          pb = java.lang.ProcessBuilder.new(@args)

          # this isn't good
          env = pb.environment
          ENV.each { |k,v| env.put(k, v) }

          @process = pb.start

          self
        end

        def kill
          assert_started
          @process.destroy
        end
        alias_method :kill!, :kill

        def wait
          assert_started
          @process.waitFor
          [nil, @process.exitValue] # no robust way to get pid here
        end

        def exit_value
          assert_started
          @process.exitValue
        rescue java.lang.IllegalThreadStateException
          nil
        end

        def assert_started
          raise Error::WebDriverError, "process not started" unless @process
        end
      end

      module IronRubyProcess
        def start
          args = @args.dup

          @process                           = System::Diagnostics::Process.new
          @process.StartInfo.UseShellExecute = true
          @process.StartInfo.FileName        = args.shift
          @process.StartInfo.Arguments       = args.join ' '
          @process.start

          self
        end

        def kill
          assert_started
          @process.Kill
        end

        def wait
          assert_started
          @process.WaitForExit
          [pid, exit_value]
        end

        def pid
          assert_started
          @process.Id
        end

        def exit_value
          assert_started
          return unless @process.HasExited

          @process.ExitCode
        end

        def assert_started
          raise Error::WebDriverError, "process not started" unless @process
        end
      end

    end # ChildProcess
  end # WebDriver
end # Selenium