import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;

public class CommandExecuter{
    public static void ExecuteCommand(String command) {
        try {
            log(command);
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while (true) {
                line = in.readLine();
                if (line == null) { break; }
                System.out.println(line);
            }

            BufferedReader errors = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            line = null;
            while (true) {
                line = errors.readLine();
                if (line == null) { break; }
                System.out.println(line);
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //private static SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss:SSS");

    private static void log(String message) {
        System.out.println(message);
    }
}
