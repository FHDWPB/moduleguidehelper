package moduleguidehelper.io;

import java.io.*;
import java.util.*;

import org.testng.*;
import org.testng.annotations.*;

import moduleguidehelper.model.bibtex.*;

public class BibTeXParserTest {

    @DataProvider
    public Object[][] parseData() {
        return new Object[][] {
            {"", new BibTeXDatabase()},
            {"@string{x=\"foo\"}", new BibTeXDatabase(List.of(new BibTeXString("x", new BibTeXText("foo"))))},
            {"@string(x={foo})", new BibTeXDatabase(List.of(new BibTeXString("x", new BibTeXText("foo"))))},
            {"@string{x={foo}}", new BibTeXDatabase(List.of(new BibTeXString("x", new BibTeXText("foo"))))},
            {"@string{y1={foo bar}}", new BibTeXDatabase(List.of(new BibTeXString("y1", new BibTeXText("foo bar"))))},
            {
                "@string( y1  = \"foo \n  bar\" )",
                new BibTeXDatabase(List.of(new BibTeXString("y1", new BibTeXText("foo bar"))))
            },
            {
                "@string{xy42z= y1 # \"baz\"}",
                new BibTeXDatabase(
                    List.of(
                        new BibTeXString(
                            "xy42z",
                            new BibTeXConcatenation(new BibTeXIdentifier("y1"), new BibTeXText("baz"))
                        )
                    )
                )
            },
            {
                "@preamble{\\newcommand{\\foo}{}}",
                new BibTeXDatabase(List.of(new BibTeXPreamble("\\newcommand{\\foo}{}")))
            },
            {
                "@comment{@foo{bar,id=\"baz\"}}",
                new BibTeXDatabase(List.of(new BibTeXComment("@foo{bar,id=\"baz\"}")))
            }
        };
    }

    @Test(dataProvider="parseData")
    public void parseTest(final String fileText, final BibTeXDatabase expected) throws IOException {
        Assert.assertEquals(BibTeXParser.parse(new StringReader(fileText)), expected);
    }

}
