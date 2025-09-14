import javax.tools.*;
/*
class InMemoryJavaFile extends SimpleJavaFileObject {
    private final String code;

    protected InMemoryJavaFile(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
              Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}*/


public class Compiler{
    class CompilerResult{ // todo
        int errors;
        int warnings;
        boolean success;
    }

    public static boolean CopileCMD(String file){
        String command = "javac " + file;

        CommandExecuter.ExecuteCommand(command);
        return true;
    }

/*
    public static boolean CompileAPI(ArrayList<String> files){
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);


        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);

        InMemoryJavaFile mem_file = new InMemoryJavaFile(name, code);


        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        boolean success = compiler.getTask(
            null, 
            standardFileManager, 
            diagnostics, 
            null, 
            null, 
            compilationUnits
        ).call();
    }*/

    public static void Run(String file){
        int subStrPos = Math.max(file.lastIndexOf('/'), file.lastIndexOf('\\'));
        CopileCMD(file);

        String fileName = file.substring(subStrPos + 1);
        String path = file.substring(0, subStrPos + 1);
        String command = "java -classpath " + path + " " + fileName.substring(0, fileName.lastIndexOf('.'));

        CommandExecuter.ExecuteCommand(command);
    }
}
