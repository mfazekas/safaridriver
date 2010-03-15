require "timeout"
require "socket"

require "selenium/webdriver/firefox/util"
require "selenium/webdriver/firefox/binary"
require "selenium/webdriver/firefox/profiles_ini"
require "selenium/webdriver/firefox/profile"
require "selenium/webdriver/firefox/extension_connection"
require "selenium/webdriver/firefox/launcher"
require "selenium/webdriver/firefox/bridge"

module Selenium
  module WebDriver

    # @private
    module Firefox

       DEFAULT_PROFILE_NAME         = "WebDriver".freeze
       DEFAULT_PORT                 = 7055
       DEFAULT_ENABLE_NATIVE_EVENTS = [:windows, :linux].include? Platform.os
       DEFAULT_SECURE_SSL           = false
       DEFAULT_LOAD_NO_FOCUS_LIB    = Platform.os == :linux

    end
  end
end


# SocketError was added in Ruby 1.8.7.
# If it's not defined, we add it here so it can be used in rescues.
unless defined? SocketError
  class SocketError < IOError; end
end