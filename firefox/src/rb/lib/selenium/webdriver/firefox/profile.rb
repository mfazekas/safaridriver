module Selenium
  module WebDriver
    module Firefox
      class Profile

        ANONYMOUS_PROFILE_NAME = "WEBDRIVER_ANONYMOUS_PROFILE"
        EXTENSION_NAME         = "fxdriver@googlecode.com"
        EM_NAMESPACE_URI       = "http://www.mozilla.org/2004/em-rdf#"
        NO_FOCUS_LIBRARY_NAME  = "x_ignore_nofocus.so"

        DEFAULT_EXTENSION_SOURCE = File.expand_path("#{WebDriver.root}/firefox/src/extension")

        XPTS = [
          ["#{WebDriver.root}/firefox/prebuilt/nsINativeEvents.xpt", "components/nsINativeEvents.xpt"],
          ["#{WebDriver.root}/firefox/prebuilt/nsICommandProcessor.xpt", "components/nsICommandProcessor.xpt"],
          ["#{WebDriver.root}/firefox/prebuilt/nsIResponseHandler.xpt", "components/nsIResponseHandler.xpt"],
        ]

        NATIVE_WINDOWS = ["#{WebDriver.root}/firefox/prebuilt/Win32/Release/webdriver-firefox.dll", "platform/WINNT_x86-msvc/components/webdriver-firefox.dll"]
        NATIVE_LINUX   = [
          ["#{WebDriver.root}/firefox/prebuilt/linux/Release/libwebdriver-firefox.so", "platform/Linux_x86-gcc3/components/libwebdriver-firefox.so"],
          ["#{WebDriver.root}/firefox/prebuilt/linux64/Release/libwebdriver-firefox.so", "platform/Linux_x86_64-gcc3/components/libwebdriver-firefox.so"]
        ]

        NO_FOCUS = [
          ["#{WebDriver.root}/firefox/prebuilt/linux64/Release/x_ignore_nofocus.so", "amd64/x_ignore_nofocus.so"],
          ["#{WebDriver.root}/firefox/prebuilt/linux/Release/x_ignore_nofocus.so", "x86/x_ignore_nofocus.so"],
        ]

        SHARED = [
          ["#{WebDriver.root}/common/src/js/extension/dommessenger.js", "content/dommessenger.js"]
        ]

        attr_reader :name, :directory
        attr_writer :secure_ssl, :native_events, :load_no_focus_lib
        attr_accessor :port

        class << self
          def ini
            @ini ||= ProfilesIni.new
          end

          def from_name(name)
            ini[name]
          end
        end

        #
        # Create a new Profile instance
        #
        # @example User configured profile
        #
        #   profile = Selenium::WebDriver::Firefox::Profile.new
        #   profile['network.proxy.http'] = 'localhost'
        #   profile['network.proxy.http_port'] = 9090
        #
        #   driver = Selenium::WebDriver.for :firefox, :profile => profile
        #

        def initialize(directory = nil)
          @directory = directory ? create_tmp_copy(directory) : Dir.mktmpdir("webdriver-profile")

          unless File.directory? @directory
            raise Error::WebDriverError, "Profile directory does not exist: #{@directory.inspect}"
          end

          # TODO: replace constants with options hash
          @port              = DEFAULT_PORT
          @extension_source  = DEFAULT_EXTENSION_SOURCE
          @native_events     = DEFAULT_ENABLE_NATIVE_EVENTS
          @secure_ssl        = DEFAULT_SECURE_SSL
          @load_no_focus_lib = DEFAULT_LOAD_NO_FOCUS_LIB

          @additional_prefs  = {}
        end

        #
        # Set a preference for this particular profile.
        # @see http://preferential.mozdev.org/preferences.html
        #

        def []=(key, value)
          case value
          when String
            if Util.stringified?(value)
              raise ArgumentError, "preference values must be plain strings: #{key.inspect} => #{value.inspect}"
            end

            value = %{"#{value}"}
          when TrueClass, FalseClass, Integer, Float
            value = value.to_s
          else
            raise TypeError, "invalid preference: #{value.inspect}:#{value.class}"
          end

          @additional_prefs[key.to_s] = value
        end

        def absolute_path
          if Platform.win?
            directory.gsub("/", "\\")
          else
            directory
          end
        end

        def update_user_prefs
          prefs = current_user_prefs

          prefs.merge! OVERRIDABLE_PREFERENCES
          prefs.merge! @additional_prefs
          prefs.merge! DEFAULT_PREFERENCES

          prefs['webdriver_firefox_port']           = @port
          prefs['webdriver_accept_untrusted_certs'] = 'true' unless secure_ssl?
          prefs['webdriver_enable_native_events']   = 'true' if native_events?
          prefs["startup.homepage_welcome_url"]     = prefs["browser.startup.homepage"] # If the user sets the home page, we should also start up there

          write_prefs prefs
        end

        def add_webdriver_extension(force_creation = false)
          ext_path = File.join(extensions_dir, EXTENSION_NAME)

          if File.exists?(ext_path)
            return unless force_creation
          end

          FileUtils.rm_rf ext_path
          FileUtils.mkdir_p File.dirname(ext_path), :mode => 0700
          FileUtils.cp_r @extension_source, ext_path

          from_to = XPTS + SHARED

          if native_events?
            case Platform.os
            when :linux
              NATIVE_LINUX.each do |lib|
                from_to << lib
              end
            when :windows
              from_to << NATIVE_WINDOWS
            else
              raise Error::WebDriverError, "can't enable native events on #{Platform.os.inspect}"
            end
          end

          if load_no_focus_lib?
            from_to += NO_FOCUS
            modify_link_library_path(NO_FOCUS.map { |source, dest| File.join(ext_path, File.dirname(dest)) })
          end

          from_to.each do |source, destination|
            dest = File.join(ext_path, destination)
            FileUtils.mkdir_p File.dirname(dest)
            FileUtils.cp source, dest
          end

          delete_extensions_cache
        end

        # TODO: add_extension

        def extensions_dir
          @extensions_dir ||= File.join(directory, "extensions")
        end

        def user_prefs_path
          @user_prefs_path ||= File.join(directory, "user.js")
        end

        def delete_extensions_cache
          cache = File.join(directory, "extensions.cache")
          FileUtils.rm_f cache if File.exist?(cache)
        end

        def modify_link_library_path(paths)
          old_path = ENV['LD_LIBRARY_PATH']

          unless [nil, ''].include?(old_path)
            paths << old_path
          end

          ENV['LD_LIBRARY_PATH'] = paths.join(File::PATH_SEPARATOR)
          ENV['LD_PRELOAD']      = NO_FOCUS_LIBRARY_NAME
        end

        def native_events?
          @native_events == true
        end

        def load_no_focus_lib?
          @load_no_focus_lib == true
        end

        def secure_ssl?
          @secure_ssl == true
        end

        private

        def create_tmp_copy(directory)
          tmp_directory = Dir.mktmpdir("webdriver-rb-profilecopy")

          # TODO: must be a better way..
          FileUtils.rm_rf tmp_directory
          FileUtils.mkdir_p File.dirname(tmp_directory), :mode => 0700
          FileUtils.cp_r directory, tmp_directory

          tmp_directory
        end


        def current_user_prefs
          return {} unless File.exist?(user_prefs_path)

          prefs = {}

          File.read(user_prefs_path).split("\n").each do |line|
            if line =~ /user_pref\("([^"]+)"\s*,\s*(.+?)\);/
              prefs[$1.strip] = $2.strip
            end
          end

          prefs
        end

        def write_prefs(prefs)
          File.open(user_prefs_path, "w") do |file|
            prefs.each do |key, value|
              p key => value if $DEBUG
              file.puts %{user_pref("#{key}", #{value});}
            end
          end
        end

        OVERRIDABLE_PREFERENCES = {
          "browser.startup.page"     => '0',
          "browser.startup.homepage" => '"about:blank"'
        }.freeze

        DEFAULT_PREFERENCES = {
          "app.update.auto"                           => 'false',
          "app.update.enabled"                        => 'false',
          "browser.download.manager.showWhenStarting" => 'false',
          "browser.EULA.override"                     => 'true',
          "browser.EULA.3.accepted"                   => 'true',
          "browser.link.open_external"                => '2',
          "browser.link.open_newwindow"               => '2',
          "browser.safebrowsing.enabled"              => 'false',
          "browser.search.update"                     => 'false',
          "browser.sessionstore.resume_from_crash"    => 'false',
          "browser.shell.checkDefaultBrowser"         => 'false',
          "browser.tabs.warnOnClose"                  => 'false',
          "browser.tabs.warnOnOpen"                   => 'false',
          "dom.disable_open_during_load"              => 'false',
          "extensions.update.enabled"                 => 'false',
          "extensions.update.notifyUser"              => 'false',
          "security.warn_entering_secure"             => 'false',
          "security.warn_submit_insecure"             => 'false',
          "security.warn_entering_secure.show_once"   => 'false',
          "security.warn_entering_weak"               => 'false',
          "security.warn_entering_weak.show_once"     => 'false',
          "security.warn_leaving_secure"              => 'false',
          "security.warn_leaving_secure.show_once"    => 'false',
          "security.warn_submit_insecure"             => 'false',
          "security.warn_viewing_mixed"               => 'false',
          "security.warn_viewing_mixed.show_once"     => 'false',
          "signon.rememberSignons"                    => 'false',
          "javascript.options.showInConsole"          => 'true',
          "browser.dom.window.dump.enabled"           => 'true'
        }.freeze

      end # Profile
    end # Firefox
  end # WebDriver
end # Selenium