require "net/http"

module Selenium
  module WebDriver
    module Remote

      # @private
      class DefaultHttpClient
        CONTENT_TYPE    = "application/json"
        DEFAULT_HEADERS = { "Accept" => CONTENT_TYPE, "Content-Length" => "0" }

        def initialize(url)
          @server_url = url
        end

        def call(verb, url, command_hash)
          response = nil
          url      = @server_url.merge(url) unless url.kind_of?(URI)
          headers  = DEFAULT_HEADERS.dup

          if command_hash
            payload = command_hash.to_json
            headers["Content-Type"] = "#{CONTENT_TYPE}; charset=utf-8"
            headers["Content-Length"] = payload.bytesize.to_s if [:post, :put].include?(verb)

            if $DEBUG
              puts "   >>> #{payload}"
              puts "     > #{headers.inspect}"
            end
          end

          request verb, url, headers, payload
        end

        private

        def http
          # ignoring SSL for now
          @http ||= Net::HTTP.new @server_url.host, @server_url.port
        end

        def request(verb, url, headers, payload)
          request  = Net::HTTP.const_get(verb.to_s.capitalize).new(url.path, headers)
          response = http.request(request, payload)

          # TODO: should be checking against a maximum redirect count
          if response.kind_of? Net::HTTPRedirection
            request(:get, URI.parse(response['Location']), DEFAULT_HEADERS.dup, nil)
          else
            create_response response
          end
        end

        def create_response(res)
          puts "<- #{res.body}\n" if $DEBUG
          if res.content_type == CONTENT_TYPE
            Response.new do |r|
              r.code         = res.code.to_i
              r.payload      = JSON.parse(res.body.strip)
            end
          elsif res.code == '204'
            Response.new { |r| r.code = res.code.to_i }
          else
            raise "Unexpected content type: #{res.content_type.inspect} (#{res.code})\n#{res.body}"
          end
        end

      end # DefaultHttpClient
    end # Remote
  end # WebDriver
end # Selenium
