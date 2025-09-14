import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;


public final class TextEdit extends JFrame implements ActionListener{
    class FileInfo{
        boolean saved;
        String path;
        String name;

        public FileInfo(boolean s, String pt, String nm){
            saved = s;
            path = pt;
            name = nm; 
        }
    }

    private static Container layout;
    private static JTabbedPane tabs;
    private static JFrame frame;
    private static int returnValue = 0;

    private static String file_path;

    public static ArrayList<FileInfo> m_files = new ArrayList<FileInfo>();

    public boolean Saved(int i){return m_files.get(i).saved;};

    class SyntaxHighlighter {
        public static void SetTextColor(StyledDocument doc, int start, int length, Color color) {
            Style style = doc.addStyle("TextColor", null);
            StyleConstants.setForeground(style, color);
            StyleConstants.setBackground(style, Color.GRAY);
            doc.setCharacterAttributes(start, length, style, false);
        }
    }

    class JavaSyntaxHighlighter {
        private static boolean ValidKeyWord(String text, int start, int end){
            char[] valid_spaces = {' ', '\n', ';', ',', ':', '.', '+', '-', '*', '/', '(', ')', '{', '}', '[', ']', '!', '=', '<', '>', '|', '&'};
            int space_count = 22; // TODO: dynamic
            boolean a = false;
            boolean b = false;

            if(start > 0){
                for (int i = 0; i < space_count; i++){
                    if(text.charAt(start - 1) == valid_spaces[i]){
                        a = true;
                        break;
                    }
                }
            }else{
                a = true;
            }

            if(end > 0){
                for (int i = 0; i < space_count; i++){
                    if(text.charAt(end) == valid_spaces[i]){
                        b = true;
                        break;
                    }
                }
            }else{
                b = true;
            }
            return a && b;
        }

        public static void Highlight(JTextPane textPane, StyledDocument doc) {
            // Define syntax highlighting styles for Java
            Style defaultStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
            Style keywordStyle = textPane.addStyle("KeywordStyle", defaultStyle);
            StyleConstants.setForeground(keywordStyle, Color.BLUE);

            

            // Apply styles to keywords
            String[] keywords = {
                "abstract", "boolean", "break", "class", "extends", "for", "if", "new", "return",
                "while", "public", "private", "static", "void", "int", "double", "import", "@Override"
            };
            String text = textPane.getText();
            SyntaxHighlighter.SetTextColor(doc, 0, text.length(), Color.BLACK); // TODO multible styles (light/dark)


            for (String keyword : keywords) {
                int pos = 0;

                while ((pos = text.indexOf(keyword, pos)) >= 0) {
                    if(ValidKeyWord(text, pos, keyword.length() + pos)){
                        SyntaxHighlighter.SetTextColor(doc, pos, keyword.length(), Color.BLUE);
                    }
                    pos += keyword.length();
                }
            }


            // strings
            int pos = 0;
            int start = 0;
            boolean flipFlop = false;
            while ((pos = text.indexOf("\"", pos)) >= 0) {
                if(flipFlop){
                    flipFlop = false;
                    SyntaxHighlighter.SetTextColor(doc, start, (pos - start) + 1, Color.WHITE);
                }else{
                    flipFlop = true;
                    start = pos;
                }
                pos += 1;
            }


            //   chars
            pos = 0;
            start = 0;
            flipFlop = false;
            while ((pos = text.indexOf("\'", pos)) >= 0) {
                if(flipFlop){
                    flipFlop = false;
                    SyntaxHighlighter.SetTextColor(doc, start, (pos - start) + 1, Color.WHITE);
                }else{
                    flipFlop = true;
                    start = pos;
                }
                pos += 1;
            }


            // brackets
            //   ()
            pos = 0;
            start = 0;
            flipFlop = false;

            ArrayList<Integer> openBrackets = new ArrayList<Integer>();
            ArrayList<Integer> closedBrackets = new ArrayList<Integer>();

            ArrayList<Boolean> openBracketsWrong = new ArrayList<Boolean>();
            ArrayList<Boolean> closedBracketsWrong = new ArrayList<Boolean>();

            while ((pos = text.indexOf("(", pos)) >= 0) {
                System.out.println(pos);
                openBrackets.add(pos);
                openBracketsWrong.add(true);
                pos += 1;
            }
            pos = 0;
            while ((pos = text.indexOf(")", pos)) >= 0) {
                System.out.println(pos);
                closedBrackets.add(pos);
                closedBracketsWrong.add(true);
                pos += 1;
            }
            for(int i = 0; i < closedBrackets.size(); i++){
                for(int x = 1; x < openBrackets.size(); x++){
                    if((openBrackets.get(x) < closedBrackets.get(i)) && x < (closedBrackets.size() - 1)){
                        continue;
                    }
                    x--;
                    if(openBrackets.get(x) > closedBrackets.get(i)){
                        break;
                    }
                    
                    while(!openBracketsWrong.get(x) && x > 0){
                        x--;
                    };

                    if(!openBracketsWrong.get(x)){
                        break;
                    }
                    openBracketsWrong.set(x, false);
                    closedBracketsWrong.set(i, false);
                    break;
                }
            }
            // mark unclosed brackets
            for (int i = 0; i < closedBracketsWrong.size(); i++){
                if(closedBracketsWrong.get(i)){
                    SyntaxHighlighter.SetTextColor(doc, closedBrackets.get(i), 1, Color.RED);
                }else{
                    SyntaxHighlighter.SetTextColor(doc, closedBrackets.get(i), 1, Color.YELLOW);
                }
            }
            for (int i = 0; i < openBracketsWrong.size(); i++){
                if(openBracketsWrong.get(i)){
                    SyntaxHighlighter.SetTextColor(doc, openBrackets.get(i), 1, Color.RED);
                }else{
                    SyntaxHighlighter.SetTextColor(doc, openBrackets.get(i), 1, Color.YELLOW);
                }
            }

            //   {}
            pos = 0;
            start = 0;
            flipFlop = false;

            openBrackets = new ArrayList<Integer>();
            closedBrackets = new ArrayList<Integer>();

            openBracketsWrong = new ArrayList<Boolean>();
            closedBracketsWrong = new ArrayList<Boolean>();

            while ((pos = text.indexOf("{", pos)) >= 0) {
                openBrackets.add(pos);
                openBracketsWrong.add(true);
                pos += 1;
            }
            pos = 0;
            while ((pos = text.indexOf("}", pos)) >= 0) {
                closedBrackets.add(pos);
                closedBracketsWrong.add(true);
                pos += 1;
            }
            for(int i = 0; i < closedBrackets.size(); i++){
                for(int x = 1; x < openBrackets.size(); x++){
                    if((openBrackets.get(x) < closedBrackets.get(i)) && x < (closedBrackets.size() - 1)){
                        continue;
                    }
                    x--;
                    if(openBrackets.get(x) > closedBrackets.get(i)){
                        break;
                    }
                    
                    while(!openBracketsWrong.get(x) && x > 0){
                        x--;
                    };

                    if(!openBracketsWrong.get(x)){
                        break;
                    }
                    openBracketsWrong.set(x, false);
                    closedBracketsWrong.set(i, false);

                    break;
                }
            }
            // mark unclosed brackets
            for (int i = 0; i < closedBracketsWrong.size(); i++){
                if(closedBracketsWrong.get(i)){
                    SyntaxHighlighter.SetTextColor(doc, closedBrackets.get(i), 1, Color.RED);
                }else{
                    SyntaxHighlighter.SetTextColor(doc, closedBrackets.get(i), 1, Color.YELLOW);
                }
            }
            for (int i = 0; i < openBracketsWrong.size(); i++){
                if(openBracketsWrong.get(i)){
                    SyntaxHighlighter.SetTextColor(doc, openBrackets.get(i), 1, Color.RED);
                }else{
                    SyntaxHighlighter.SetTextColor(doc, openBrackets.get(i), 1, Color.YELLOW);
                }
            }

            //   []
            pos = 0;
            start = 0;
            flipFlop = false;

            openBrackets = new ArrayList<Integer>();
            closedBrackets = new ArrayList<Integer>();

            openBracketsWrong = new ArrayList<Boolean>();
            closedBracketsWrong = new ArrayList<Boolean>();

            while ((pos = text.indexOf("[", pos)) >= 0) {
                openBrackets.add(pos);
                openBracketsWrong.add(true);
                pos += 1;
            }
            pos = 0;
            while ((pos = text.indexOf("]", pos)) >= 0) {
                closedBrackets.add(pos);
                closedBracketsWrong.add(true);
                pos += 1;
            }
            for(int i = 0; i < closedBrackets.size(); i++){
                for(int x = 1; x < openBrackets.size(); x++){
                    if((openBrackets.get(x) < closedBrackets.get(i)) && x < (closedBrackets.size() - 1)){
                        continue;
                    }
                    x--;
                    if(openBrackets.get(x) > closedBrackets.get(i)){
                        break;
                    }
                    
                    while(!openBracketsWrong.get(x) && x > 0){
                        x--;
                    };

                    if(!openBracketsWrong.get(x)){
                        break;
                    }
                    openBracketsWrong.set(x, false);
                    closedBracketsWrong.set(i, false);

                    break;
                }
            }
            // mark unclosed brackets
            for (int i = 0; i < closedBracketsWrong.size(); i++){
                if(closedBracketsWrong.get(i)){
                    SyntaxHighlighter.SetTextColor(doc, closedBrackets.get(i), 1, Color.RED);
                }else{
                    SyntaxHighlighter.SetTextColor(doc, closedBrackets.get(i), 1, Color.YELLOW);
                }
            }
            for (int i = 0; i < openBracketsWrong.size(); i++){
                if(openBracketsWrong.get(i)){
                    SyntaxHighlighter.SetTextColor(doc, openBrackets.get(i), 1, Color.RED);
                }else{
                    SyntaxHighlighter.SetTextColor(doc, openBrackets.get(i), 1, Color.YELLOW);
                }
            }


            // last ones!!!!
            // comments (//)
            pos = 0;
            start = 0;
            flipFlop = false;
            String cur_search = "//";
            while ((pos = text.indexOf(cur_search, pos)) >= 0) {
                if(flipFlop){
                    flipFlop = false;
                    cur_search = "//";
                    SyntaxHighlighter.SetTextColor(doc, start, (pos - start), Color.GREEN);
                }else{
                    cur_search = "\n";
                    flipFlop = true;
                    start = pos;
                }
                pos += 1;
            }
            if(flipFlop){
                SyntaxHighlighter.SetTextColor(doc, start, (pos - start), Color.GREEN);
            }

            // comments (/**/)
            pos = 0;
            start = 0;
            flipFlop = false;
            cur_search = "/*";
            while ((pos = text.indexOf(cur_search, pos)) >= 0) {
                if(flipFlop){
                    flipFlop = false;
                    cur_search = "/*";
                    SyntaxHighlighter.SetTextColor(doc, start, (pos - start) + 2, Color.GREEN);
                }else{
                    cur_search = "*/";
                    flipFlop = true;
                    start = pos;
                }
                pos += 1;
            }
            if(flipFlop){
                flipFlop = false;
                cur_search = "//";
                SyntaxHighlighter.SetTextColor(doc, start, (pos - start), Color.RED);
            }
        }
    }


    private void SaveFile(int i){
        if(m_files.get(i).path.length() < 1){
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setDialogTitle("Choose destination.");
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            returnValue = jfc.showSaveDialog(null);
            m_files.get(i).path = jfc.getSelectedFile().getAbsolutePath();
        }
        try {
            System.out.println("Saving file: " + m_files.get(i).path);
            File f = new File(m_files.get(i).path);

            tabs.setTabComponentAt(0, new JLabel(f.getName()));

            FileWriter out = new FileWriter(f);
            JScrollPane scrollPane = (JScrollPane) tabs.getComponentAt(i);
            JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
            StyledDocument doc = textPane.getStyledDocument();
            out.write(doc.getText(0, doc.getLength()));
            out.close();
            m_files.get(i).saved = true;
        } catch (FileNotFoundException ex) {
            Component f = null;
            JOptionPane.showMessageDialog(f,"File not found.");
        } catch (IOException ex) {
            Component f = null;
            JOptionPane.showMessageDialog(f,"Error.");
        } catch (BadLocationException b){
            System.out.println("Error copying text");
            return;
        }
    }

    private void HandleUnsaved(){
        int i = 0;
        while(m_files.get(i).saved){
            i++;
            if(i >= m_files.size()){
                return;
            }
        }
        Object[] options = { "YES", "NO" };
        int ret = JOptionPane.showOptionDialog(null, "You have at least one unsaved File, do you want to save them?", "Unsaved File(s)", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if(ret > 0){
            return;
        }
        for(i = 0; i < m_files.size(); i++){
            if(Saved(i)){
                continue;
            }
            SaveFile(i);
        }
    }


    public TextEdit() { run(); }

    public void OnChage(){
        m_files.get(tabs.getSelectedIndex()).saved = false;
        JScrollPane scrollPane = (JScrollPane) tabs.getSelectedComponent();
        JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
        StyledDocument doc = textPane.getStyledDocument();
        //JavaSyntaxHighlighter.Highlight(textPane, doc);
    }

    /*private void OnChanged(DocumentEvent e){
        m_saved = false;
    }*/

    private int NewTab(String name){
        JTextPane newText = new JTextPane();
        newText.setBackground(Color.GRAY);
        newText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                OnChage();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                OnChage();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                OnChage();
            }
        });

        JScrollPane newScroll = new JScrollPane(newText);
        newScroll.setBackground(Color.GRAY);
        tabs.addTab(name, newScroll);

        m_files.add(new FileInfo(false, "", name));
        return tabs.getTabCount() - 1;
    }

    public void run() {
        frame = new JFrame("Java Editor");
        file_path = "";

        // Set the look-and-feel (LNF) of the application
        // Try to default to whatever the host system prefers
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(TextEdit.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Set attributes of the app window
        tabs = new JTabbedPane();
        tabs.setBackground(Color.GRAY);

        layout = frame.getContentPane();

        layout.add(tabs, BorderLayout.CENTER);

        layout.add(Log.Init(), BorderLayout.PAGE_END);

        for (int i = 0; i < 100; i++){
            Log.Message("Test");
            Log.Warning("Test");
            Log.Error("Test");
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        NewTab("New Java File");


        frame.setSize(640, 480);
        frame.setVisible(true);
        frame.setBackground(Color.GRAY);


        // Build the menu
        JMenuBar menu_main = new JMenuBar();
        menu_main.setBackground(Color.GRAY);

        JMenu menu_file = new JMenu("File");

        JMenuItem menuitem_new = new JMenuItem("New");
        JMenuItem menuitem_open = new JMenuItem("Open");
        JMenuItem menuitem_save = new JMenuItem("Save");
        JMenuItem menuitem_quit = new JMenuItem("Quit");

        menuitem_new.addActionListener(this);
        menuitem_open.addActionListener(this);
        menuitem_save.addActionListener(this);
        menuitem_quit.addActionListener(this);

        menu_main.add(menu_file);

        menu_file.add(menuitem_new);
        menu_file.add(menuitem_open);
        menu_file.add(menuitem_save);
        menu_file.add(menuitem_quit);


        // clear log button
        JButton clear_button = new JButton("Clear Log");
        clear_button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                Log.Clear();
            }
        });

        // colorer (TODO make automatic)
        JButton format_button = new JButton("Format");
        format_button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollPane scrollPane = (JScrollPane) tabs.getSelectedComponent();
                JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
                StyledDocument doc = textPane.getStyledDocument();
                JavaSyntaxHighlighter.Highlight(textPane, doc);
            }
        });

        // compiler 
        JButton compile_button = new JButton("Compile");
        compile_button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = TextEdit.tabs.getSelectedIndex();
                if(TextEdit.m_files.get(id).path.length() < 1){
                    HandleUnsaved();
                }
                Compiler.CopileCMD(TextEdit.m_files.get(id).path);
            }
        });

        // runner
        JButton run_button = new JButton("Run");
        run_button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = TextEdit.tabs.getSelectedIndex();
                if(TextEdit.m_files.get(id).path.length() < 1){
                    HandleUnsaved();
                }
                Compiler.Run(TextEdit.m_files.get(id).path);
            }
        });

        menu_main.add(clear_button);
        menu_main.add(format_button);
        menu_main.add(compile_button);
        menu_main.add(run_button);

        frame.setJMenuBar(menu_main);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String ingest = null;
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Choose destination.");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        String ae = e.getActionCommand();
        if (ae.equals("Open")) { // open new
            //HandleUnsaved();
            
            returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File f = new File(jfc.getSelectedFile().getAbsolutePath());
            try{
                int id = NewTab(f.getName());
                FileReader read = new FileReader(f);
                Scanner scan = new Scanner(read);
                JScrollPane scrollPane = (JScrollPane) tabs.getComponentAt(id);
                JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
                StyledDocument doc = textPane.getStyledDocument();

                m_files.get(id).saved = true;
                m_files.get(id).path = jfc.getSelectedFile().getAbsolutePath();

                while(scan.hasNextLine()){
                    String line = scan.nextLine() + "\n";
                    doc.insertString(doc.getLength(), line, null);
                }

                JavaSyntaxHighlighter.Highlight(textPane, doc);
            }
            catch ( FileNotFoundException ex) { ex.printStackTrace(); }
            catch (BadLocationException b){
                return;
            }
        }
        // SAVE
        } else if (ae.equals("Save")) {
            SaveFile(tabs.getSelectedIndex());
        } else if (ae.equals("New")) {
            NewTab("New Java File");
        } else if (ae.equals("Quit")) {
            HandleUnsaved();
            System.exit(0);
        }
    }
}
