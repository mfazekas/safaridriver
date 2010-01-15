/*
 * Created on Feb 25, 2006
 *
 */
package org.openqa.selenium.server.htmlrunner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.openqa.jetty.http.HttpContext;
import org.openqa.jetty.http.HttpException;
import org.openqa.jetty.http.HttpHandler;
import org.openqa.jetty.http.HttpRequest;
import org.openqa.jetty.http.HttpResponse;
import org.openqa.jetty.log.LogFactory;
import org.openqa.jetty.util.StringUtil;

/**
 * Handles results of HTMLRunner (aka TestRunner, FITRunner) in automatic mode.
 *  
 * @author Dan Fabulich
 * @author Darren Cotterill
 * @author Ajit George
 *
 */
@SuppressWarnings("serial")
public class SeleniumHTMLRunnerResultsHandler implements HttpHandler {
    static Log log = LogFactory.getLog(SeleniumHTMLRunnerResultsHandler.class);
    
    HttpContext context;
    List<HTMLResultsListener> listeners;
    boolean started = false;
    
    public SeleniumHTMLRunnerResultsHandler() {
        listeners = new Vector<HTMLResultsListener>();
    }
    
    public void addListener(HTMLResultsListener listener) {
        listeners.add(listener);
    }
    
    public void handle(String pathInContext, String pathParams, HttpRequest request, HttpResponse res) throws HttpException, IOException {
        if (!"/postResults".equals(pathInContext)) return;
        request.setHandled(true);
        log.info("Received posted results");
        String result = request.getParameter("result");
        if (result == null) {
            res.getOutputStream().write("No result was specified!".getBytes());
        }
        String seleniumVersion = request.getParameter("selenium.version");
        String seleniumRevision = request.getParameter("selenium.revision");
        String totalTime = request.getParameter("totalTime");
        String numTestTotal = request.getParameter("numTestTotal");
        String numTestPasses = request.getParameter("numTestPasses");
        String numTestFailures = request.getParameter("numTestFailures");
        String numCommandPasses = request.getParameter("numCommandPasses");
        String numCommandFailures = request.getParameter("numCommandFailures");
        String numCommandErrors = request.getParameter("numCommandErrors");
        String suite = request.getParameter("suite");
        String postedLog = request.getParameter("log");
        
        int numTotalTests = Integer.parseInt(numTestTotal);
        
        List<String> testTables = createTestTables(request, numTotalTests);

        
        HTMLTestResults results = new HTMLTestResults(seleniumVersion, seleniumRevision,
                result, totalTime, numTestTotal,
                numTestPasses, numTestFailures, numCommandPasses, numCommandFailures, numCommandErrors, suite, testTables, postedLog);
        
        for (Iterator<HTMLResultsListener> i = listeners.iterator(); i.hasNext();) {
            HTMLResultsListener listener = i.next();
            listener.processResults(results);
            i.remove();
        }
        processResults(results, res);
    }
    
    /** Print the test results out to the HTML response */
    private void processResults(HTMLTestResults results, HttpResponse res) throws IOException {
        res.setContentType("text/html");
        OutputStream out = res.getOutputStream();
        Writer writer = new OutputStreamWriter(out, StringUtil.__ISO_8859_1);
        results.write(writer);
        writer.flush();
    }
    
    private List<String> createTestTables(HttpRequest request, int numTotalTests) {
        List<String> testTables = new LinkedList<String>();
        for (int i = 1; i <= numTotalTests; i++) {
            String testTable = request.getParameter("testTable." + i);
            //System.out.println("table " + i);
            //System.out.println(testTable);
            testTables.add(testTable);
        }
        return testTables;
    }

    public String getName() {
        return SeleniumHTMLRunnerResultsHandler.class.getName();
    }

    public HttpContext getHttpContext() {
        return context;
    }

    public void initialize(HttpContext c) {
        this.context = c;
        
    }

    public void start() throws Exception {
        started = true;
    }

    public void stop() throws InterruptedException {
        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
