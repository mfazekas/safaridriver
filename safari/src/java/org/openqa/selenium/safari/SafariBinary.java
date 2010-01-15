package org.openqa.selenium.safari;

import org.openqa.selenium.WebDriverException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the launching, attaching, and terminating Safari.
 * 
 * @author kurniady@google.com (Andrian Kurniady)
 */
public class SafariBinary {
  private static final String SAFARI_BINARY_LOCATION = 
      "/Applications/Safari.app/Contents/MacOS/Safari";
  
  private static final int LOAD_WAIT = 5000;
  
  private ProcessWrapper safariProcess;
  private ProcessBuilder processBuilder;
  
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
    
    @Override
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
  }
  
  public void startSafari() {
    if (safariProcess == null) {
      try {
        safariProcess = new ProcessWrapper(processBuilder.start());
        safariProcess.addProcessListener(new ProcessListener() {          
          @Override
          public void onProcessFinished() {
            safariProcess = null;            
          }          
        });
        
        // TODO(kurniady): Add polling every 1 second until Safari's extension is loaded
        // currently we just rely on RemoteWebDriver's failure to know that the extension
        // hasn't been loaded, which may be good enough.
        Thread.sleep(LOAD_WAIT);
      } catch (Exception e) {
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
  }
}
