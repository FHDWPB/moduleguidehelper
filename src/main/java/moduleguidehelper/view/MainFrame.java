package moduleguidehelper.view;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.*;

import javax.swing.*;

import moduleguidehelper.store.*;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private boolean guideSelected;

    public MainFrame(final String version, final File directory) {
        super(String.format("Module Guide Helper (Version %s)", version));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new GridBagLayout());
        final Container content = this.getContentPane();
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        final JPanel buttons = new JPanel();
        buttons.setLayout(new GridBagLayout());
        final JButton pullButton = new JButton("Aktualisieren");
        pullButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                Process process;
                try {
                    process = new ProcessBuilder(
                        "git",
                        "add",
                        "-A"
                    ).inheritIO().directory(directory).start();
                    process.waitFor(60, TimeUnit.SECONDS);
                    process = new ProcessBuilder(
                        "git",
                        "commit",
                        "-m",
                        "update"
                    ).inheritIO().directory(directory).start();
                    process.waitFor(60, TimeUnit.SECONDS);
                    process = new ProcessBuilder(
                        "git",
                        "pull",
                        "--rebase"
                    ).inheritIO().directory(directory).start();
                    process.waitFor(60, TimeUnit.SECONDS);
                    process = new ProcessBuilder(
                        "git",
                        "push"
                    ).inheritIO().directory(directory).start();
                    process.waitFor(60, TimeUnit.SECONDS);
                } catch (IOException | InterruptedException e1) {
                    JOptionPane.showMessageDialog(MainFrame.this, e1);
                }
            }

        });
        buttons.add(pullButton, constraints);
        final JButton generateButton = new JButton("Modulhandbücher erzeugen");
        Store.INSTANCE.registerGuideObserver(
            guides -> {
                this.guideSelected = !guides.isEmpty();
                generateButton.setEnabled(this.guideSelected);
            }
        );
        generateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    Store.INSTANCE.generatePDFs(directory);
                } catch (final Exception e) {
                    JOptionPane.showMessageDialog(MainFrame.this, e);
                }
            }

        });
        constraints.gridy = 1;
        buttons.add(generateButton, constraints);
        constraints.gridy = 0;
        constraints.ipadx = 10;
        constraints.ipady = 10;
        constraints.gridwidth = 4;
        content.add(new JLabel(), constraints);
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        content.add(new JLabel(), constraints);
        constraints.gridx = 1;
        content.add(new GuideChooserPanel(directory.listFiles(file -> file.getName().endsWith(".json"))), constraints);
        constraints.gridx = 2;
        content.add(buttons, constraints);
        constraints.gridx = 3;
        content.add(new JLabel(), constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridheight = 1;
        constraints.gridwidth = 4;
        content.add(new JLabel(), constraints);
        this.pack();
        this.setLocationRelativeTo(null);
    }

}
