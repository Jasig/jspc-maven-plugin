package org.codehaus.mojo.jspc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Compile JSPs.
 *
 * @version $Id$
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PROCESS_CLASSES, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CompileMojo extends CompilationMojoSupport {
    /**
     * Project classpath.
     *
     * @parameter expression="${project.compileClasspathElements}"
     * @required
     */
    private List<String> classpathElements;

    protected List<String> getClasspathElements() throws MojoExecutionException {
        List<String> list = new ArrayList<String>(classpathElements.size());
        boolean tldExists = false;
        String[] tlds = new String[] { "tld" };
        File tempJarDir;
        try {
            tempJarDir = File.createTempFile("jscp-", "");
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create jscp temp dir", e);
        }

        try {
            tempJarDir.delete();
            tempJarDir.mkdir();
            
            for (final String target : classpathElements) {
                File file = new File(target);
                if (file.isFile()) {
                    list.add(target);
                }
                else if (file.isDirectory()) {
                    Collection<File> tldFiles = FileUtils.listFiles(file, tlds, true);
                    if (!tldFiles.isEmpty()) {
                        try {
                            FileUtils.copyDirectory(file, tempJarDir);
                        } catch (IOException e) {
                            throw new MojoExecutionException("Failed copy '" + file + "' to '" + tempJarDir + "'", e);
                        }
                        tldExists = true;
                    }
                    //Fix for https://jira.codehaus.org/browse/MJSPC-60
                    else {
                        list.add(target);
                    }
                }
            }

            if (getLog().isDebugEnabled()) {
                getLog().debug("tldExists: " + tldExists);
            }

            if (tldExists) {
                File tempJarFile;
                try {
                    tempJarFile = File.createTempFile("jscptld-", ".jar");
                } catch (IOException e) {
                    throw new MojoExecutionException("Failed to create jscptld temp file", e);
                }
                tempJarFile.deleteOnExit();
                try {
                    createJarArchive(tempJarFile, tempJarDir);
                } catch (IOException e) {
                    throw new MojoExecutionException("Failed create jar '" + tempJarFile + "' from '" + tempJarFile + "'", e);
                }
                list.add(tempJarFile.getAbsolutePath());
            }
        }
        finally {
            FileUtils.deleteQuietly(tempJarDir);
        }
        return list;
    }

    protected void createJarArchive(File archiveFile, File tempJarDir) throws IOException {
        JarOutputStream jos = null;
        try {
            jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(archiveFile)), new Manifest());
    
            int pathLength = tempJarDir.getAbsolutePath().length() + 1;
            Collection<File> files = FileUtils.listFiles(tempJarDir, null, true);
            for (final File file : files) {
                if (!file.isFile()){
                    continue;
                }
    
                if(getLog().isDebugEnabled()) {
                    getLog().debug("file: " + file.getAbsolutePath());
                }
                
                // Add entry
                String name = file.getAbsolutePath().substring(pathLength);
                JarEntry jarFile = new JarEntry(name);
                jos.putNextEntry(jarFile);
    
                FileUtils.copyFile(file, jos);
            }
        } finally {
            IOUtils.closeQuietly(jos);
        }
    }
}
