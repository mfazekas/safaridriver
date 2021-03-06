#!/usr/bin/python
#
# Copyright 2008-2009 WebDriver committers
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
from selenium.firefox.webdriver import WebDriver
from selenium.common_tests import api_examples
from selenium.common.webserver import SimpleWebServer


def setup_module(module):
    webserver = SimpleWebServer()
    webserver.start()
    FirefoxApiExampleTest.webserver = webserver
    FirefoxApiExampleTest.driver = WebDriver()


class FirefoxApiExampleTest(api_examples.ApiExampleTest):
    pass


def teardown_module(module):
    FirefoxApiExampleTest.driver.quit()
    FirefoxApiExampleTest.webserver.stop()
