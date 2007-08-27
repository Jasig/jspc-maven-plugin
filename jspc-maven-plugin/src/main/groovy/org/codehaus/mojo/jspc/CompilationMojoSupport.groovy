/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") you may not use this file except in compliance
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

package org.codehaus.mojo.jspc

import org.codehaus.mojo.groovy.GroovyMojoSupport

import org.apache.commons.lang.SystemUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.StopWatch

import org.apache.maven.project.MavenProject

import org.apache.maven.shared.model.fileset.FileSet
import org.apache.maven.shared.model.fileset.util.FileSetManager

import org.codehaus.plexus.util.InterpolationFilterReader

import org.codehaus.mojo.jspc.compiler.JspCompiler

/**
 * Support JSP compilation mojos.
 *
 * @version $Id$
 */
abstract class CompilationMojoSupport
    extends GroovyMojoSupport
{
    /**
     * The working directory to create the generated java source files.
     *
     * @parameter expression="${project.build.directory}/jsp-source"
     * @required
     */
    String workingDirectory
    
    /**
     * The sources of the webapp.  Default is <tt>${basedir}/src/main/webapp</tt>.
     *
     * @parameter
     */
    FileSet sources
    
    /**
     * The path and location to the web fragment file.
     *
     * @parameter expression="${project.build.directory}/web-fragment.xml"
     * @required
     */
    File webFragmentFile
    
    /**
     * The path and location of the original web.xml file.
     *
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF/web.xml"
     * @required
     */
    File inputWebXml
    
    /**
     * The final path and file name of the web.xml.
     *
     * @parameter expression="${project.build.directory}/jspweb.xml"
     * @required
     */
    File outputWebXml
    
    /**
     * Character encoding.
     *
     * @parameter
     */
    String javaEncoding

    //
    // TODO: Rename these, they are not descriptive enough
    //
    
    /**
     * Provide source compatibility with specified release.
     *
     * @parameter
     */
    String source

    /**
     * Generate class files for specific VM version.
     *
     * @parameter
     */
    String target

    /**
     * Sets if you want to compile the JSP classes.
     *
     * @parameter default-value="true"
     */
    boolean compile

    /**
     * Set this to false if you don't want to include the compiled JSPs
     * in your web.xml nor add the generated sources to your project's
     * source roots.
     *
     * @parameter default-value="true"
     */
    boolean includeInProject

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
     *
     * @parameter default-value="</web-app>"
     */
    String injectString
    
    private static final String DEFAULT_INJECT_STRING = "</web-app>"
    
    /**
     * The package in which the jsp files will be contained.
     *
     * @parameter default-value="jsp"
     */
    String packageName

    /**
     * Verbose level option for JcpC.
     *
     * @parameter default-value="0"
     */
    int verbose

    /**
     * Show Success option for JcpC.
     *
     * @parameter default-value="true"
     */
    boolean showSuccess

    /**
     * Set Smap Dumped option for JcpC.
     *
     * @parameter default-value="false"
     */
    boolean smapDumped

    /**
     * Set Smap Supressed option for JcpC.
     *
     * @parameter default-value="false"
     */
    boolean smapSupressed

    /**
     * List Errors option for JcpC.
     *
     * @parameter default-value="true"
     */
    boolean listErrors

    /**
     * Validate XML option for JcpC.
     *
     * @parameter default-value="false"
     */
    boolean validateXml

    /**
     * Removes the spaces from the generated JSP files.
     *
     * @parameter default-value="true"
     */
    boolean trimSpaces

    /**
     * Provides filtering of the generated web.xml text.
     *
     * @parameter default-value="true"
     */
    boolean filtering

    // Sub-class must provide
    protected abstract List getClasspathElements()
    
    //
    // Components
    //

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project

    /**
     * @component
     */
    private JspCompiler jspCompiler
    
    //
    // Mojo
    //

    void execute() {
        boolean isWar = "war" == project.packaging

        if (!isWar || !includeInProject) {
            log.warn("Compiled JSPs will not be added to the project and web.xml will " +
                     "not be modified, either because includeInProject is set to false or " +
                     "because the project's packaging is not 'war'.")
        }

        // Setup defaults (complex, can't init from expression)
        if (!sources) {
            sources = new FileSet()
            sources.directory = "${project.basedir}/src/main/webapp"
            sources.includes = null
        }
        
        if (log.debugEnabled) {
            log.debug("Source directory: $sources.directory")
            log.debug("Classpath: $classpathElements")
            log.debug("Output directory: $workingDirectory")
        }

        //
        // FIXME: Need to get rid of this and add a more generic way to configure the compiler
        //        perhaps nested configuration object for these details.  Only require the basics
        //        in mojo parameters that apply to all
        //
        
        def args = []
        
        args << '-uriroot'
        args << sources.directory
        
        args << '-d'
        args << workingDirectory
        
        if (javaEncoding != null) {
            args << '-javaEncoding'
            args << javaEncoding
        }
        
        if (showSuccess) {
            args << '-s'
        }
        
        if (listErrors) {
            args << '-l'
        }
        
        args << '-webinc'
        args << webFragmentFile
        
        args << '-p'
        args << packageName
        
        args << '-classpath'
        args << classpathElements.join(File.pathSeparator)

        def count
        if (sources.includes) {
            def fsm = new FileSetManager()
            sources.useDefaultExcludes = true
            def includes = fsm.getIncludedFiles(sources)
            count = includes.size()
                
            includes.each {
                args << new File(sources.directory, it)
            }
        }
        
        jspCompiler.args = args
        log.debug("Jscp args: $args")
        
        jspCompiler.smapDumped = smapDumped
        jspCompiler.smapSuppressed = smapSupressed
        jspCompiler.compile = compile
        jspCompiler.validateXml = validateXml
        jspCompiler.trimSpaces = trimSpaces
        jspCompiler.verbose = verbose
        jspCompiler.compilerSourceVM = source
        jspCompiler.compilerTargetVM = target
        
        // Make directories if needed
        ant.mkdir(dir: workingDirectory)
        ant.mkdir(dir: project.build.directory)
        ant.mkdir(dir: project.build.outputDirectory)
        
        // JspC needs URLClassLoader, with tools.jar
        def parent = Thread.currentThread().contextClassLoader
        def cl = new JspcMojoClassLoader(parent)
        cl.addURL(toolsJar.toURI().toURL())
        Thread.currentThread().setContextClassLoader(cl)

        try {
            // Show a nice message when we know how many files are included
            if (count) {
                log.info("Compiling $count JSP source file" + (count > 1 ? 's' : '') + " to $workingDirectory")
            }
            else {
                log.info("Compiling JSP source files to $workingDirectory")
            }
            
            def watch = new StopWatch()
            watch.start()
            
            jspCompiler.compile()
            
            log.info("Compiled completed in $watch")
        }
        finally {
            // Set back the old classloader
            Thread.currentThread().contextClassLoader = parent
        }
        
        // Maybe install the generated classes into the default output directory
        if (compile && isWar) {
            ant.copy(todir: project.build.outputDirectory) {
                fileset(dir: workingDirectory) {
                    include(name: '**/*.class')
                }
            }
        }
        
        if (isWar && includeInProject) {
            writeWebXml()
            project.addCompileSourceRoot(workingDirectory)
        }
    }
    
    /**
     * Figure out where the tools.jar file lives.
     */
    private File getToolsJar() {
        def javaHome = new File(System.properties['java.home'])
        
        def file
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(javaHome, '../Classes/classes.jar').canonicalFile
        }
        else {
            file = new File(javaHome, '../lib/tools.jar').canonicalFile
        }
        
        assert file.exists() : "Missing tools.jar at: $file"
        
        log.debug("Using tools.jar: $file")
        
        return file
    }
    
    private void writeWebXml() {
        // Read the files
        assert inputWebXml.exists()
        String webXml = inputWebXml.text
        
        assert webFragmentFile.exists()
        String fragmentXml = webFragmentFile.text
        
        if (webXml.indexOf(injectString) == -1) {
            fail("Missing inject string: '$injectString' in: $inputWebXml")
        }
        
        def output = StringUtils.replace(webXml, injectString, fragmentXml)
        
        // If using the default, then tack on the end of the document
        if (injectString == DEFAULT_INJECT_STRING) {
            output += DEFAULT_INJECT_STRING
        }
        
        // Allow generated xml to be filtered
        if (filtering) {
            output = filter(output)
        }

        // Write the file
        outputWebXml.parentFile.mkdirs()
        outputWebXml.write(output)
    }

    private String filter(String input) {
        assert input
        
        def reader = new StringReader(input)
        
        // Setup chained readers to filter
        reader = new InterpolationFilterReader(reader, project.properties, '${', '}')
        reader = new InterpolationFilterReader(reader, project.properties, '@', '@')
        
        return reader.readLines().join('\n')
    }
}
