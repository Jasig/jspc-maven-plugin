package org.codehaus.mojo.jspc.compiler.tomcat7;

import org.codehaus.mojo.jspc.compiler.JspCompiler;
import org.codehaus.mojo.jspc.compiler.JspCompilerFactory;
import org.codehaus.plexus.component.annotations.Component;

@Component(role=JspCompilerFactory.class, hint="tomcat7")
public class JspCompilerFactoryImpl implements JspCompilerFactory {

    public JspCompiler createJspCompiler() {
        return new JspCompilerImpl();
    }
}
