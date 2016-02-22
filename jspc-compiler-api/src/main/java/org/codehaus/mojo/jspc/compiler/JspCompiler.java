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

package org.codehaus.mojo.jspc.compiler;

import java.io.File;

/**
 * Interface to provide plugable JSP compilation.
 *
 * @version $Id$
 */
public interface JspCompiler {
    void setWebappDirectory(String webappDir);
    
    void setOutputDirectory(File outputDirectory);
    
    void setEncoding(String encoding);
    
    void setShowSuccess(boolean showSuccesses);
    
    void setListErrors(boolean listErrors);
    
    void setWebFragmentFile(File webFragmentFile);
    
    void setPackageName(String packageName);
    
    void setClasspath(Iterable<String> classpathElements);

    void setSmapDumped(boolean setSmapDumped);

    void setSmapSuppressed(boolean setSmapSuppressed);

    void setCompile(boolean setCompile);

    void setValidateXml(boolean validateXml);

    void setTrimSpaces(boolean trimSpaces);

    void setErrorOnUseBeanInvalidClassAttribute(boolean error);

    void setVerbose(int verbose);

    void setCompilerSourceVM(String source);

    void setCompilerTargetVM(String target);

    void setCaching(boolean caching);

    void setGenStringAsCharArray(boolean genStringAsCharArray);

    void setPoolingEnabled(boolean poolingEnabled);

    void setClassDebugInfo(boolean classDebugInfo);
    
    void setCompileThreads(int threads);
    
    void setCompileTimeout(long timeout);
    
    void compile(Iterable<File> jspFiles) throws Exception;
}
