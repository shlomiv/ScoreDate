import java.io.File;
import java.io.IOException;
	
/**
 * Collection of utility methods for native code.
 */
public class NativeUtils {

    /**
     * Load a named library from a directory.<br>
     * Note: Loading of a JNI library should always be done in the corresponding
     * Java class or otherwise native methods may result in
     * {@link UnsatisfiedLinkError}s if different {@link ClassLoader}s are
     * involved.
     * 
     * @param directory
     *            directory the library is located in
     * @param name
     *            name of library
     */
    public static void load(File directory, String name)
                    throws UnsatisfiedLinkError {
            load(new File(directory, System.mapLibraryName(name)));
    }

    /**
     * Load a library from a file.
     * 
     * @param file
     *            the library file
     */
    public static void load(File file) throws UnsatisfiedLinkError {
            try {
                    System.load(file.getCanonicalPath());
            } catch (IOException ex) {
                    UnsatisfiedLinkError error = new UnsatisfiedLinkError();
                    error.initCause(ex);
                    throw error;
            }
    }

    public static boolean isWindows() {
            return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isMac() {
            return System.getProperty("os.name").toLowerCase().contains("mac");
    }
}