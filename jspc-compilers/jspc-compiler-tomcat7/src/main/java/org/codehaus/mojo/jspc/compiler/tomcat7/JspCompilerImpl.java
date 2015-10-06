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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.apache.jasper.JspC;
import org.codehaus.mojo.jspc.compiler.JspCompiler;

/**
 * JSP compiler for Tomcat 7.
 *
 * @version $Id$
 */
public class JspCompilerImpl implements JspCompiler {
    private boolean showSuccess = false;
    private boolean listErrors = false;
	private boolean failOnError;
	private String classpath;
	private File outputDirectory;
	private String encoding;
	private File webFragmentFile;
	private String packageName;
	private boolean smapDumped;
	private boolean smapSuppressed;
	private boolean compile;
	private boolean validateXml;
	private boolean trimSpaces;
	private boolean errorOnUseBeanInvalidClassAttribute;
	private int verbose;
	private String compilerSourceVM;
	private String compilerTargetVM;
	private String webappDir;
    
    public JspCompilerImpl() {
    	this.failOnError = true;
    }

    public void setWebappDirectory(String webappDir) {
    	this.webappDir = webappDir;
    }

    public void setOutputDirectory(File outputDirectory) {
    	this.outputDirectory = outputDirectory;
    }

    public void setEncoding(String encoding) {
    	this.encoding = encoding;
    }

    public void setShowSuccess(boolean showSuccess) {
        this.showSuccess = showSuccess;
    }

    public void setListErrors(boolean listErrors) {
        this.listErrors = listErrors;
    }

    public void setWebFragmentFile(File webFragmentFile) {
    	this.webFragmentFile = webFragmentFile;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setClasspath(Iterable<String> classpathElements) {
        classpath = StringUtils.join(classpathElements.iterator(), File.pathSeparator);
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

    public void setErrorOnUseBeanInvalidClassAttribute(boolean error) {
    	this.errorOnUseBeanInvalidClassAttribute = error;
    }

    public void setVerbose(final int verbose) {
    	this.verbose = verbose;
    }

    public void setCompilerSourceVM(final String source) {
    	this.compilerSourceVM = source;
    }

    public void setCompilerTargetVM(final String target) {
    	this.compilerTargetVM = target;
    }

    public void compile(Iterable<File> jspFiles) throws Exception {
        final List<String> args = new ArrayList<String>();
        
        if (showSuccess) {
            args.add("-s");
        }
        
        if (listErrors) {
            args.add("-l");
        }
        
        System.out.println("Creating executor");
        System.out.println("---------------------------------");
        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        List<Callable<String>> jobs = new ArrayList<Callable<String>>();
        
		for (final File jspFile : jspFiles) {
			jobs.add(new Callable<String>() {
				public String call() throws Exception {
					ArrayList<String> argList = new ArrayList<String>(args);
					argList.add(jspFile.getAbsolutePath());
					JspC jspc = new JspC(); 
					jspc.setFailOnError(failOnError);
			        jspc.setUriroot(webappDir);
			        jspc.setOutputDir(outputDirectory.getAbsolutePath());
			        jspc.setJavaEncoding(encoding);
			        jspc.setWebXmlFragment(webFragmentFile.getAbsolutePath());
			        jspc.setPackage(packageName);
			        jspc.setClassPath(classpath);

			        jspc.setSmapDumped(smapDumped);
			        jspc.setSmapSuppressed(smapSuppressed);
			        jspc.setCompile(compile);
			        jspc.setValidateXml(validateXml);
			        jspc.setTrimSpaces(trimSpaces);
			        jspc.setErrorOnUseBeanInvalidClassAttribute(errorOnUseBeanInvalidClassAttribute);
			        jspc.setVerbose(verbose);
			        jspc.setCompilerSourceVM(compilerSourceVM);
			        jspc.setCompilerTargetVM(compilerTargetVM);
					
					jspc.setArgs(args.toArray(new String[args.size()]));

					jspc.execute();
					return "compiled: " + jspFile.getAbsolutePath();
				}
			});
		}
		executor.invokeAll(jobs);
    }
}
