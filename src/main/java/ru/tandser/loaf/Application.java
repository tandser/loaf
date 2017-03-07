package ru.tandser.loaf;

import java.io.InputStream;

public class Application {

    private static final String FILE_1 = "1.xml";
    private static final String FILE_2 = "2.xml";
    private static final String FILE_3 = "3.cvs";

    public static void main(String[] args) throws Exception {
        Core core = new Core("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
        InputStream stylesheet = Application.class.getResourceAsStream("/articles.xsl");
        core.retrieve(FILE_1);
        core.convert(stylesheet, FILE_1, FILE_2);
        core.fromXmlToCvs(FILE_2, FILE_3);
        core.dispose();
    }
}