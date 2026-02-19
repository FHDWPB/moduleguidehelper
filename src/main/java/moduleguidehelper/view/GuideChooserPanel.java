package moduleguidehelper.view;

import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import moduleguidehelper.store.*;

public class GuideChooserPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public GuideChooserPanel(final File[] guides) {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(new JLabel("Auswahl der Modulhandbücher:"));
        final JList<File> chooser = new JList<File>(guides);
        chooser.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        chooser.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(final ListSelectionEvent e) {
                Store.INSTANCE.setGuides(chooser.getSelectedValuesList());
            }

        });
        this.add(new JScrollPane(chooser));
    }

}
