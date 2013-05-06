package org.codehaus.mojo.jspc;

public class CompileMojo extends CompilationMojoSupport {
/**
 * Project classpath.
 *
 * @parameter expression="${project.compileClasspathElements}"
 * @required
 */
private List classpathElements

protected List getClasspathElements() {
    def List list = new ArrayList()
    def boolean tldExists = false
    def String[] tlds = ["tld"]
    def File tempJarDir = File.createTempFile("jscp-", "")
    tempJarDir.delete()
    tempJarDir.mkdir()
    
    try{
        for (target in classpathElements){
            File file = new File(target)
            if(file.isFile()){
                list << target
            } else if(file.isDirectory()){
                Collection tldFiles = FileUtils.listFiles(file, tlds, true)
                if(!tldFiles.isEmpty()){
                    FileUtils.copyDirectory(file, tempJarDir)
                    tldExists = true
                }
            }
        }

        if(log.debugEnabled) {
            log.debug("tldExists: ${tldExists}")
        }

        if(tldExists){
            def File tempJarFile = File.createTempFile("jscptld-", ".jar")
            tempJarFile.deleteOnExit()
            createJarArchive(tempJarFile, tempJarDir)
            list << tempJarFile
        }
    } finally {
        FileUtils.deleteDirectory(tempJarDir)
    }
    return list
}

protected void createJarArchive(File archiveFile, File tempJarDir) {
    JarOutputStream jos = null;
    try {
        jos = new JarOutputStream(new FileOutputStream(archiveFile), new Manifest());

        def pathLength = tempJarDir.getAbsolutePath().length() + 1
        def Collection files = FileUtils.listFiles(tempJarDir, null, true)
        for (int i = 0; i < files.size; i++) {
            if (!files[i].isFile()){
                continue
            }

            if(log.debugEnabled) {
                log.debug("file: " + files[i].getAbsolutePath())
            }
            
            // Add entry
            def name = files[i].getAbsolutePath().substring(pathLength)
            JarEntry jarFile = new JarEntry(name);
            jos.putNextEntry(jarFile);

            FileInputStream fis = null
            try {
                fis = new FileInputStream(files[i]);
                IOUtils.copy(fis, jos)
            } finally {
                IOUtils.closeQuietly(fis)
            }
        }
    } finally {
        IOUtils.closeQuietly(jos)
    }
}

}
