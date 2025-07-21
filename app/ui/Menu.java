package ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;

import appWindow.AppPanel;
import appWindow.EnvironmentApplication;
import stryckyzzzComponents.StryckyzzzClasses.StryckyzzzTextArea;

/** Menu ui class, handles app's menu
 * <p>
 *  @author MelodieBazGui
 */
public class Menu extends JComponent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4316949777569926841L;
	
	private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenuItem newItem;
    private JMenuItem langItem;
    private JMenuItem exitItem;
    private JMenuBar menuBar;
    private JMenuItem aboutItem;
    private JDialog dialog;

    /**
     * Instantiate the menu from the app's JPanel
     * <p>
     * Creates a JMenuBar, and JmenuItems, filling them with language text handlers
     * <p>
     * @param pa
     */
    public Menu(AppPanel appPanel) {
    	
        menuBar = new JMenuBar();

        fileMenu = new JMenu(new StryckyzzzTextArea("menu.file").getText());
        newItem = new JMenuItem(new StryckyzzzTextArea("menu.newFile").getText());
        langItem = new JMenuItem(new StryckyzzzTextArea("menu.lang").getText());
        exitItem = new JMenuItem(new StryckyzzzTextArea("menu.exit").getText());

        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(langItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        helpMenu = new JMenu(new StryckyzzzTextArea("menu.help").getText());
        aboutItem = new JMenuItem(new StryckyzzzTextArea("menu.help").getText());
        aboutItem.addActionListener(e ->
            JOptionPane.showMessageDialog(
                    new JFrame(),
                    EnvironmentApplication.NAME + " " + EnvironmentApplication.VERSION,
                    new StryckyzzzTextArea("menu.help").getText(),
                    JOptionPane.INFORMATION_MESSAGE)
            );
       
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        EnvironmentApplication.frame.setJMenuBar(menuBar);
        
        langItem.addActionListener(e -> {
            JPanel langPanel = new JPanel(new GridLayout(0, 1));
            List<JButton> jbuttonList = new ArrayList<>();

            EnvironmentApplication.LL.getLangs().forEach((s) -> {
                JButton langButton = new JButton(new StryckyzzzTextArea("lang." + s).getText());
                langButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    	EnvironmentApplication.changeDefaultLang(s + ".txt");
                    	EnvironmentApplication.LL.loadLanguage();
                    	appPanel.reloadUIs();
                    	dialog.dispose();
                    }
                });
                jbuttonList.add(langButton);
            });

            jbuttonList.forEach(langPanel::add);

            dialog = new JDialog(EnvironmentApplication.frame, EnvironmentApplication.LL.getSingle("menu.lang"), true);
            dialog.getContentPane().add(langPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(appPanel);
            dialog.setVisible(true);
        });
        
    }
    
    /**
     * Resets text upon language change, using STAs
     * Do not reload anything else
     */
    public void reloadText() {
        fileMenu.setText(new StryckyzzzTextArea("menu.file").getText());
        newItem.setText(new StryckyzzzTextArea("menu.newFile").getText());
        langItem.setText(new StryckyzzzTextArea("menu.lang").getText());
        exitItem.setText(new StryckyzzzTextArea("menu.exit").getText());

        helpMenu.setText(new StryckyzzzTextArea("menu.help").getText());
        aboutItem.setText(new StryckyzzzTextArea("menu.help").getText());
    }
}

