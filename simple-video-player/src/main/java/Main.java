import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

public class Main {
    public static void main(String[] args) {
        // 加载dll
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "vlc");

        @SuppressWarnings("unused")
        Window frame = new Window();

       // System.exit(0);

    }

}
