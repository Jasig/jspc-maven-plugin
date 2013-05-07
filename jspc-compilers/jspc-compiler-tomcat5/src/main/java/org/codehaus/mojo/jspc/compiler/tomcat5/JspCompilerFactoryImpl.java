package org.codehaus.mojo.jspc.compiler.tomcat5;

import org.codehaus.mojo.jspc.compiler.JspCompiler;
import org.codehaus.mojo.jspc.compiler.JspCompilerFactory;
import org.codehaus.plexus.component.annotations.Component;

@Component(role=JspCompilerFactory.class, hint="tomcat5")
public class JspCompilerFactoryImpl implements JspCompilerFactory {

    public JspCompiler createJspCompiler() {
        return new JspCompilerImpl();
    }
}
