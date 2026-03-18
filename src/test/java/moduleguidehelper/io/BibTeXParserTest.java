package moduleguidehelper.io;

import java.io.*;
import java.math.*;
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
            },
            {
                "@book{Laloux_2014,\n  author = {Laloux, Frederic},\n  title={Reinventing Organizations},\n  year = 2014}",
                new BibTeXDatabase(
                    List.of(
                        new BibTeXEntry(
                            "book",
                            "Laloux_2014",
                            Map.of(
                                "author", new BibTeXText("Laloux, Frederic"),
                                "title", new BibTeXText("Reinventing Organizations"),
                                "year", new BibTeXNumber(BigInteger.valueOf(2014))
                            )
                        )
                    )
                )
            },
            {
                "@misc{test,isbn=9783031862120}",
                new BibTeXDatabase(
                    List.of(
                        new BibTeXEntry(
                            "misc",
                            "test",
                            Map.of("isbn", new BibTeXNumber(new BigInteger("9783031862120")))
                        )
                    )
                )
            },
            {
                "@misc{test,number={007}}",
                new BibTeXDatabase(
                    List.of(new BibTeXEntry("misc", "test", Map.of("number", new BibTeXText("007"))))
                )
            }
        };
    }

    @DataProvider
    public Object[][] parseExceptionData() {
        return new Object[][] {
            {"@string{1x=\"foo\"}"},
            {"@stri@ng(x={foo})"},
            {"@string{x:{foo}}"},
            {"@string{y1={foo{ bar}}"},
            {"@string( y1  = \"foo \n  bar} )"},
            {"@string{xy 42z= y1 # \"baz\"}"},
            {"@ preamble{\\newcommand{\\foo}{}}"},
            {"@comment {@foo{bar,id=\"baz\"}}"},
            {"@book{Laloux_2014,\nauthor = {Laloux, Frederic}\ntitle={Reinventing Organizations},\nyear = 2014}"}
        };
    }

    @Test(dataProvider="parseExceptionData")
    public void parseExceptionTest(final String fileText) throws IOException {
        Assert.assertThrows(IOException.class, () -> BibTeXParser.parse(new StringReader(fileText)));
    }

    @Test(dataProvider="parseData")
    public void parseTest(final String fileText, final BibTeXDatabase expected) throws IOException {
        Assert.assertEquals(BibTeXParser.parse(new StringReader(fileText)), expected);
    }

}
