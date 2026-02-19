package moduleguidehelper.store;

import java.io.*;
import java.util.*;

@FunctionalInterface
public interface GuideObserver {

    void notify(Set<File> guides);

}
