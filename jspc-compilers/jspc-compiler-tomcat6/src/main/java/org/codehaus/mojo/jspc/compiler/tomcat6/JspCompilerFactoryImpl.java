package org.codehaus.mojo.jspc.compiler.tomcat6;

import org.codehaus.mojo.jspc.compiler.JspCompiler;
import org.codehaus.mojo.jspc.compiler.JspCompilerFactory;
import org.codehaus.plexus.component.annotations.Component;

@Component(role=JspCompilerFactory.class, hint="tomcat6")
public class JspCompilerFactoryImpl implements JspCompilerFactory {

    public JspCompiler createJspCompiler() {
        return new JspCompilerImpl();
    }
}
