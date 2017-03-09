package ch.hsr.maloney.util;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;


public class CustomClassLoader extends URLClassLoader {
    public CustomClassLoader(URLClassLoader classLoader) {
        super(classLoader.getURLs());
    }

    // Make method public
    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}
