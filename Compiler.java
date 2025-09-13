public class Compiler{
    class CompilerResult{ // todo
        int errors;
        int warnings;
        boolean success;
    }

    public static void Compile(String file){
        String command = "javac " + file;
        System.out.println("Run command: " + command);

        CommandExecuter.ExecuteCommand(command);
    }

    public static void Run(String file){
        int subStrPos = Math.max(file.lastIndexOf('/'), file.lastIndexOf('\\'));
        Compile(file);

        String fileName = file.substring(subStrPos + 1);
        String path = file.substring(0, subStrPos + 1);
        String command = "java -classpath " + path + " " + fileName.substring(0, fileName.lastIndexOf('.'));
        System.out.println("Run command: " + command);

        CommandExecuter.ExecuteCommand(command);
    }
}
