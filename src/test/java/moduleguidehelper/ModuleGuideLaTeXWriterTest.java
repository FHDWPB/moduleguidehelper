package moduleguidehelper;

import org.testng.*;
import org.testng.annotations.*;

public class ModuleGuideLaTeXWriterTest {

    @DataProvider
    public Object[][] escapeForLaTeXData() {
        return new Object[][] {
            {null, ""},
            {"", ""},
            {"foo", "foo"},
            {"$$$$", "\\$\\$\\$\\$"},
            {"$$ $$", " "},
            {"$$bar$$", "bar"},
            {"$$\\llb{}$$", "\\llb{}"},
            {"$$$$$", "\\$\\$\\$\\$\\$"},
            {"$$$", "\\$\\$\\$"},
            {"$$", "\\$\\$"},
            {"$", "\\$"},
            {"$${$x+y$}$$", "{$x+y$}"},
            {"foo$$bar$$baz", "foobarbaz"},
            {"foo$$\\llb{}$$baz", "foo\\llb{}baz"},
            {"&", "\\&"},
            {"%", "\\%"},
            {"_", "\\_"},
            {"#", "\\#"},
            {"{", "\\{"},
            {"}", "\\}"},
            {"~", "\\textasciitilde{}"},
            {"\\", "\\textbackslash{}"},
            {"\"", "''"}
        };
    }

    @Test(dataProvider="escapeForLaTeXData")
    public void escapeForLaTeXTest(final String text, final String expected) {
        Assert.assertEquals(ModuleGuideLaTeXWriter.escapeForLaTeX(text), expected);
    }

}
