package net.darkhax.blasphemetest;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;

import net.darkhax.blaspheme.Blaspheme;

// This is just a basic GUI to test the lib. I suck at GUIs, so
// any feedback would be very appreciated. 
public class Main extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static boolean downloading = false;
    
    public static void main (String[] args) {
        
        javax.swing.SwingUtilities.invokeLater( () -> {
            
            try {
                
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                final JFrame frame = new Main();
                frame.pack();
                frame.setVisible(true);
                frame.setSize(690, 240);
                frame.setResizable(false);
                frame.setLocationRelativeTo(null);
                frame.setTitle("Blaspheme Pack Downloader");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
            
            catch (final Exception e) {
                
            }          
        });
    }
    
    public Main() {
        
        this.getContentPane().setLayout(new FlowLayout());
        
        final JTextField modpackField = new JTextField(40);
        modpackField.setText("Enter Your Pack URL Here");
        this.add(modpackField);
        
        final JButton button = new JButton("Download Pack");
        button.addActionListener(e -> {
            
            if (!downloading) {
                
                downloading = true;
                
                try {
                    
                    new Thread() {
                        @Override
                        public void run () {
                            
                            try {
                                Blaspheme.downloadModPackFromURL(modpackField.getText(), false);
                            }
                            catch (final Exception e) {
                                
                                e.printStackTrace();
                            }
                            downloading = false;
                        }
                    }.start();
                    
                }
                
                catch (final Exception e1) {
                    
                    e1.printStackTrace();
                    downloading = false;
                }
            }
        });
        this.add(button);
        
        final JTextArea logArea = new JTextArea(8, 80);
        logArea.setBackground(Color.WHITE);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        final JScrollPane scrollPane = new JScrollPane(logArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        final DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        Blaspheme.LOGGER.addHandler(new LogHandler(logArea));
        this.add(scrollPane);
    }
    
    private static class LogHandler extends Handler {
        
        private final JTextArea area;
        
        public LogHandler(JTextArea area) {
            
            this.area = area;
        }
        
        @Override
        public void publish (LogRecord record) {
            
            this.area.append(record.getMessage() + "\n");
        }
        
        @Override
        public void flush () {
            
        }
        
        @Override
        public void close () throws SecurityException {
            
        }
    }
}