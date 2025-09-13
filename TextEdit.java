import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileSystemView;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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


public final class TextEdit extends JFrame implements ActionListener{
    private static JTextArea area;
    private static JFrame frame;
    private static int returnValue = 0;

    private static String file_path;
    private static boolean m_saved = true;

    public boolean Saved(){return m_saved;};

    private void SaveFile(){
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setDialogTitle("Choose destination.");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        returnValue = jfc.showSaveDialog(null);
        try {
            File f = new File(jfc.getSelectedFile().getAbsolutePath());
            FileWriter out = new FileWriter(f);
            out.write(area.getText());
            out.close();
            m_saved = true;
        } catch (FileNotFoundException ex) {
            Component f = null;
            JOptionPane.showMessageDialog(f,"File not found.");
        } catch (IOException ex) {
            Component f = null;
            JOptionPane.showMessageDialog(f,"Error.");
        }
    }

    private void HandleUnsaved(){
        if(Saved()){
            return;
        }
        Object[] options = { "YES", "NO" };
        int ret = JOptionPane.showOptionDialog(null, "You have one unsaved File, do you want to save it?", "Unsaved File", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if(ret > 0){
            return;
        }

        SaveFile();
    }


    public TextEdit() { run(); }

    private void OnChanged(DocumentEvent e){
        m_saved = false;
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
        area = new JTextArea();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        area.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                OnChanged(e);
            }
            public void removeUpdate(DocumentEvent e) {
                OnChanged(e);
            }
            public void insertUpdate(DocumentEvent e) {
                OnChanged(e);
            }
        });

        JScrollPane scroll_pane = new JScrollPane(area);
        frame.add(scroll_pane);
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
                if(TextEdit.file_path.length() < 1){
                    HandleUnsaved();
                }
                Compiler.Compile(TextEdit.file_path);
            }
        });

        JButton run_button = new JButton("Run");
        run_button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(TextEdit.file_path.length() < 1){
                    HandleUnsaved();
                }
                Compiler.Run(TextEdit.file_path);
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
            HandleUnsaved();

            returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File f = new File(jfc.getSelectedFile().getAbsolutePath());
            try{
                FileReader read = new FileReader(f);
                Scanner scan = new Scanner(read);
                while(scan.hasNextLine()){
                    String line = scan.nextLine() + "\n";
                    ingest = ingest + line;
            }
                area.setText(ingest);
                file_path = jfc.getSelectedFile().getAbsolutePath();
            }
            catch ( FileNotFoundException ex) { ex.printStackTrace(); }
            }
        // SAVE
        } else if (ae.equals("Save")) {
            SaveFile();
        } else if (ae.equals("New")) {
            HandleUnsaved();
            area.setText("");
            file_path = "";
        } else if (ae.equals("Quit")) {
            HandleUnsaved();
            System.exit(0);
        }
    }
}
