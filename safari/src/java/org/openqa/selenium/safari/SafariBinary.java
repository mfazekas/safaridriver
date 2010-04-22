package org.openqa.selenium.safari;

import org.openqa.selenium.WebDriverException;

import java.net.HttpURLConnection;
import java.io.IOException;
import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import java.util.Map;
import java.util.List;

/**
 * This class handles the launching, attaching, and terminating Safari.
 * 
 * @author kurniady@google.com (Andrian Kurniady)
 */
public class SafariBinary {
  private static final String SAFARI_BINARY_LOCATION = 
      "/Applications/Safari.app/Contents/MacOS/Safari";
  
  private static final int MAX_LOAD_WAIT = 10000;
  
  private ProcessWrapper safariProcess;
  private ProcessBuilder processBuilder;
  private SafariExtension safariExtension;
  
  interface ProcessListener {
    void onProcessFinished();
  }

  private class ProcessWrapper implements Runnable {
    private final Process process;
    private final List<ProcessListener> listeners;
    
    ProcessWrapper(Process process) {
      this.process = process;
      this.listeners = new ArrayList<ProcessListener> ();
      Runtime.getRuntime().addShutdownHook(new Thread(){
        @Override
        public void run() {
          quit();
        }
      });
      new Thread(this).start();
    }
    
    void addProcessListener(ProcessListener listener) {
      listeners.add(listener);
    }
    
    void quit() {
      process.destroy();
    }
    
    void waitFor() throws InterruptedException {
      process.waitFor();
    }
    
    public void run() {
      int in = 0;
      while (in != -1) {
        try {
          in = process.getInputStream().read();    
          System.out.print((char)in);
        } catch (IOException e) {
          System.err.println("Safari terminated. (" + e + ")");
          break;
        }
      }
      
      for (ProcessListener listener : listeners) {
        listener.onProcessFinished();
      }
    }    
  }
  
  public SafariBinary() {
    processBuilder = 
      new ProcessBuilder(SAFARI_BINARY_LOCATION).redirectErrorStream(true);
    
    safariExtension = new SafariExtension();
  }
  
  private void setupBuilderEnvironments() throws IOException {
    Map<String, String> extraEnvs = new HashMap<String, String>(); 
    safariExtension.addLoadExtensionEnvs(extraEnvs,SAFARI_BINARY_LOCATION);
    safariExtension.addListenPortEnv(extraEnvs,new URL(getUrl()));
    processBuilder.environment().putAll(extraEnvs);	
  }
  
  private void waitForServerToRespond(URL appUrl,long timeoutInMilliseconds,ProcessWrapper process) throws Exception {
    long start = System.currentTimeMillis();
    boolean responding = false;
    while (!responding && (System.currentTimeMillis() - start < timeoutInMilliseconds)) {
      HttpURLConnection connection = null;
      try {
        connection = (HttpURLConnection) appUrl.openConnection();
        connection.setConnectTimeout(500);
        connection.setRequestMethod("TRACE");
        connection.connect();
        responding = true;
      } catch (IOException e) {
        responding = false;
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }
    }
    if (!responding) {
      System.out.println("Waiting for process:"+process+"!\n");
      process.waitFor();
      throw new Exception("Unable to start serer on url:" + appUrl +  "time:" + (System.currentTimeMillis() - start) + "process:"+ process);
    }
  }
  
  public void startSafari() {
    if (safariProcess == null) {
      try {
        setupBuilderEnvironments();
        
        safariProcess = new ProcessWrapper(processBuilder.start());
        safariProcess.addProcessListener(new ProcessListener() {
          public void onProcessFinished() {
            safariProcess = null;            
          }          
        });
        
        waitForServerToRespond(new URL(getUrl()),MAX_LOAD_WAIT,safariProcess);
      } catch (Exception e) {
    	quit();
        throw new WebDriverException("Failed to launch Safari.", e);
      }
    } else {
      throw new WebDriverException("Safari is already running.");
    }
  }
  
  public boolean isRunning() {
    return safariProcess == null;
  }
  
  public String getUrl() {
    return "http://localhost:4000/hub";
  }
  
  public void quit() {
    if (safariProcess != null) {
      safariProcess.quit();
      safariProcess = null;
    }
    safariExtension.clean();
  }
}
