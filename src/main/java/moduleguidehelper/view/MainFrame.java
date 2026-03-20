package moduleguidehelper.view;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.function.*;

import javax.swing.*;

import moduleguidehelper.store.*;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private boolean fileSelected;

    public MainFrame(final String version, final File directory) {
        super(String.format("Module Guide Helper (Version %s)", version));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLayout(new GridBagLayout());
        final Container content = this.getContentPane();
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        final JLabel progress = new JLabel(" ");
        final Consumer<Integer> progressListener = p -> {
            if (p < 0) {
                progress.setText(" ");
            } else {
                progress.setText(String.format("Fortschritt: %d%%", p));
            }
            MainFrame.this.revalidate();
            MainFrame.this.repaint();
        };
        final JPanel buttons = new JPanel();
        buttons.setLayout(new GridBagLayout());
        final JButton pullButton = new JButton("Aktualisieren");
        pullButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    new Thread(() -> {
                        try {
                            Store.INSTANCE.syncgit(directory, progressListener);
                            progressListener.accept(-1);
                            JOptionPane.showMessageDialog(null, "Erfolgreich aktualisiert!");
                        } catch (final Exception e) {
                            progressListener.accept(-1);
                            JOptionPane.showMessageDialog(MainFrame.this, e);
                        }
                    }).start();
                } catch (final Exception e) {
                    progressListener.accept(-1);
                    JOptionPane.showMessageDialog(MainFrame.this, e);
                }
            }

        });
        constraints.fill = GridBagConstraints.BOTH;
        buttons.add(pullButton, constraints);
        final JButton generateButton = new JButton("PDFs erzeugen");
        Store.INSTANCE.registerFileObserver(
            files -> {
                this.fileSelected = !files.isEmpty();
                generateButton.setEnabled(this.fileSelected);
            }
        );
        generateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    new Thread(() -> {
                        try {
                            Store.INSTANCE.generatePDFs(directory, progressListener);
                            progressListener.accept(-1);
                            JOptionPane.showMessageDialog(null, "Erfolgreich kompiliert!");
                        } catch (final Exception e) {
                            progressListener.accept(-1);
                            JOptionPane.showMessageDialog(MainFrame.this, e);
                        }
                    }).start();
                } catch (final Exception e) {
                    progressListener.accept(-1);
                    JOptionPane.showMessageDialog(MainFrame.this, e);
                }
            }

        });
        constraints.gridy = 1;
        buttons.add(generateButton, constraints);
        constraints.gridy = 0;
        constraints.ipadx = 10;
        constraints.ipady = 10;
        constraints.gridwidth = 5;
        constraints.fill = GridBagConstraints.NONE;
        content.add(new JLabel(), constraints);
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        content.add(new JLabel(), constraints);
        constraints.gridx = 1;
        content.add(
            new FileChooserPanel(
                "Auswahl der Modulhandbücher:",
                directory.listFiles(file -> file.getName().endsWith(".json")),
                Store.INSTANCE::setGuides
            ),
            constraints
        );
        constraints.gridx = 2;
        content.add(
            new FileChooserPanel(
                "Auswahl einzelner Module:",
                directory.toPath().resolve("modules").toFile().listFiles(file -> file.getName().endsWith(".json")),
                Store.INSTANCE::setModules
            ),
            constraints
        );
        constraints.gridx = 3;
        content.add(buttons, constraints);
        constraints.gridx = 4;
        content.add(new JLabel(), constraints);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridheight = 1;
        constraints.gridwidth = 5;
        content.add(new JLabel(), constraints);
        constraints.gridy = 3;
        content.add(progress, constraints);
        constraints.gridy = 4;
        content.add(new JLabel(), constraints);
        this.pack();
        this.setLocationRelativeTo(null);
    }

}
