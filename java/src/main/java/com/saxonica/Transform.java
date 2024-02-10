package com.saxonica;

import net.sf.saxon.Configuration;

public class Transform extends net.sf.saxon.Transform {
    public Transform(Configuration config) {
        super(config);
    }

    public static void main(String[] args) {
        Configuration config;
        //#if EE==true
        config = new EEConfiguration();
        //#endif
        //#if PE==true
        config = new PEConfiguration();
        //#endif

        new net.sf.saxon.Transform(config).run();
    }

}
