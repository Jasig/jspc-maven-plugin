package org.codehaus.mojo.jspc;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;


public class CompileMojoIT {
    @Test
    public void testMyMojo() throws VerificationException {
        File f = getClasspathFile("/jspc-1/pom.xml").getParentFile();
        
        Verifier verifier  = new Verifier(f.getAbsolutePath() );
        verifier.setLogFileName("verifier.log");
        verifier.setDebug(true);
        verifier.executeGoal( "package" );
    }

    protected File getClasspathFile(final String file) {
        final URL jspc1Pom = this.getClass().getResource(file);
        try {
          return new File(jspc1Pom.toURI());
        } catch(URISyntaxException e) {
            return new File(jspc1Pom.getPath());
        }
    }
}
