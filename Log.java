import javax.swing.JTabbedPane;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Log{
    public static void Message(String msg){
        model.addElement(new MessageData(0, msg));
    }
    public static void Warning(String msg){
        model.addElement(new MessageData(1, msg));
    }
    public static void Error(String msg){
        model.addElement(new MessageData(2, msg));
    }

    public static void Clear(){
        model.clear();
    }

    private static JTabbedPane master;
    private static DefaultListModel<MessageData> model;

    public static JTabbedPane Init(){
        master = new JTabbedPane();

        JMenuBar menu_main = new JMenuBar();
        menu_main.setBackground(Color.GRAY);

        model = new DefaultListModel<>();


        JList<MessageData> messageList = new JList(model);
        JScrollPane messageScroll = new JScrollPane(messageList);

        messageList.setCellRenderer(new MessageListCellRenderer());

        master.addTab("Log", messageScroll);

        return master;
    }

    public static class MessageData{
        public String content;
        public int severity;

        public MessageData(int sev, String msg){
            severity = sev;
            content = msg;
        }
    }

    public static class MessageListCellRenderer extends JPanel implements ListCellRenderer<MessageData> {
        private JLabel type_label;
        private JLabel content_label;

        public MessageListCellRenderer() {
            super(new BorderLayout(30, 0));
            type_label = new JLabel();
            content_label = new JLabel();

            add(type_label, BorderLayout.LINE_START);
            add(content_label, BorderLayout.CENTER);
            
            //setOpaque(true); //for visible backgrounds
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends MessageData> list, MessageData value, int index, boolean isSelected,
                boolean cellHasFocus) {
            content_label.setText(value.content);
            
            if(value.severity == 0){ // message
                type_label.setText("message");
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                content_label.setForeground(list.getForeground());
                content_label.setBackground(list.getBackground());
                type_label.setForeground(list.getForeground());
                type_label.setBackground(list.getBackground());
            }else if(value.severity == 1){ // warning
                type_label.setText("Warning");            
                setBackground(Color.YELLOW);
                content_label.setBackground(Color.YELLOW);
                type_label.setBackground(Color.YELLOW);
            }else{ // error
                type_label.setText("ERROR    ");
                setBackground(Color.RED);
                content_label.setBackground(Color.RED);
                type_label.setBackground(Color.RED);
            }
            
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                content_label.setForeground(list.getSelectionForeground());
                content_label.setBackground(list.getSelectionBackground());
                type_label.setForeground(list.getSelectionForeground());
                type_label.setBackground(list.getSelectionBackground());
            }
            return this;
        }

    }
}