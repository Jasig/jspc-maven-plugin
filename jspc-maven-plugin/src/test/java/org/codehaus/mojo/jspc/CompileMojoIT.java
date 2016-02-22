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
