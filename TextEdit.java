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
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.*;

import java.util.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;

class FileInfo{
        boolean saved;
        boolean compiled;
        Path path;
        String name;

        public FileInfo(boolean s, Path pt, String nm, boolean cmpt){
            saved = s;
            path = pt;
            name = nm;
            compiled = cmpt;
        }

        public String toString(){
            return name;
        }
    }


public final class TextEdit extends JFrame implements ActionListener {

    /*class FileInfo{
        boolean saved;
        boolean compiled;
        Path path;
        String name;

        public FileInfo(boolean s, Path pt, String nm, boolean cmpt){
            saved = s;
            path = pt;
            name = nm;
            compiled = cmpt;
        }

        public String toString(){
            return name;
        }
    }*/

    private static Container layout;
    private static JTabbedPane tabs;
    private static JTree tree;
    private static JFrame frame;
    private static int returnValue = 0;

    private static String file_path;

    public static ArrayList<FileInfo> m_files = new ArrayList<FileInfo>();
    public static ArrayList<FileInfo> m_files_not_compiled = new ArrayList<FileInfo>();
    

    public boolean Saved(int i){return m_files.get(i).saved;};


    Collection<Path> allFiles;

    Path root_folder;

    private void setFont(FontUIResource myFont) {
        UIManager.put("CheckBoxMenuItem.acceleratorFont", myFont);
        UIManager.put("Button.font", myFont);
        UIManager.put("ToggleButton.font", myFont);
        UIManager.put("RadioButton.font", myFont);
        UIManager.put("CheckBox.font", myFont);
        UIManager.put("ColorChooser.font", myFont);
        UIManager.put("ComboBox.font", myFont);
        UIManager.put("Label.font", myFont);
        UIManager.put("List.font", myFont);
        UIManager.put("MenuBar.font", myFont);
        UIManager.put("Menu.acceleratorFont", myFont);
        UIManager.put("RadioButtonMenuItem.acceleratorFont", myFont);
        UIManager.put("MenuItem.acceleratorFont", myFont);
        UIManager.put("MenuItem.font", myFont);
        UIManager.put("RadioButtonMenuItem.font", myFont);
        UIManager.put("CheckBoxMenuItem.font", myFont);
        UIManager.put("OptionPane.buttonFont", myFont);
        UIManager.put("OptionPane.messageFont", myFont);
        UIManager.put("Menu.font", myFont);
        UIManager.put("PopupMenu.font", myFont);
        UIManager.put("OptionPane.font", myFont);
        UIManager.put("Panel.font", myFont);
        UIManager.put("ProgressBar.font", myFont);
        UIManager.put("ScrollPane.font", myFont);
        UIManager.put("Viewport.font", myFont);
        UIManager.put("TabbedPane.font", myFont);
        UIManager.put("Slider.font", myFont);
        UIManager.put("Table.font", myFont);
        UIManager.put("TableHeader.font", myFont);
        UIManager.put("TextField.font", myFont);
        UIManager.put("Spinner.font", myFont);
        UIManager.put("PasswordField.font", myFont);
        UIManager.put("TextArea.font", myFont);
        UIManager.put("TextPane.font", myFont);
        UIManager.put("EditorPane.font", myFont);
        UIManager.put("TabbedPane.smallFont", myFont);
        UIManager.put("TitledBorder.font", myFont);
        UIManager.put("ToolBar.font", myFont);
        UIManager.put("ToolTip.font", myFont);
        UIManager.put("Tree.font", myFont);
        UIManager.put("FormattedTextField.font", myFont);
        UIManager.put("IconButton.font", myFont);
        UIManager.put("InternalFrame.optionDialogTitleFont", myFont);
        UIManager.put("InternalFrame.paletteTitleFont", myFont);
        UIManager.put("InternalFrame.titleFont", myFont);
    }

    public void RescanDir(){
        allFiles = new ArrayList<Path>();
        while(m_files.size() > 0){
            m_files.remove(0);
            tabs.remove(0);
        }
        
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Root");
        try {
            AddTree(top, root_folder, allFiles);
        }catch (IOException ioE){
            Log.Error("got some IO error");
        }catch (NullPointerException ex){
            Log.Error("got some nullptr error");
        }

        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(new TreeSelectionListener(){
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

                if (node == null) return;

                Object nodeInfo = node.getUserObject();
                if (!node.isLeaf()) {
                    return;
                }
                FileInfo file_infsoas = (FileInfo)nodeInfo;
                if(file_infsoas == null){
                    return;
                }
                
                Open(file_infsoas.path);
            }
        });

        JScrollPane treeView = new JScrollPane(tree);
        layout.add(treeView, BorderLayout.LINE_START);
    }

    private void AddTree(DefaultMutableTreeNode parent, Path directory, Collection<Path> all) throws IOException {
        Log.Message("Stepping into: " + directory.getFileName().toString());
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
            for (Path child : ds) {
                //Log.Message(child.getFileName().toString());
                if (Files.isDirectory(child)) {
                    DefaultMutableTreeNode child_node = new DefaultMutableTreeNode(child.getFileName().toString());
                    parent.add(child_node);

                    AddTree(child_node, child, all);
                }else{
                    String file_str =  child.toString();
                    if(file_str.endsWith(".java")){
                        //Log.Message("Adding " + child.getFileName().toString());
                        JButton button = new JButton(child.getFileName().toString());
                        DefaultMutableTreeNode child_node = new DefaultMutableTreeNode(new FileInfo(true, child, child.getFileName().toString(), false));
                        //m_files_all.add((FileInfo)child_node.getUserObject());
                        m_files_not_compiled.add((FileInfo)child_node.getUserObject());
                        
                        parent.add(child_node);
                        all.add(child);
                    }
                }

            }
        }
    }


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
                "while", "public", "private", "static", "void", "int", "double", "import", "@Override", "implements", "true", "false"
            };
            String text = textPane.getText();//.replace("\n", "");//.replace("\r", "");

            // remove newline characters
            

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
                //System.out.println(pos);
                openBrackets.add(pos);
                openBracketsWrong.add(true);
                pos += 1;
            }
            pos = 0;
            while ((pos = text.indexOf(")", pos)) >= 0) {
                //System.out.println(pos);
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

    private void Open(Path path){
        try {
            path = path.toRealPath();
        }catch (IOException iE){
            Log.Error("invald Path");
            return;
        }

        Log.Warning(Integer.toString(m_files.size()));
        for(int i = 0; i < m_files.size(); i++){
            try{
                Log.Warning(m_files.get(i).path.toRealPath().toString());
                if(m_files.get(i).path.toRealPath().toString().equals(path.toString())){
                    tabs.setSelectedIndex(i);
                    Log.Message(m_files.get(i).path.toRealPath().toString() + " " + path.toString() + " is already open!");
                    return;
                }
            }catch (IOException iE){
                continue;
            }
        }
/*
        String ingest = null;
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Choose destination.");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);


        returnValue = jfc.showOpenDialog(null);*/
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File f = path.toFile();
            try{
                int id = NewTab(f.getName());
                FileReader read = new FileReader(f);
                Scanner scan = new Scanner(read);
                JScrollPane scrollPane = (JScrollPane) tabs.getComponentAt(id);
                JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
                StyledDocument doc = textPane.getStyledDocument();

                m_files.get(id).saved = true;
                m_files.get(id).path = path;

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
    }

    private void SaveFile(int i){
        m_files_not_compiled.add(m_files.get(i));
        if(m_files.get(i).path.toString().length() < 1){
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setDialogTitle("Choose destination.");
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            returnValue = jfc.showSaveDialog(null);
            m_files.get(i).path = Paths.get(jfc.getSelectedFile().getAbsolutePath());
        }
        try {
            System.out.println("Saving file: " + m_files.get(i).path.toString());
            File f = m_files.get(i).path.toFile();

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
        tabs.setTitleAt(i, m_files.get(i).name);
    }

    private void HandleUnsaved(){
        for (int i = 0; i < m_files.size(); i++){
            if(!m_files.get(i).saved){
                break;
            }
        }
        Object[] options = { "YES", "NO" };
        int ret = JOptionPane.showOptionDialog(null, "You have at least one unsaved File, do you want to save them?", "Unsaved File(s)", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if(ret > 0){
            return;
        }
        for(int i = 0; i < m_files.size(); i++){
            if(Saved(i)){
                continue;
            }
            SaveFile(i);
        }
    }


    public TextEdit() { run(); }

    public void OnChage(){
        if(Saved(tabs.getSelectedIndex())){
            m_files_not_compiled.add(m_files.get(tabs.getSelectedIndex()));
        }
        m_files.get(tabs.getSelectedIndex()).saved = false;
        m_files.get(tabs.getSelectedIndex()).compiled = false;
        tabs.setTitleAt(tabs.getSelectedIndex(), m_files.get(tabs.getSelectedIndex()).name + " *");
    
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

        m_files.add(new FileInfo(false, Paths.get(""), name, false));
        m_files_not_compiled.add(m_files.get(m_files.size() - 1));
        return tabs.getTabCount() - 1;
    }

    private void CloseTab(int i){
        HandleUnsaved();
        m_files.remove(i);
        tabs.remove(i);
    }

    public void run() {
        setFont(new FontUIResource(new Font("monospaced", Font.PLAIN, 14)));

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
/*
        for (int i = 0; i < 100; i++){
            Log.Message("Test");
            Log.Warning("Test");
            Log.Error("Test");
        }*/
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        RescanDir();
        

        frame.setSize(640, 480);
        frame.setVisible(true);
        frame.setBackground(Color.GRAY);


        // Build the menu
        JMenuBar menu_main = new JMenuBar();
        menu_main.setBackground(Color.GRAY);

        JMenu menu_file = new JMenu("File");

        JMenuItem menuitem_new = new JMenuItem("New");
        JMenuItem menuitem_open = new JMenuItem("Open Folder");
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
                HandleUnsaved();
                if(m_files_not_compiled.size() < 1){
                    return;
                }
                if(Compiler.CopileCMD(m_files_not_compiled)){
                    m_files_not_compiled.clear();
                }
            }
        });

        // runner
        JButton run_button = new JButton("Run");
        run_button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = TextEdit.tabs.getSelectedIndex();
                HandleUnsaved();
                if(m_files_not_compiled.size() < 1){
                    if(Compiler.CopileCMD(m_files_not_compiled)){
                        m_files_not_compiled.clear();
                    }
                }
                Compiler.Run(TextEdit.m_files.get(id).path.toString());
                m_files_not_compiled.clear();
            }
        });

        // cloce tab
        JButton close_button = new JButton("Close Tab");
        close_button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = TextEdit.tabs.getSelectedIndex();
                CloseTab(id);
            }
        });

        menu_main.add(clear_button);
        menu_main.add(format_button);
        menu_main.add(compile_button);
        menu_main.add(run_button);
        menu_main.add(close_button);

        frame.setJMenuBar(menu_main);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        

        String ae = e.getActionCommand();
        if (ae.equals("Open Folder")) { // open new
            //HandleUnsaved();
            String ingest = null;
            JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            jfc.setDialogTitle("Choose Folder.");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            returnValue = jfc.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                HandleUnsaved();
                root_folder = Paths.get(jfc.getSelectedFile().getAbsolutePath());
                RescanDir();
            }
        }
        else if (ae.equals("Save")) {
            SaveFile(tabs.getSelectedIndex());
        } else if (ae.equals("New")) {
            NewTab("New Java File *");
        } else if (ae.equals("Quit")) {
            HandleUnsaved();
            System.exit(0);
        }
    }
}
