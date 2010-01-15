package org.openqa.selenium.server;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.openqa.jetty.http.*;
import org.openqa.jetty.http.handler.*;
import org.openqa.jetty.util.*;

public class StaticContentHandler extends ResourceHandler {
	private static final long serialVersionUID = 8031049889874827358L;
	private static boolean slowResources;
    private List<ResourceLocator> resourceLocators = new ArrayList<ResourceLocator>();
    public static final int SERVER_DELAY = 1000;
    private final String debugURL;
    private final boolean proxyInjectionMode;

    public StaticContentHandler(String debugURL, boolean proxyInjectionMode) {
        this.debugURL = debugURL;
        this.proxyInjectionMode = proxyInjectionMode;
    }

    public void handle(String pathInContext, String pathParams, HttpRequest httpRequest, HttpResponse httpResponse)
            throws IOException {

        hackRemoveLastModifiedSince(httpRequest);
        setNoCacheHeaders(httpResponse);
        if (pathInContext.equals("/core/RemoteRunner.html") && proxyInjectionMode) {
            pathInContext = pathInContext.replaceFirst("/core/RemoteRunner.html",
                    "/core/InjectedRemoteRunner.html");
        }
        callSuperHandle(pathInContext, pathParams, httpRequest, httpResponse);

        String resourceName = getResource(pathInContext).getName();
        if (resourceName.endsWith("MISSING RESOURCE")) {
        	httpResponse.setAttribute("NotFound", "True");
        }
        
    }
    
    protected void callSuperHandle(String pathInContext, String pathParams, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
    	super.handle(pathInContext, pathParams, httpRequest, httpResponse);
    }
    
    /** DGF Opera just refuses to honor my cache settings.  This will
     * force jetty to return the document anyway.
     */
    private void hackRemoveLastModifiedSince(HttpRequest req) {
        if (null == req.getField(HttpFields.__IfModifiedSince)) {
            return;
        }
        try {
            Field f = HttpMessage.class.getDeclaredField("_header");
            f.setAccessible(true);
            HttpFields header = (HttpFields) f.get(req);
            header.remove(HttpFields.__IfModifiedSince);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** Sets all the don't-cache headers on the HttpResponse */
    private void setNoCacheHeaders(HttpResponse res) {
        res.setField(HttpFields.__CacheControl, "no-cache");
        res.setField(HttpFields.__Pragma, "no-cache");
        res.setField(HttpFields.__Expires, HttpFields.__01Jan1970);
    }


    protected Resource getResource(final String pathInContext) throws IOException {
        delayIfNeed(pathInContext);
        // DGF go through the resource locators in reverse order, to prefer the classpath locator last
        for (int i = resourceLocators.size()-1; i >= 0; i--) {
            ResourceLocator resourceLocator = resourceLocators.get(i);
            Resource resource = resourceLocator.getResource(getHttpContext(), pathInContext);
            if (resource.exists()) {
                return resource;
            }
        }
        return Resource.newResource("MISSING RESOURCE");
    }

    private void delayIfNeed(String pathInContext) {
        if (slowResources) {
            pause(SERVER_DELAY);
            if (pathInContext != null && pathInContext.endsWith("slow.html")) {
                pause(SERVER_DELAY);
            }
        }
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }


    public void addStaticContent(ResourceLocator locator) {
        resourceLocators.add(locator);
    }

    public void sendData(HttpRequest request,
                         HttpResponse response,
                         String pathInContext,
                         Resource resource,
                         boolean writeHeaders) throws IOException {
        if (!proxyInjectionMode) {
            super.sendData(request, response, pathInContext, resource, writeHeaders);
            return;
        }
        ResourceCache.ResourceMetaData metaData = (ResourceCache.ResourceMetaData) resource.getAssociate();
        String mimeType = metaData.getMimeType();
        response.setContentType(mimeType);
        if (resource.length() != -1) {
            response.setField(HttpFields.__ContentLength, metaData.getLength());
        }
        InjectionHelper.injectJavaScript(request, response, resource.getInputStream(), response.getOutputStream(), debugURL);
        request.setHandled(true);
    }
    
    public static void setSlowResources(boolean slowResources) {
        StaticContentHandler.slowResources = slowResources;
    }
    
    public static boolean getSlowResources() {
        return slowResources;
    }
}