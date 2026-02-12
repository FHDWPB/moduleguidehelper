package moduleguidehelper;

import java.io.*;

public abstract class ModuleGuideWriter {

    private final ModuleGuide guide;

    public ModuleGuideWriter(final ModuleGuide guide) {
        this.guide = guide;
    }

    public void write(final String modulesFolder, final BufferedWriter writer) throws IOException {
        this.writeDocumentStart(writer);
        this.writeTitlePage(this.guide, writer);
        this.writeIntro(this.guide, writer);
        final ModuleOverview overview = ModuleOverview.create(this.guide);
        this.writeOverview(this.guide, overview, writer);
        this.writeModules(this.guide, overview.weightSum(), modulesFolder, writer);
        this.writeDocumentEnd(writer);
    }

    protected abstract void writeDocumentEnd(final BufferedWriter writer) throws IOException;

    protected abstract void writeDocumentStart(final BufferedWriter writer) throws IOException;

    protected abstract void writeIntro(final ModuleGuide guide, final BufferedWriter writer) throws IOException;

    protected abstract void writeModules(
        final ModuleGuide guide,
        final int weightSum,
        final String modulesFolder,
        final BufferedWriter writer
    ) throws IOException;

    protected abstract void writeOverview(
        final ModuleGuide guide,
        final ModuleOverview overview,
        final BufferedWriter writer
    ) throws IOException;

    protected abstract void writeTitlePage(final ModuleGuide guide, final BufferedWriter writer) throws IOException;

}
