/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulobot.logs;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import modulobot.network.Linker;

/**
 *
 * @author Jordan
 */
public class GlobalHandler extends FileHandler {

    private Linker linker;
    public GlobalHandler(Linker linker, String filePath) throws IOException {
        super(filePath);
        super.setFormatter(new SimpleFormatter());
        this.linker = linker;
    }
    
    @Override
    public synchronized void publish(LogRecord record) {
        super.publish(record);
        linker.println(getFormatter().format(record));
    }
}
