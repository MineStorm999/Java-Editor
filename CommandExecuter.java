import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.Scanner;

public class CommandExecuter{
    public static int ExecuteCommand(String command) { // returns error count
        int error = 0;
        try {
            Log.Message(command);
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while (true) {
                line = in.readLine();
                if (line == null) { break; }
                Log.Message(line);
            }

            BufferedReader errors = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            line = null;
            while (true) {
                line = errors.readLine();
                if (line == null) { break; }
                Log.Error(line);
                error++;
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            error++;
        }
        return error;
    }
}
