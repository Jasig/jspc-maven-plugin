package org.codehaus.mojo.jspc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.XmlStreamReader;
import org.apache.commons.io.output.XmlStreamWriter;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.mojo.jspc.compiler.JspCompiler;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.StringUtils;

abstract class CompilationMojoSupport extends AbstractMojo {
    /**
     * The working directory to create the generated java source files.
     *
     * @parameter expression="${project.build.directory}/jsp-source"
     * @required
     */
    String workingDirectory;
    
    /**
     * The sources of the webapp.  Default is <tt>${basedir}/src/main/webapp</tt>.
     *
     * @parameter
     */
    FileSet sources;
    
    /**
     * The path and location to the web fragment file.
     *
     * @parameter expression="${project.build.directory}/web-fragment.xml"
     * @required
     */
    File webFragmentFile;
    
    /**
     * The path and location of the original web.xml file.
     *
     * @parameter expression="${basedir}/src/main/webapp/WEB-INF/web.xml"
     * @required
     */
    File inputWebXml;
    
    /**
     * The final path and file name of the web.xml.
     *
     * @parameter expression="${project.build.directory}/jspweb.xml"
     * @required
     */
    File outputWebXml;
    
    /**
     * Character encoding.
     *
     * @parameter
     */
    String javaEncoding;

    //
    // TODO: Rename these, they are not descriptive enough
    //
    
    /**
     * Provide source compatibility with specified release.
     *
     * @parameter
     */
    String source;

    /**
     * Generate class files for specific VM version.
     *
     * @parameter
     */
    String target;

    /**
     * Sets if you want to compile the JSP classes.
     *
     * @parameter default-value="true"
     */
    boolean compile;

    /**
     * Set this to false if you don"t want to include the compiled JSPs
     * in your web.xml nor add the generated sources to your project"s
     * source roots.
     *
     * @parameter default-value="true"
     */
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
     *
     * @parameter default-value="</web-app>"
     */
    String injectString;
    
    private static final String DEFAULT_INJECT_STRING = "</web-app>";
    
    /**
     * The package in which the jsp files will be contained.
     *
     * @parameter default-value="jsp"
     */
    String packageName;

    /**
     * Verbose level option for JspC.
     *
     * @parameter default-value="0"
     */
    int verbose;

    /**
     * Show Success option for JspC.
     *
     * @parameter default-value="true"
     */
    boolean showSuccess;

    /**
     * Set Smap Dumped option for JspC.
     *
     * @parameter default-value="false"
     */
    boolean smapDumped;

    /**
     * Set Smap Suppressed option for JspC.
     *
     * @parameter default-value="false"
     */
    boolean smapSuppressed;

    /**
     * List Errors option for JspC.
     *
     * @parameter default-value="true"
     */
    boolean listErrors;

    /**
     * Validate XML option for JspC.
     *
     * @parameter default-value="false"
     */
    boolean validateXml;

    /**
     * Removes the spaces from the generated JSP files.
     *
     * @parameter default-value="true"
     */
    boolean trimSpaces;

    /**
     * Provides filtering of the generated web.xml text.
     *
     * @parameter default-value="true"
     */
    boolean filtering;

    /**
     * Set to {@code true} to disable the plugin.
     *
     * @parameter expression="${jspc.skip}" default-value="false"
     */
    boolean skip;

    /**
     * Issue an error when the value of the class attribute in a useBean action is
     * not a valid bean class
     *
     * @parameter default-value="true"
     */
    boolean errorOnUseBeanInvalidClassAttribute;
    
    //
    // Components
    //

    /**
     * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
     */
    private String encoding;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private JspCompiler jspCompiler;
    
    // Sub-class must provide
    protected abstract List getClasspathElements();

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }
        
        final Log log = this.getLog();
        
        boolean isWar = "war" == project.getPackaging();

        if (!isWar || !includeInProject) {
            log.warn("Compiled JSPs will not be added to the project and web.xml will " +
                     "not be modified, either because includeInProject is set to false or " +
                     "because the project's packaging is not 'war'.");
        }

        // Setup defaults (complex, can"t init from expression)
        //TODO
        if (sources == null) {
            sources = new FileSet();
            sources.setDirectory("${project.basedir}/src/main/webapp");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Source directory: $sources.directory");
            log.debug("Classpath: $classpathElements");
            log.debug("Output directory: $workingDirectory");
        }

        //
        // FIXME: Need to get rid of this and add a more generic way to configure the compiler
        //        perhaps nested configuration object for these details.  Only require the basics
        //        in mojo parameters that apply to all
        //
        
        List<String> args = new ArrayList<String>();
        
        args.add("-uriroot");
        args.add(sources.getDirectory());
        
        args.add("-d");
        args.add(workingDirectory);
        
        if (javaEncoding != null) {
            args.add("-javaEncoding");
            args.add(javaEncoding);
        }
        
        if (showSuccess) {
            args.add("-s");
        }
        
        if (listErrors) {
            args.add("-l");
        }
        
        args.add("-webinc");
        args.add(webFragmentFile.getAbsolutePath());
        
        args.add("-p");
        args.add(packageName);
        
        args.add("-classpath");
        args.add(StringUtils.join(getClasspathElements().iterator(), File.pathSeparator));

        int count = 0;
        if (sources.getIncludes() != null) {
            final FileSetManager fsm = new FileSetManager();
            sources.setUseDefaultExcludes(true);
            final String[] includes = fsm.getIncludedFiles(sources);
            count = includes.length;
                
            for (final String it : includes) {
                args.add(new File(sources.getDirectory(), it).toString());
            }
        }
        
        jspCompiler.setArgs(args.toArray(new String[0]));
        log.debug("Jspc args: " + args);
        
        jspCompiler.setSmapDumped(smapDumped);
        jspCompiler.setSmapSuppressed(smapSuppressed);
        jspCompiler.setCompile(compile);
        jspCompiler.setValidateXml(validateXml);
        jspCompiler.setTrimSpaces(trimSpaces);
        jspCompiler.setVerbose(verbose);
        jspCompiler.setErrorOnUseBeanInvalidClassAttribute(errorOnUseBeanInvalidClassAttribute);
        jspCompiler.setCompilerSourceVM(source);
        jspCompiler.setCompilerTargetVM(target);
        
        //TODO
//        // Make directories if needed
//        ant.mkdir(dir: workingDirectory)
//        ant.mkdir(dir: project.build.directory)
//        ant.mkdir(dir: project.build.outputDirectory)
        
        // JspC needs URLClassLoader, with tools.jar
        final ClassLoader parent = Thread.currentThread().getContextClassLoader();
        final JspcMojoClassLoader cl = new JspcMojoClassLoader(parent);
        cl.addURL(findToolsJar());
        Thread.currentThread().setContextClassLoader(cl);

        try {
            // Show a nice message when we know how many files are included
            if (count > 0) {
                log.info("Compiling " + count + " JSP source file" + (count > 1 ? "s" : "") + " to " + workingDirectory);
            }
            else {
                log.info("Compiling JSP source files to " + workingDirectory);
            }
            
            final StopWatch watch = new StopWatch();
            watch.start();
            
            jspCompiler.compile();
            
            log.info("Compilation completed in " + watch);
        }
        catch (Exception e) {
            throw new MojoFailureException("Failed to compile JSPS", e);
        }
        finally {
            // Set back the old classloader
            Thread.currentThread().setContextClassLoader(parent);
        }
        
        // Maybe install the generated classes into the default output directory
        if (compile && isWar) {
            final FileSet fs = new FileSet();
            fs.setDirectory(workingDirectory);
            //TODO
//            ant.copy(todir: project.build.outputDirectory) {
//                fileset(dir: workingDirectory) {
//                    include(name: "**/*.class")
//                }
//            }
        }
        
        if (isWar && includeInProject) {
            writeWebXml();
            project.addCompileSourceRoot(workingDirectory);
        }
    }
    
    /**
     * Figure out where the tools.jar file lives.
     */
    private URL findToolsJar() throws MojoExecutionException {
        final File javaHome = FileUtils.resolveFile(new File(File.pathSeparator), System.getProperty("java.home"));
        
        final File file;
        if (SystemUtils.IS_OS_MAC_OSX) {
            file = FileUtils.resolveFile(javaHome, "../Classes/classes.jar");
        }
        else {
            file = FileUtils.resolveFile(javaHome, "../lib/tools.jar");
        }
        
        if (!file.exists()) {
            throw new MojoExecutionException("Could not find tools.jar at '" + file + "' under java.home: " + javaHome);
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
        
        final String webXml = readXmlToString(inputWebXml, filtering);
        if (webXml.indexOf(injectString) == -1) {
            throw new MojoExecutionException("web.xml does not contain inject string '" + injectString + "' - " + webFragmentFile);
        }
        
        final String fragmentXml = readXmlToString(webFragmentFile, filtering);
        
        String output = StringUtils.replace(webXml, injectString, fragmentXml);
        
        // If using the default, then tack on the end of the document
        if (DEFAULT_INJECT_STRING.equals(injectString)) {
            output += DEFAULT_INJECT_STRING;
        }

        // Make sure parent dirs exist
        outputWebXml.getParentFile().mkdirs();
        
        // Write the file
        XmlStreamWriter xmlStreamWriter = null;
        try {
            xmlStreamWriter = new XmlStreamWriter(outputWebXml, this.encoding);
            IOUtils.write(output, xmlStreamWriter);
        }
        catch (IOException e) {
            throw new MojoExecutionException("Failed to write '" + outputWebXml + "' as XML file with default encoding: " + this.encoding, e);
        }
        finally {
            IOUtils.closeQuietly(xmlStreamWriter);
        }
    }

    protected String readXmlToString(File f, boolean filtering) throws MojoExecutionException {
        Reader reader = null;
        try {
            reader = new XmlStreamReader(new BufferedInputStream(new FileInputStream(f)), true, this.encoding);
            
            if (filtering) {
                final Properties properties = project.getProperties();
                reader = new InterpolationFilterReader(reader, properties, "${", "}");
                reader = new InterpolationFilterReader(reader, properties, "@", "@");
            }
            
            return IOUtils.toString(reader);
        }
        catch (IOException e) {
            throw new MojoExecutionException("Failed to read '" + f + "' as XML file with default encoding: " + this.encoding, e);
        }
        finally {
            IOUtils.closeQuietly(reader);
        }
    }
}
