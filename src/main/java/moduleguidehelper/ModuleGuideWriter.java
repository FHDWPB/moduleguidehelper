package moduleguidehelper;

import java.io.*;

public abstract class ModuleGuideWriter {

    protected final ModuleGuide guide;

    public ModuleGuideWriter(final ModuleGuide guide) {
        this.guide = guide;
    }

    public void write(final String modulesFolder, final BufferedWriter writer) throws IOException {
        this.writeDocumentStart(writer);
        this.writeTitlePage(writer);
        this.writeIntro(writer);
        final ModuleOverview overview = ModuleOverview.create(this.guide);
        this.writeOverview(overview, writer);
        this.writeModules(overview.weightSum(), modulesFolder, writer);
        this.writeDocumentEnd(writer);
    }

    protected abstract void writeDocumentEnd(final BufferedWriter writer) throws IOException;

    protected abstract void writeDocumentStart(final BufferedWriter writer) throws IOException;

    protected abstract void writeIntro(final BufferedWriter writer) throws IOException;

    protected abstract void writeModules(
        final int weightSum,
        final String modulesFolder,
        final BufferedWriter writer
    ) throws IOException;

    protected abstract void writeOverview(
        final ModuleOverview overview,
        final BufferedWriter writer
    ) throws IOException;

    protected abstract void writeTitlePage(final BufferedWriter writer) throws IOException;

}
