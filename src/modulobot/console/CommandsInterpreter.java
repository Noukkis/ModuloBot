/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulobot.console;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import modulobot.Constantes;
import modulobot.bot.Bot;
import modulobot.logs.HTMLFormatter;
import modulobot.modules.Module;
import modulobot.network.Linker;

/**
 *
 * @author Jordan
 */
public class CommandsInterpreter implements Runnable {

    private Bot bot;
    private Linker linker;
    private boolean running;
    private static final Logger LOGGER = Logger.getLogger(CommandsInterpreter.class.getName());

    public CommandsInterpreter(Bot bot, Linker linker) {
        this.bot = bot;
        this.linker = linker;
        running = true;
        try {
            Handler handler = new FileHandler(Constantes.LOGS_FOLDER  + "commands.log");
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
        } catch (IOException | SecurityException ex) {
            Logger.getGlobal().log(Level.SEVERE, "Can't handle command logger into commands.log file", ex);
        }
    }

    @Override
    public void run() {
        while (running) {
            String[] command = splitCommand(linker.readLine());
            String s = "";
            switch (command[0].toLowerCase()) {
                case "help":
                    s = getHelp(command[1]);
                    break;
                case "reload":
                    bot.reloadModules();
                    break;
                case "list":
                    s = bot.listModules();
                    break;
                case "loadnews":
                    s = bot.loadnews();
                    break;
                case "log":
                        s = bot.log(command[1]);
                    break;
                case "shutdown":
                    s = bot.shutdown();
                    running = false;
                    break;
                case "stop":
                    if (command[1] != null) {
                        s = bot.stopModule(command[1]);
                    } else {
                        s = "bad use of command";
                    }
                    break;
                default:
                    s = searchForModuleCommand(command);
                    break;
            }
            linker.println(s);
        }
    }

    private String getHelp(String s) {
        if (s == null) {
            return "help";
        }
        for (Module module : bot.getListeners().getListeners(Module.class)) {
            if (module.getName().equalsIgnoreCase(s) && module.getModuleCtrl() != null) {
                return module.getModuleCtrl().getHelp();
            }
        }
        return "No help for this module";
    }

    private String searchForModuleCommand(String[] command) {
        ArrayList<Module> modules = new ArrayList(Arrays.asList(bot.getListeners().getListeners(Module.class)));
        for (Module module : modules) {
            if (module.getName().replaceAll(" ", "").equalsIgnoreCase(command[0]) && module.getModuleCtrl() != null) {
                String[] moduleCommand = splitCommand(command[1]);
                try {
                    Object o = module.getModuleCtrl().getClass().getMethod(moduleCommand[0], String.class).invoke(module.getModuleCtrl(), moduleCommand[1]);
                    return o instanceof String ? (String) o : "";
                } catch (Exception ex) {
                    return "No method with such name in the \"" + module.getName() + "\" module";
                }
            }
        }
        return "Not an existing command, type \"help\" for commands list";
    }

    private String[] splitCommand(String s) {
        if(s == null){
            return new String[2];
        }
        String[] command = new String[2];
        command[0] = s.split(" ")[0];
        if (s.contains(" ")) {
            command[1] = s.substring(s.indexOf(' ')).replaceFirst(" ", "");
        }
        return command;
    }

}
