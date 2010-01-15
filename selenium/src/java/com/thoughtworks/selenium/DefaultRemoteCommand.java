/*
 * Copyright 2006 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.thoughtworks.selenium;

import java.io.*;
import java.net.*;

/**
 * The default implementation of the RemoteCommand interface
 * 
 * @see com.thoughtworks.selenium.RemoteCommand
 * @author Paul Hammant
 * @version $Revision$
 */
public class DefaultRemoteCommand implements RemoteCommand {
    // as we have beginning and ending pipes, we will have 1 more entry than we need
    private static final int NUMARGSINCLUDINGBOUNDARIES = 4;
    private static final int FIRSTINDEX = 1;
    private static final int SECONDINDEX = 2;
    private static final int THIRDINDEX = 3;
    private final String command;
    private final String[] args;


    public DefaultRemoteCommand(String command, String[] args) {
        this.command = command;
        this.args = args;
        if ("selectWindow".equals(command) && args[0]==null) {
            // hackylicious I know, but what a dorky interface!  Users naturally give us too much credit, and submit a null argument
            // instead of a string "null".  Our code elsewhere assumes that all arguments are non-null, so
            // I fix this up here in order to avoid trouble later:
            args[0] = "null";
        }
    }

    public String getCommandURLString() {
        StringBuffer sb = new StringBuffer("cmd=");
        sb.append(urlEncode(command));
        if (args == null) return sb.toString();
        for (int i = 0; i < args.length; i++) {
            sb.append('&');
            sb.append(Integer.toString(i+1));
            sb.append('=');
            sb.append(urlEncode(args[i]));
        }
        return sb.toString();
    }
    
    public String toString() {
        return getCommandURLString();
    }

    /** Factory method to create a RemoteCommand from a wiki-style input string */
    public static RemoteCommand parse(String inputLine) {
        if (null == inputLine) throw new NullPointerException("inputLine can't be null");
        String[] values = inputLine.split("\\|");
        if (values.length != NUMARGSINCLUDINGBOUNDARIES) {
            throw new IllegalStateException("Cannot parse invalid line: " + inputLine + values.length);
        }
        return new DefaultRemoteCommand(values[FIRSTINDEX], new String[] {values[SECONDINDEX], values[THIRDINDEX]});
    }
    
    /** Encodes the text as an URL using UTF-8.
     * 
     * @param text the text too encode
     * @return the encoded URI string
     * @see URLEncoder#encode(java.lang.String, java.lang.String)
     */
    public static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
