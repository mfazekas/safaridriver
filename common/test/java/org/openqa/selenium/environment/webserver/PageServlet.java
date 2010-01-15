/*
Copyright 2007-2009 WebDriver committers
Copyright 2007-2009 Google Inc.
Portions copyright 2007 ThoughtWorks, Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.openqa.selenium.environment.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PageServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // Do nothing
    }

    response.setContentType("text/html");

    int lastIndex = request.getPathInfo().lastIndexOf('/');
    String
        pageNumber =
        (lastIndex == -1 ? "Unknown" : request.getPathInfo().substring(lastIndex + 1));
    String res = String.format("<html><head><title>Page%s</title></head>" +
                               "<body>Page number <span id=\"pageNumber\">%s</span>" +
                               "<p><a href=\"../xhtmlTest.html\" target=\"_top\">top</a>" +
//        		"<script>var s=''; for (var i in window) {s += i + ' -> ' + window[i] + '<p>';} document.write(s);</script>" +
"</body></html>",
                               pageNumber, pageNumber);

    response.getOutputStream().println(res);
  }
}
