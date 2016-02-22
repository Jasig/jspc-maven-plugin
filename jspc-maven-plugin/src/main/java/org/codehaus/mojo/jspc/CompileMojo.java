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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

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

/**
 * Compile JSPs.
 *
 * @version $Id$
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PROCESS_CLASSES, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CompileMojo extends CompilationMojoSupport {
    /**
     * Project classpath.
     */
    @Parameter(defaultValue="${project.compileClasspathElements}", required=true)
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
        addBuildOutputDirectoryTo(list);
        addWebAppOutputDirectoryTo(list); // FIXME Workaround: Exploded .war folder until Tomcat 7 can be used
        return list;
    }

    private void addBuildOutputDirectoryTo(List<String> list) {
        // If output directory contained .TLD files it wasn't added before. This is verified and done here if necessary.
        if (classpathElements.contains(project.getBuild().getOutputDirectory()) && !list.contains(project.getBuild().getOutputDirectory())) {
            list.add(project.getBuild().getOutputDirectory());
        }
    }

    private void addWebAppOutputDirectoryTo(List<String> list) {
        if (!list.contains(project.getBuild().getOutputDirectory())) {
            list.add(project.getBuild().getOutputDirectory());
        }
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
                // normalize path as the JspCompiler expects '/' as separator
                name = name.replace('\\', '/');
                JarEntry jarFile = new JarEntry(name);
                jos.putNextEntry(jarFile);

                FileUtils.copyFile(file, jos);
            }
        } finally {
            IOUtils.closeQuietly(jos);
        }
    }
}
