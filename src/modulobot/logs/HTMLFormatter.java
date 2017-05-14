/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulobot.logs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Jordan
 */
public class HTMLFormatter extends Formatter {

    private final Date dat = new Date();
    private final String title;

    public HTMLFormatter(String title) {
        this.title = title;
    }

    @Override
    public String getHead(Handler h) {
        return "<!DOCTYPE HTML> <html lang=\"fr\"> <head> <meta charset=\"utf-8\"/> <title>" + title + "</title> <style> table { border-collapse: collapse; } td { border: 1px solid black; } th { border: 1px solid black; background-color: #dddddd; } .severe { background-color: #c60000; } .warning { background-color: #ffe842; } .stacktrace { color: red; } </style> </head> <body> <h1>" + title + "</h1> <table> <tr> <th>Date</th> <th>Level</th> <th>Methode</th> <th>Classe</th> </tr>";
    }

    @Override
    public String format(LogRecord record) {
        dat.setTime(record.getMillis());
        String level = record.getLevel().getName();
        String classe = (record.getSourceClassName() != null) ? record.getSourceClassName() : "-";
        String method = (record.getSourceMethodName() != null) ? record.getSourceMethodName() : "-";
        String message = formatMessage(record);

        String res = "<tr class=\"" + level + "\">";
        res += "<td>" + dat + "</td> <td>" + level + "</td> <td>" + method + "</td> <td>" + classe + "<td>" + message + "</td> </tr>";
        if (record.getThrown() != null) {
            String throwable = "";
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
            res += "<tr> <td colspan=5 class=\"stacktrace\">" + throwable + "</td>";
        }
        return res;
    }

    @Override
    public String getTail(Handler h) {
        return "</table> <footer> <hr/> <p>Crée par Noukkis</p> </footer> </body> </html>";
    }

}
