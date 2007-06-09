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

/**
 * Interface to provide plugable JSP compilation.
 *
 * @version $Id$
 */
public interface JspCompiler
{
    //
    // HACK: For now just expose the same API as JspC, should eventually abstract this a little more
    //

    void setArgs(final String[] strArgs);

    void setSmapDumped(final boolean setSmapDumped);

    void setSmapSuppressed(final boolean setSmapSupressed);

    void setCompile(final boolean setCompile);

    void setValidateXml(final boolean validateXml);

    void setTrimSpaces(final boolean trimSpaces);

    void setVerbose(final int verbose);

    void setCompilerSourceVM(final String source);

    void setCompilerTargetVM(final String target);

    void compile() throws Exception;
}
