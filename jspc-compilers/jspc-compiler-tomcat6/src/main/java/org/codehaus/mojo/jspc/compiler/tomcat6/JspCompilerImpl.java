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

package org.codehaus.mojo.jspc.compiler.tomcat6;

import org.codehaus.mojo.jspc.compiler.JspCompiler;
import org.codehaus.plexus.component.annotations.Component;

import org.apache.jasper.JspC;

/**
 * JSP compiler for Tomcat 6.
 *
 * @version $Id$
 */
@Component(role=JspCompiler.class, hint="tomcat6")
public class JspCompilerImpl
    implements JspCompiler
{
    private String[] args;

    private boolean smapDumped;

    private boolean smapSuppressed;

    private boolean compile;

    private boolean validateXml;

    private boolean trimSpaces;

    private int verbose;

    private String compilerSource;

    private String compilerTarget;

    private boolean errorOnUseBeanInvalidClassAttribute;

    public void setArgs(final String[] args) {
        this.args = args;
    }

    public void setSmapDumped(final boolean smapDumped) {
        this.smapDumped = smapDumped;
    }

    public void setSmapSuppressed(final boolean smapSuppressed) {
        this.smapSuppressed = smapSuppressed;
    }

    public void setCompile(final boolean compile) {
        this.compile = compile;
    }

    public void setValidateXml(final boolean validateXml) {
        this.validateXml = validateXml;
    }

    public void setTrimSpaces(final boolean trimSpaces) {
        this.trimSpaces = trimSpaces;
    }

    public void setVerbose(final int verbose) {
        this.verbose = verbose;
    }

    public void setCompilerSourceVM(final String source) {
        this.compilerSource = source;
    }

    public void setCompilerTargetVM(final String target) {
        this.compilerTarget = target;
    }

    public void setErrorOnUseBeanInvalidClassAttribute(boolean error) {
        this.errorOnUseBeanInvalidClassAttribute = error;
    }

    public void compile() throws Exception {
        JspC jspc = new JspC();
        jspc.setArgs(args);
        jspc.setSmapDumped(smapDumped);
        jspc.setSmapSuppressed(smapSuppressed);
        jspc.setCompile(compile);
        jspc.setValidateXml(validateXml);
        jspc.setTrimSpaces(trimSpaces);
        jspc.setVerbose(verbose);
        jspc.setErrorOnUseBeanInvalidClassAttribute(errorOnUseBeanInvalidClassAttribute);

        // Fail on error - important
        jspc.setFailOnError(true);

        if (compilerSource != null) {
            jspc.setCompilerSourceVM(compilerSource);
        }

        if (compilerTarget != null) {
            jspc.setCompilerTargetVM(compilerTarget);
        }

        jspc.execute();
    }
}
