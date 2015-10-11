/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.mojo.jspc.compiler.tomcat7;

import org.apache.jasper.JspC;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

/**
 * JspC Implementation
 */
public class JspCImpl extends JspC {

    private Map<String, String> parameters;

    protected ServletContext getServletContext() {
        if (context == null) {
            initServletContext();
        }
        return context;
    }

    public void setContextInitParameter(String name, String value) {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        parameters.put(name, value);
    }

    public void execute() {
        if (parameters != null) {
            for(Map.Entry<String, String> entry: parameters.entrySet())
            getServletContext().setInitParameter(entry.getKey(), entry.getValue());
        }
        super.execute();
    }
}
