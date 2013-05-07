package org.codehaus.mojo.jspc;

import java.io.File;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;


public class CompileMojoIT {
    @Test
    public void testJspc5() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/jspc-5" );
        
        testJspc(testDir);
    }
    
    @Test
    public void testJspc6() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/jspc-6" );
        
        testJspc(testDir);
    }
    
    @Test
    public void testJspc7() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/jspc-7" );
        
        testJspc(testDir);
    }

    protected void testJspc(File testDir) throws VerificationException {
        Verifier verifier  = new Verifier(testDir.getAbsolutePath() );
        verifier.setLogFileName("verifier.log");
        
//        verifier.setDebug(true);
//        verifier.setMavenDebug(true);
        
        verifier.executeGoal("clean");
        verifier.executeGoal("package");
        
        verifier.verifyErrorFreeLog();
        
        verifier.assertFilePresent("target/jspweb.xml");
        verifier.assertFilePresent("target/classes/jsp/index_jsp.class");
    }
}
