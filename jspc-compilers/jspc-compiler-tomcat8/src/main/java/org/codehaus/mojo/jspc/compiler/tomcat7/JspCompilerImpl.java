/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.lang.StringUtils;
import org.apache.jasper.JspC;
import org.codehaus.mojo.jspc.compiler.JspCompiler;

/**
 * JSP compiler for Tomcat 6.
 *
 * @version $Id$
 */
public class JspCompilerImpl implements JspCompiler {
    private final JspC jspc;
    private boolean showSuccess = false;
    private boolean listErrors = false;
    
    public JspCompilerImpl() {
        jspc = new JspC();
        jspc.setFailOnError(true);
    }

    public void setWebappDirectory(String webappDir) {
        jspc.setUriroot(webappDir);
    }

    public void setOutputDirectory(File outputDirectory) {
        jspc.setOutputDir(outputDirectory.getAbsolutePath());
    }

    public void setEncoding(String encoding) {
        jspc.setJavaEncoding(encoding);
    }

    public void setShowSuccess(boolean showSuccess) {
        this.showSuccess = showSuccess;
    }

    public void setListErrors(boolean listErrors) {
        this.listErrors = listErrors;
    }

    public void setWebFragmentFile(File webFragmentFile) {
        jspc.setWebXmlFragment(webFragmentFile.getAbsolutePath());
    }

    public void setPackageName(String packageName) {
        jspc.setPackage(packageName);
    }

    public void setClasspath(Iterable<String> classpathElements) {
        final String classpath = StringUtils.join(classpathElements.iterator(), File.pathSeparator);
        jspc.setClassPath(classpath);
    }

    public void setSmapDumped(final boolean smapDumped) {
        jspc.setSmapDumped(smapDumped);
    }

    public void setSmapSuppressed(final boolean smapSuppressed) {
        jspc.setSmapSuppressed(smapSuppressed);
    }

    public void setCompile(final boolean compile) {
        jspc.setCompile(compile);
    }

    public void setValidateXml(final boolean validateXml) {
        jspc.setValidateXml(validateXml);
    }

    public void setTrimSpaces(final boolean trimSpaces) {
        jspc.setTrimSpaces(trimSpaces);
    }

    public void setErrorOnUseBeanInvalidClassAttribute(boolean error) {
        jspc.setErrorOnUseBeanInvalidClassAttribute(error);
    }

    public void setVerbose(final int verbose) {
        jspc.setVerbose(verbose);
    }

    public void setCompilerSourceVM(final String source) {
        jspc.setCompilerSourceVM(source);
    }

    public void setCompilerTargetVM(final String target) {
        jspc.setCompilerTargetVM(target);
    }

    public void compile(Iterable<File> jspFiles) throws Exception {
        final List<String> args = new ArrayList<String>();
        
        if (showSuccess) {
            args.add("-s");
        }
        
        if (listErrors) {
            args.add("-l");
        }
        
        for (final File jspFile : jspFiles) {
            args.add(jspFile.getAbsolutePath());
        }
        
        jspc.setArgs(args.toArray(new String[args.size()]));

        jspc.execute();
    }

  public void setCaching(boolean caching) {
    jspc.setCaching(caching);
  }

  public void setGenStringAsCharArray(boolean genStringAsCharArray) {
    jspc.setGenStringAsCharArray(genStringAsCharArray);
  }

  public void setPoolingEnabled(boolean poolingEnabled) {
    jspc.setPoolingEnabled(poolingEnabled);
    
  }

  public void setClassDebugInfo(boolean classDebugInfo) {
    jspc.setClassDebugInfo(classDebugInfo);
    
  }

  public void setCompileThreads(int threads) {
    //TODO make multithreaded
    
  }

  public void setCompileTimeout(long timeout) {
    //TODO set timeout
    
  }
}
