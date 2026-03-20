package moduleguidehelper.view;

import java.io.*;
import java.util.*;
import java.util.function.*;

import javax.swing.*;
import javax.swing.event.*;

public class FileChooserPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public FileChooserPanel(final String title, final File[] files, final Consumer<List<File>> listener) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(new JLabel(title));
        final JList<File> chooser = new JList<File>(files);
        chooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        chooser.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(final ListSelectionEvent e) {
                listener.accept(chooser.getSelectedValuesList());
            }

        });
        this.add(new JScrollPane(chooser));
    }

}
