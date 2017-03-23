package ch.hsr.maloney.util;
import java.io.File;
import java.net.MalformedURLException;
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

    public static CustomClassLoader createPluginLoader() throws MalformedURLException {
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        CustomClassLoader myClassLoader = new CustomClassLoader(urlClassLoader);

        // Extracts the plugins folder which is a sibling of the application folder (like libs)
        String path = CustomClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File pluginFolder = new File(path).getParentFile().toPath().resolveSibling("plugins").toFile();
        File[] jars = pluginFolder.listFiles((dir, filename) -> filename.endsWith(".jar"));
        if (jars != null) {
            for (File jar : jars) {
                myClassLoader.addURL(jar.toURI().toURL());
            }
        }
        return myClassLoader;
    }
}
