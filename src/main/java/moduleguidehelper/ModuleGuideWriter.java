package moduleguidehelper;

import java.io.*;

public abstract class ModuleGuideWriter {

    private final ModuleGuide book;

    private final ModuleMap modules;

    public ModuleGuideWriter(final ModuleGuide book, final ModuleMap modules) {
        this.book = book;
        this.modules = modules;
    }

    public void write(final BufferedWriter writer) throws IOException {
        this.writeDocumentStart(writer);
        this.writeTitlePage(this.book, writer);
        this.writeIntro(this.book, writer);
        this.writeOverview(this.book, ModuleOverview.create(this.book, this.modules), writer);
        this.writeModules(this.book, this.modules, writer);
        this.writeDocumentEnd(writer);
    }

    protected abstract void writeDocumentEnd(final BufferedWriter writer) throws IOException;

    protected abstract void writeDocumentStart(final BufferedWriter writer) throws IOException;

    protected abstract void writeIntro(final ModuleGuide book, final BufferedWriter writer) throws IOException;

    protected abstract void writeModules(ModuleGuide book, ModuleMap modules, BufferedWriter writer) throws IOException;

    protected abstract void writeOverview(
        final ModuleGuide book,
        final ModuleOverview overview,
        final BufferedWriter writer
    ) throws IOException;

    protected abstract void writeTitlePage(final ModuleGuide book, final BufferedWriter writer) throws IOException;

}
