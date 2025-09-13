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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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


    private static JTabbedPane tabs;
    private static JFrame frame;
    private static int returnValue = 0;

    private static String file_path;

    public static ArrayList<FileInfo> m_files = new ArrayList<FileInfo>();

    public boolean Saved(int i){return m_files.get(i).saved;};

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
        m_files.get(tabs.getSelectedIndex());
    }

    /*private void OnChanged(DocumentEvent e){
        m_saved = false;
    }*/

    void OpenFile(){

    }

    private int NewTab(String name){
        JTextPane newText = new JTextPane();
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(tabs);
        NewTab("New Java File");


        frame.setSize(640, 480);
        frame.setVisible(true);

        // Build the menu
        JMenuBar menu_main = new JMenuBar();

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

        JButton compile_button = new JButton("Compile");
        compile_button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = TextEdit.tabs.getSelectedIndex();
                if(TextEdit.m_files.get(id).path.length() < 1){
                    HandleUnsaved();
                }
                Compiler.Compile(TextEdit.m_files.get(id).path);
            }
        });

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
                JScrollPane scrollPane = (JScrollPane) tabs.getSelectedComponent();
                JTextPane textPane = (JTextPane) scrollPane.getViewport().getView();
                StyledDocument doc = textPane.getStyledDocument();

                m_files.get(id).saved = true;

                while(scan.hasNextLine()){
                    String line = scan.nextLine() + "\n";
                    doc.insertString(doc.getLength(), line, null);
                }
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
