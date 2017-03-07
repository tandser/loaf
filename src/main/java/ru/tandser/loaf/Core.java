package ru.tandser.loaf;

import lombok.Getter;
import lombok.Setter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Core implements Serializable {

    public static final String SELECT = "SELECT id_art, name, code, username, guid FROM article LIMIT 9999";

    @Getter @Setter private String url;
    @Getter @Setter private String username;
    @Getter @Setter private String password;

    private transient Connection connection;

    public Core(String url, String username, String password) {
        this.url      = url;
        this.username = username;
        this.password = password;
    }

    private void printExceptionMessage(Exception exc) {
        if (exc.getMessage() != null && !exc.getMessage().isEmpty()) {
            System.err.println(exc.getMessage());
        }
    }

    public void retrieve(String filename) throws CoreException {
        try {
            connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
            connection.setReadOnly(true);
            try (Statement statement = connection.createStatement();
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filename))) {
                ResultSet resultSet = statement.executeQuery(SELECT);
                XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance()
                        .createXMLStreamWriter(outputStream, "UTF-8");
                xmlStreamWriter.writeStartDocument("UTF-8", "1.0");
                xmlStreamWriter.writeStartElement("articles");
                while (resultSet.next()) {
                    xmlStreamWriter.writeEmptyElement("article");
                    xmlStreamWriter.writeAttribute("id_art",   resultSet.getString("id_art"));
                    xmlStreamWriter.writeAttribute("name",     resultSet.getString("name"));
                    xmlStreamWriter.writeAttribute("code",     resultSet.getString("code"));
                    xmlStreamWriter.writeAttribute("username", resultSet.getString("username"));
                    xmlStreamWriter.writeAttribute("guid",     resultSet.getString("guid"));
                }
                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.writeEndDocument();
                xmlStreamWriter.close();
            }
        } catch (Exception exc) {
            printExceptionMessage(exc);
            throw new CoreException();
        } finally {
            if (connection != null) {
                try {
                    connection.setReadOnly(false);
                } catch (Exception exc) {
                    printExceptionMessage(exc);
                }
            }
        }
    }

    public void convert(InputStream stylesheet, String input, String output) throws CoreException {
        try {
            Transformer transformer = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
                    .newTransformer(new StreamSource(stylesheet));
            transformer.transform(new StreamSource(input), new StreamResult(Paths.get(output).toFile()));
        } catch (Exception exc) {
            printExceptionMessage(exc);
            throw new CoreException();
        }
    }

    public void fromXmlToCvs(String input, String output) {
        try {
            new DefaultHandler() {

                private static final String ARTICLES  = "articles";
                private static final String ARTICLE   = "article";
                private static final String ID_ART    = "id_art";
                private static final String NAME      = "name";
                private static final String CODE      = "code";
                private static final String USERNAME  = "username";
                private static final String GUID      = "guid";
                private static final char   SEPARATOR = ';';

                private PrintWriter printWriter;

                public void parse() throws Exception {
                    SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                    saxParser.parse(new BufferedInputStream(new FileInputStream(input)), this);
                }

                @Override
                public void startDocument() throws SAXException {
                    try {
                        printWriter = new PrintWriter(new BufferedWriter(new FileWriter(output)));
                    } catch (Exception exc) {
                        printExceptionMessage(exc);
                        throw new CoreException();
                    }
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    printWriter.print(String.copyValueOf(ch, start, length));
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    switch (qName) {
                        case ARTICLES :
                        case ARTICLE  : break;
                        case GUID     : printWriter.println();
                                        break;
                        default       : printWriter.print(SEPARATOR);
                    }
                }

                @Override
                public void endDocument() throws SAXException {
                    printWriter.close();
                }
            }.parse();
        } catch (Exception exc) {
            printExceptionMessage(exc);
            throw new CoreException();
        }
    }

    public void dispose() throws CoreException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception exc) {
            printExceptionMessage(exc);
            throw new CoreException();
        }
    }
}