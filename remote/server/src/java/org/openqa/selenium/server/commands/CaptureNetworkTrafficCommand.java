package org.openqa.selenium.server.commands;

import org.openqa.jetty.http.HttpRequest;
import org.openqa.jetty.http.HttpResponse;

import java.util.*;
import java.text.SimpleDateFormat;

public class CaptureNetworkTrafficCommand extends Command {
    private static List<Entry> entries = new ArrayList<Entry>();

    public static void clear() {
        entries.clear();
    }

    public static void capture(Entry entry) {
        entries.add(entry);
    }

    private String type; // ie: XML, JSON, plain text, etc

    public CaptureNetworkTrafficCommand(String type) {
        this.type = type;
    }

    public String execute() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        if ("json".equalsIgnoreCase(type)) {
            /*

            [{
            statusCode: 200,
            method: 'GET',
            url: 'http://foo.com/index.html',
            bytes: 12422,
            start: '2009-03-15T14:23:00.000-0700',
            end: '2009-03-15T14:23:00.102-0700',
            timeInMillis: 102,
            requestHeaders: [{
              name: 'Foo',
              value: 'Bar'
            }],
            responseHeaders: [{
              name: 'Baz',
              value: 'Blah'
            }]
            },{
            ...
            }]


             */
            sb.append("[");
            for (Iterator<Entry> iterator = entries.iterator(); iterator.hasNext();) {
                Entry entry = iterator.next();
                sb.append("{\n");

                sb.append("    statusCode: ").append(entry.statusCode).append(",\n");
                sb.append("    method: ").append(json(entry.method)).append(",\n");
                sb.append("    url: ").append(json(entry.url)).append(",\n");
                sb.append("    bytes: ").append(entry.bytes).append(",\n");
                sb.append("    start: '").append(sdf.format(entry.start)).append("',\n");
                sb.append("    end: '").append(sdf.format(entry.end)).append("',\n");
                sb.append("    timeInMillis: ").append((entry.end.getTime() - entry.start.getTime())).append(",\n");

                sb.append("    requestHeaders:[");
                jsonHeaders(sb, entry.requestHeaders);
                sb.append("],\n");

                sb.append("    responseHeaders:[");
                jsonHeaders(sb, entry.responseHeaders);
                sb.append("]\n");

                sb.append("}");

                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("]\n");
        } else if ("xml".equalsIgnoreCase(type)) {
            /*
            <traffic>
             <entry statusCode="200" method="GET" url="http://foo.com/index.html" bytes="12422" start="2009-03-15T14:23:00.000-0700" end="2009-03-15T14:23:00.102-0700" timeInMillis="102">
              <requestHeaders>
               <header name=""></header>
              </requestHeaders>
              <responseHeaders>
               <header name=""></header>
              </responseHeaders>
             </entry>
            </traffic>
             */
            sb.append("<traffic>\n");
            for (Entry entry : entries) {
                sb.append("<entry ");

                sb.append("statusCode=\"").append(entry.statusCode).append("\" ");
                sb.append("method=\"").append(json(entry.method)).append("\" ");
                sb.append("url=\"").append(xml(entry.url)).append("\" ");
                sb.append("bytes=\"").append(entry.bytes).append("\" ");
                sb.append("start=\"").append(sdf.format(entry.start)).append("\" ");
                sb.append("end=\"").append(sdf.format(entry.end)).append("\" ");
                sb.append("timeInMillis=\"").append((entry.end.getTime() - entry.start.getTime())).append("\">\n");

                sb.append("    <requestHeaders>\n");
                xmlHeaders(sb, entry.requestHeaders);
                sb.append("    </requestHeaders>\n");

                sb.append("    <responseHeaders>\n");
                xmlHeaders(sb, entry.responseHeaders);
                sb.append("    </responseHeaders>\n");


                sb.append("</entry>\n");
            }
            sb.append("</traffic>\n");
        } else {
            /*
             200 GET http://foo.com/index.html
             12422 bytes
             102ms (2009-03-15T14:23:00.000-0700 - 2009-03-15T14:23:00.102-0700)

             Request Headers
              - Foo => Bar
             Response Headers
              - Baz => Blah
             ================================================================

             */


            for (Entry entry : entries) {
                sb.append(entry.statusCode).append(" ").append(entry.method).append(" ").append(entry.url).append("\n");
                sb.append(entry.bytes).append(" bytes\n");
                sb.append(entry.end.getTime() - entry.start.getTime()).append("ms (").append(sdf.format(entry.start)).append(" - ").append(sdf.format(entry.end)).append("\n");
                sb.append("\n");
                sb.append("Request Headers\n");
                for (Header header : entry.requestHeaders) {
                    sb.append(" - ").append(header.name).append(" => ").append(header.value).append("\n");
                }
                sb.append("Response Headers\n");
                for (Header header : entry.responseHeaders) {
                    sb.append(" - ").append(header.name).append(" => ").append(header.value).append("\n");
                }
                sb.append("================================================================\n");
                sb.append("\n");
            }
        }

        clear();

        return "OK," + sb.toString();
    }

    private void xmlHeaders(StringBuilder sb, List<Header> headers) {
        for (Header header : headers) {
            sb.append("        <header name=\"").append(xml(header.name)).append("\">").append(xml(header.value)).append("</header>\n");
        }
    }

    private void jsonHeaders(StringBuilder sb, List<Header> headers) {
        for (Iterator<Header> headItr = headers.iterator(); headItr.hasNext();) {
            Header header = headItr.next();

            sb.append("{\n");
            sb.append("        name: ").append(json(header.name)).append(",\n");
            sb.append("        value: ").append(json(header.value)).append("\n");
            sb.append("    }");
            if (headItr.hasNext()) {
                sb.append(",");
            }

        }
    }

    private String xml(String s) {
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("\\<", "&lt;");
        s = s.replaceAll("\\>", "&gt;");

        return s;
    }

    private Object json(String s) {
        s = s.replaceAll("\\'", "\\\\'");
        s = s.replaceAll("\n", "\\\\n");

        return "'" + s + "'";
    }

    public static class Entry {
        private String method;
        private String url;
        private int statusCode;
        private Date start;
        private Date end;
        private long bytes;
        private List<Header> requestHeaders = new ArrayList<Header>();
        private List<Header> responseHeaders = new ArrayList<Header>();

        public Entry(String method, String url) {
            this.method = method;
            this.url = url;
            this.start = new Date();
        }

        public void finish(int statusCode, long bytes) {
            this.statusCode = statusCode;
            this.bytes = bytes;
            this.end = new Date();
        }

        public void addRequestHeaders(HttpRequest request) {
            Enumeration names = request.getFieldNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String value = request.getField(name);

                requestHeaders.add(new Header(name, value));
            }
        }

        public void addResponseHeader(HttpResponse response) {
            Enumeration names = response.getFieldNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String value = response.getField(name);

                responseHeaders.add(new Header(name, value));
            }
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        @Override
        public String toString() {
            return method + "|" + statusCode + "|" + url + "|" + requestHeaders.size() + "|" + responseHeaders.size() + "\n";
        }

        public void addRequestHeader(String key, String value) {
            this.requestHeaders.add(new Header(key, value));
        }
    }

    public static class Header {
        private String name;
        private String value;

        public Header(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
