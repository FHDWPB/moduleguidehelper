package modulebookhelper;

import java.io.*;

public abstract class ModuleBookWriter {

    private final ModuleBook book;

    private final ModuleMap modules;

    public ModuleBookWriter(final ModuleBook book, final ModuleMap modules) {
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

    protected abstract void writeModules(ModuleBook book, ModuleMap modules, BufferedWriter writer) throws IOException;

    protected abstract void writeOverview(
        final ModuleBook book,
        final ModuleOverview overview,
        final BufferedWriter writer
    ) throws IOException;

    protected abstract void writeIntro(final ModuleBook book, final BufferedWriter writer) throws IOException;

    protected abstract void writeTitlePage(final ModuleBook book, final BufferedWriter writer) throws IOException;

}
