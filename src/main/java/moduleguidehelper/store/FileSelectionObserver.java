package moduleguidehelper.store;

import java.io.*;
import java.util.*;

@FunctionalInterface
public interface FileSelectionObserver {

    void notify(Set<File> selection);

}
