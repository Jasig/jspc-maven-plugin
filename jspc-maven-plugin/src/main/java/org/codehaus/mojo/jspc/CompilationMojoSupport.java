/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.mojo.jspc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.XmlStreamReader;
import org.apache.commons.io.output.XmlStreamWriter;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFileFilterRequest;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.mojo.jspc.compiler.JspCompiler;
import org.codehaus.mojo.jspc.compiler.JspCompilerFactory;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.build.incremental.BuildContext;

abstract class CompilationMojoSupport extends AbstractMojo {
    private static final String DEFAULT_INJECT_STRING = "</web-app>";
    
    /**
     * The working directory to create the generated java source files.
     */
    @Parameter(defaultValue="${project.build.directory}/jsp-source", required=true)
    File workingDirectory;
    
    /**
     * The sources of the webapp. If not specified all files under {@link #defaultSourcesDirectory} are used
     */
    @Parameter
    FileSet sources;

    /**
     * Used if {@link #sources} is not specified
     */
    @Parameter(defaultValue="${project.basedir}/src/main/webapp")
    File defaultSourcesDirectory;
    
    /**
     * The path and location to the web fragment file.
     */
    @Parameter(defaultValue="${project.build.directory}/web-fragment.xml", required=true)
    File webFragmentFile;
    
    /**
     * The path and location of the original web.xml file.
     */
    @Parameter(defaultValue="${basedir}/src/main/webapp/WEB-INF/web.xml", required=true)
    File inputWebXml;
    
    /**
     * The final path and file name of the web.xml.
     */
    @Parameter(defaultValue="${project.build.directory}/jspweb.xml", required=true)
    File outputWebXml;
    
    /**
     * Character encoding.
     */
    @Parameter(property="encoding", defaultValue="${project.build.sourceEncoding}")
    String javaEncoding = "UTF-8";

    /**
     * Provide source compatibility with specified release.
     */
    @Parameter(defaultValue="${project.build.sourceVersion}")
    String source;

    /**
     * Generate class files for specific VM version.
     */
    @Parameter(defaultValue="${project.build.targetVersion}")
    String target;

    /**
     * Sets if you want to compile the JSP classes.
     */
    @Parameter(defaultValue="true")
    boolean compile;

    /**
     * Set this to false if you don"t want to include the compiled JSPs
     * in your web.xml nor add the generated sources to your project"s
     * source roots.
     */
    @Parameter(defaultValue="true")
    boolean includeInProject;

    /**
     * The string to look for in the web.xml to replace with the web fragment
     * contents
     * 
     * If not defined, fragment will be appended before the &lt;/webapp&gt; tag
     * which is fine for servlet 2.4 and greater.  If using this parameter its
     * recommanded to use Strings such as
     * &lt;!-- [INSERT FRAGMENT HERE] --&gt;
     *
     * Be aware the &lt; and &gt; are for your pom verbatim.
     */
    @Parameter(defaultValue=DEFAULT_INJECT_STRING)
    String injectString;
    
    /**
     * The package in which the jsp files will be contained.
     */
    @Parameter(defaultValue="jsp")
    String packageName;

    /**
     * Verbose level option for JspC.
     */
    @Parameter(defaultValue="0")
    int verbose;

    /**
     * Show Success option for JspC.
     */
    @Parameter(defaultValue="true")
    boolean showSuccess;

    /**
     * Set Smap Dumped option for JspC.
     */
    @Parameter(defaultValue="false")
    boolean smapDumped;

    /**
     * Set Smap Suppressed option for JspC.
     */
    @Parameter(defaultValue="false")
    boolean smapSuppressed;

    /**
     * List Errors option for JspC.
     */
    @Parameter(defaultValue="true")
    boolean listErrors;

    /**
     * Validate XML option for JspC.
     */
    @Parameter(defaultValue="false")
    boolean validateXml;

    /**
     * Removes the spaces from the generated JSP files.
     */
    @Parameter(defaultValue="true")
    boolean trimSpaces;

    /**
     * Provides filtering of the generated web.xml text.
     */
    @Parameter(defaultValue="true")
    boolean filtering;

    /**
     * Set to {@code true} to disable the plugin.
     */
    @Parameter(property="jspc.skip", defaultValue="false")
    boolean skip;

    @Parameter(property="eLInterpreterClass")
    String eLInterpreterClass;

    @Parameter(property = "genStringAsCharArray")
    boolean genStringAsCharArray;


    @Parameter(property = "enablePooling", defaultValue="true")
    boolean enablePooling;

    /**
     * Issue an error when the value of the class attribute in a useBean action is
     * not a valid bean class
     */
    @Parameter(defaultValue="true")
    boolean errorOnUseBeanInvalidClassAttribute;

    //
    // Components
    //


    /**
     * The Maven project.
     */
    @Component
    private MavenProject project;
    
    @Component( role = MavenFileFilter.class, hint = "default" )
    private MavenFileFilter mavenFileFilter;

    @Component
    private MavenSession session;
    
    @Component
    private BuildContext buildContext;

    @Component
    private JspCompilerFactory jspCompilerFactory;
    
    // Sub-class must provide
    protected abstract List<String> getClasspathElements() throws MojoExecutionException;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }
        
        final Log log = this.getLog();
        
        final boolean isWar = "war".equals(project.getPackaging());

        if (!isWar) {
            return;
//            log.warn("Compiled JSPs will not be added to the project and web.xml will " +
//                     "not be modified because the project's packaging is not 'war'.");
        }
        if (!includeInProject) {
            log.warn("Compiled JSPs will not be added to the project and web.xml will " +
                     "not be modified because includeInProject is set to false.");
        }
        
        
        final JspCompiler jspCompiler = this.jspCompilerFactory.createJspCompiler();

        // Setup defaults (complex, can"t init from expression)
        if (sources == null) {
            sources = new FileSet();
            sources.setDirectory(this.defaultSourcesDirectory.getAbsolutePath());
            sources.setExcludes(Arrays.asList("WEB-INF/web.xml", "META-INF/**"));
        }

        jspCompiler.setWebappDirectory(sources.getDirectory());
        log.debug("Source directory: " + this.sources.getDirectory());
        
        jspCompiler.setOutputDirectory(this.workingDirectory);
        log.debug("Output directory: " + this.workingDirectory);
        
        jspCompiler.setEncoding(this.javaEncoding);
        log.debug("Encoding: " + this.javaEncoding);
        
        jspCompiler.setShowSuccess(this.showSuccess);
        
        jspCompiler.setListErrors(this.listErrors);
        
        jspCompiler.setWebFragmentFile(webFragmentFile);
        log.debug("Web Fragment: " + this.webFragmentFile);
        
        jspCompiler.setPackageName(packageName);
        log.debug("Package Name: " + this.packageName);

        
        final List<String> classpathElements = getClasspathElements();
        jspCompiler.setClasspath(classpathElements);
        log.debug("Classpath: " + classpathElements);

        //EL Interpreter Class
        if (eLInterpreterClass != null) {
            jspCompiler.setELInterpreterClass(eLInterpreterClass);
        }

        jspCompiler.setEnablePooling(enablePooling);

        jspCompiler.setGenStringAsCharArray(genStringAsCharArray);
        
        final List<File> jspFiles;
        if (sources.getIncludes() != null) {
            //Always need to get a full list of JSP files as incremental builds would result in an invalid web.xml
            final Scanner scanner = this.buildContext.newScanner(new File(sources.getDirectory()), true);
            scanner.setIncludes(sources.getIncludesArray());
            scanner.setExcludes(sources.getExcludesArray());
            scanner.addDefaultExcludes();
            
            scanner.scan();
            
            final String[] includes = scanner.getIncludedFiles();
            jspFiles = new ArrayList<File>(includes.length);
            for (final String it : includes) {
                jspFiles.add(new File(sources.getDirectory(), it));
            }
        }
        else {
            jspFiles = Collections.emptyList();
        }
        
        jspCompiler.setSmapDumped(smapDumped);
        jspCompiler.setSmapSuppressed(smapSuppressed);
        jspCompiler.setCompile(compile);
        jspCompiler.setValidateXml(validateXml);
        jspCompiler.setTrimSpaces(trimSpaces);
        jspCompiler.setVerbose(verbose);
        jspCompiler.setErrorOnUseBeanInvalidClassAttribute(errorOnUseBeanInvalidClassAttribute);
        jspCompiler.setCompilerSourceVM(source);
        jspCompiler.setCompilerTargetVM(target);

        
        // Make directories if needed
        workingDirectory.mkdirs();
        webFragmentFile.getParentFile().mkdirs();
        outputWebXml.getParentFile().mkdirs();
        
        // JspC needs URLClassLoader, with tools.jar
        final ClassLoader parent = Thread.currentThread().getContextClassLoader();
        final JspcMojoClassLoader cl = new JspcMojoClassLoader(parent);
        cl.addURL(findToolsJar());
        Thread.currentThread().setContextClassLoader(cl);

        try {
            // Show a nice message when we know how many files are included
            if (!jspFiles.isEmpty()) {
                log.info("Compiling " + jspFiles.size() + " JSP source file" + (jspFiles.size() > 1 ? "s" : "") + " to " + workingDirectory);
            }
            else {
                log.info("Compiling JSP source files to " + workingDirectory);
            }
            
            final StopWatch watch = new StopWatch();
            watch.start();
            
            jspCompiler.compile(jspFiles);
            
            log.info("Compilation completed in " + watch);
        }
        catch (Exception e) {
            throw new MojoFailureException("Failed to compile JSPS", e);
        }
        finally {
            // Set back the old classloader
            Thread.currentThread().setContextClassLoader(parent);
        }
        
        //Notify the build context that the jspFiles have been modified by the jsp compiler
        for (final File jspFile : jspFiles) {
            this.buildContext.refresh(jspFile);
        }
        
        // Maybe install the generated classes into the default output directory
        if (compile && isWar) {
            final Scanner scanner = buildContext.newScanner(this.workingDirectory);
            scanner.addDefaultExcludes();
            scanner.setIncludes(new String[] { "**/*.class" });
            scanner.scan();
            
            for (final String includedFile : scanner.getIncludedFiles()) {
                final File s = new File(this.workingDirectory, includedFile);
                final File d = new File(this.project.getBuild().getOutputDirectory(), includedFile);
                d.getParentFile().mkdirs();
                OutputStream fos = null;
                try {
                    fos = this.buildContext.newFileOutputStream(d);
                    org.apache.commons.io.FileUtils.copyFile(s, fos);
                }
                catch (IOException e) {
                    throw new MojoFailureException("Failed to copy '" + s + "' to '" + d + "'", e);
                }
                finally {
                    IOUtils.closeQuietly(fos);
                }
            }
        }
        
        if (isWar && includeInProject) {
            writeWebXml();
            project.addCompileSourceRoot(workingDirectory.toString());
        }
    }
    
    /**
     * Figure out where the tools.jar file lives.
     */
    private URL findToolsJar() throws MojoExecutionException {
        final File javaHome = FileUtils.resolveFile(new File(File.pathSeparator), System.getProperty("java.home"));
        
        final List<File> toolsPaths = new ArrayList<File>();
        
        File file = null;
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = FileUtils.resolveFile(javaHome, "../Classes/classes.jar");
            toolsPaths.add(file);
        }
        if (file == null || !file.exists()) {
            file = FileUtils.resolveFile(javaHome, "../lib/tools.jar");
            toolsPaths.add(file);
        }
        
        if (!file.exists()) {
            throw new MojoExecutionException("Could not find tools.jar at " + toolsPaths + " under java.home: " + javaHome);
        }
        getLog().debug("Using tools.jar: " + file);
        
        final URI fileUri = file.toURI();
        try {
            return fileUri.toURL();
        }
        catch (MalformedURLException e) {
            throw new MojoExecutionException("Could not generate URL from URI: " + fileUri, e);
        }
    }
    
    private void writeWebXml() throws MojoExecutionException {
        if (!inputWebXml.exists()) {
            throw new MojoExecutionException("web.xml does not exist at: " + inputWebXml);
        }
        if (!webFragmentFile.exists()) {
            throw new MojoExecutionException("web-fragment.xml does not exist at: " + webFragmentFile);
        }
        
        final String webXml = readXmlToString(inputWebXml);
        if (webXml.indexOf(injectString) == -1) {
            throw new MojoExecutionException("web.xml does not contain inject string '" + injectString + "' - " + webFragmentFile);
        }
        
        getLog().debug("Injecting " + webFragmentFile + " into " + inputWebXml + " and copying to " + outputWebXml);
        
        final String fragmentXml = readXmlToString(webFragmentFile);
        
        String output = StringUtils.replace(webXml, injectString, fragmentXml);
        
        // If using the default, then tack on the end of the document
        if (DEFAULT_INJECT_STRING.equals(injectString)) {
            output += DEFAULT_INJECT_STRING;
        }
        
        // Write the jsp web.xml file
        final File workingWebXml = new File(workingDirectory, "jspweb.xml");
        XmlStreamWriter xmlStreamWriter = null;
        try {
            xmlStreamWriter = new XmlStreamWriter(workingWebXml, this.javaEncoding);
            IOUtils.write(output, xmlStreamWriter);
        }
        catch (IOException e) {
            throw new MojoExecutionException("Failed to write '" + outputWebXml + "' as XML file with default encoding: " + this.javaEncoding, e);
        }
        finally {
            IOUtils.closeQuietly(xmlStreamWriter);
        }

        // Make sure parent dirs exist
        outputWebXml.getParentFile().mkdirs();
        
        // Copy the file into place filtering it on the way
        final MavenFileFilterRequest request = new MavenFileFilterRequest();
        request.setEncoding(this.javaEncoding);
        request.setMavenSession(this.session);
        request.setMavenProject(this.project);
        request.setFiltering(this.filtering);

        request.setFrom(workingWebXml);
        request.setTo(outputWebXml);

        try {
            this.mavenFileFilter.copyFile(request);
        }
        catch (MavenFilteringException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected String readXmlToString(File f) throws MojoExecutionException {
        Reader reader = null;
        try {
            reader = new XmlStreamReader(new BufferedInputStream(new FileInputStream(f)), true, this.javaEncoding);
            
            return IOUtils.toString(reader);
        }
        catch (IOException e) {
            throw new MojoExecutionException("Failed to read '" + f + "' as XML file with default encoding: " + this.javaEncoding, e);
        }
        finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
