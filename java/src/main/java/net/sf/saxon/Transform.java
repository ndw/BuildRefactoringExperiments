package net.sf.saxon;

import com.saxonica.PEConfiguration;

public class Transform {
    private final Configuration config;

    public Transform(Configuration config) {
        this.config = config;
    }

    public static void main(String[] args) {
        new Transform(new Configuration()).run();
    }

    public void run() {
        System.err.println("Ran net.sf.saxon.Transform with " + config.toString());
        //#if PE==true
        System.err.println("This is PE");
        //#endif
        //#if EE==true
        System.err.println("This is EE");
        //#endif
    }
}
