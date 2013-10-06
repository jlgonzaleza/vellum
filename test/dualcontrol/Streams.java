/*
 * Source https://code.google.com/p/vellum by @evanxsummers

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.  
       
 */
package dualcontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;
import vellum.exception.Exceptions;
import vellum.exception.SizeRuntimeException;

/**
 *
 * @author evan
 */
public class Streams {
    private final static Logger logger = Logger.getLogger(Streams.class);
    
    public static void close(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ioe) {
                logger.warn(ioe.getMessage());
            }
        }
    }

    public static void close(ServerSocket serverSocket) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ioe) {
                logger.warn(ioe.getMessage());
            }
        }
    }
    
    public static String readString(InputStream stream, long capacity) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null) {
                    return builder.toString();
                }
                builder.append(line);
                builder.append("\n");
                if (capacity > 0 && builder.length() > capacity) {
                    throw new SizeRuntimeException(builder.length());
                }
            } catch (Exception e) {
                throw Exceptions.newRuntimeException(e);
            }
        }
    }
    
}