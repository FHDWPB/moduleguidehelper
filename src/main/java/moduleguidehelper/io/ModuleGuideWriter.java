package moduleguidehelper.io;

import java.io.*;

import moduleguidehelper.model.*;

public abstract class ModuleGuideWriter {

    protected final ModuleGuide guide;

    public ModuleGuideWriter(final ModuleGuide guide) {
        this.guide = guide;
    }

    public void write(
        final ModuleGuide guide,
        final File modulesFolder,
        final BufferedWriter writer
    ) throws IOException {
        this.writeDocumentStart(guide.generalLanguage(), writer);
        this.writeTitlePage(writer);
        this.writeIntro(guide.mode() == CurriculumMode.DUAL, writer);
        final ModuleOverview overview = ModuleOverviewBuilder.create(this.guide);
        this.writeOverview(overview, writer);
        this.writeModules(overview.weightSum(), modulesFolder, writer);
        this.writeDocumentEnd(writer);
    }

    protected abstract void writeDocumentEnd(final BufferedWriter writer) throws IOException;

    protected abstract void writeDocumentStart(final Language language, final BufferedWriter writer) throws IOException;

    protected abstract void writeIntro(final boolean partners, final BufferedWriter writer) throws IOException;

    protected abstract void writeModules(
        final int weightSum,
        final File modulesFolder,
        final BufferedWriter writer
    ) throws IOException;

    protected abstract void writeOverview(
        final ModuleOverview overview,
        final BufferedWriter writer
    ) throws IOException;

    protected abstract void writeTitlePage(final BufferedWriter writer) throws IOException;

}
