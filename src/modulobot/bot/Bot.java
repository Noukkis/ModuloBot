/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulobot.bot;

import modulobot.modules.ModuleHelper;
import modulobot.modules.Module;
import modulobot.console.CommandsInterpreter;
import modulobot.events.PrefixedMessageReceivedEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import modulobot.Constantes;
import modulobot.Helper;
import modulobot.network.Linker;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 *
 * @author Jordan
 */
public class Bot implements EventListener {

    private EventListenerList listeners;
    private ModuleHelper moduleHelper;
    private Commands cmd;
    private Linker linker;

    private final static Logger LOGGER = Logger.getGlobal();

    public Bot(ModuleHelper moduleCtrl, Linker linker) {
        listeners = new EventListenerList();
        this.moduleHelper = moduleCtrl;
        cmd = new Commands(this);
        this.linker = linker;
        LOGGER.info("Bot created");
    }

    private void loadModules() {
        ArrayList<Module> modules = new ArrayList<>();
        File directory = new File("modules");
        if (!directory.isDirectory()) {
            linker.println("modules directory not found");
            System.exit(1);
        } else {
            ArrayList<URL> urls = new ArrayList();
            for (File f : directory.listFiles()) {
                if (f.getName().endsWith(".jar")) {
                    try {
                        urls.add(f.toURI().toURL());
                    } catch (MalformedURLException ex) {
                        linker.println("modules directory not found");
                    }
                }
            }

            ClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]));

            for (URL url : urls) {
                File f = new File(url.getFile());
                try {
                    JarFile jar = new JarFile(f);
                    for (String name : getClasses(jar)) {
                        try {
                            if (Arrays.asList(Class.forName(name, true, loader).getSuperclass()).contains(Module.class)) {
                                try {
                                    modules.add((Module) Class.forName(name, true, loader).newInstance());
                                } catch (InstantiationException ex) {
                                    linker.println("The " + name + " class of " + f.getName() + " is not instanciable");
                                } catch (IllegalAccessException ex) {
                                    linker.println("The " + name + " class of " + f.getName() + " is not accessible");
                                }
                            }
                        } catch (ClassNotFoundException ex) {

                        }
                    }

                } catch (IOException ex) {
                    linker.println("Error while loading " + f.getName());

                }

            }

            for (Module module : modules) {
                if (!Arrays.asList(listeners.getListeners(Module.class)).contains(module)) {
                    try {
                        module.preload(moduleHelper);
                        linker.println(module.getClass().getName() + " loaded");
                        listeners.add(Module.class, module);
                    } catch (Exception e) {
                        listeners.remove(Module.class, module);
                        linker.println("The " + module.getClass().getName() + " module can't be loaded (preload error)");
                    }
                } else {
                    linker.println(module.getName() + " already loaded");
                }
            }
        }
    }

    public void launch() {
        loadModules();
        new Thread(new CommandsInterpreter(this, linker)).start();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof MessageReceivedEvent && ((MessageReceivedEvent) event).getMessage().getContent().startsWith(moduleHelper.getPrefix() + moduleHelper.getPrefix())
                && !((MessageReceivedEvent) event).getAuthor().isBot()) {
            ((MessageReceivedEvent) event).getChannel().sendMessage(cmd.interpret(((MessageReceivedEvent) event).getMessage().getContent())).queue();
        } else if (event instanceof MessageReceivedEvent && ((MessageReceivedEvent) event).getMessage().getContent().startsWith(moduleHelper.getPrefix())
                && !((MessageReceivedEvent) event).getAuthor().isBot()) {
            PrefixedMessageReceivedEvent newEvent = new PrefixedMessageReceivedEvent((MessageReceivedEvent) event, moduleHelper.getPrefix());
            for (Module module : listeners.getListeners(Module.class)) {
                module.onPrefixedMessageReceived(newEvent);
            }
        } else {
            for (Module module : listeners.getListeners(Module.class)) {
                module.onEvent(event);
            }
        }
    }

    private ArrayList<String> getClasses(JarFile jar) {
        Enumeration<JarEntry> en = jar.entries();
        ArrayList<String> res = new ArrayList<>();
        while (en.hasMoreElements()) {
            String name = en.nextElement().getName();
            if (name.endsWith(".class")) {
                res.add(name.replace(".class", "").replace('/', '.').replace('\\', '.'));
            }
        }
        return res;
    }

    public void reloadModules() {
        for (Module module : listeners.getListeners(Module.class)) {
            module.stop();
        }
        listeners = new EventListenerList();
        loadModules();
    }

    public String listModules() {
        String list = "\n----------------------\n\n";
        for (Module module : listeners.getListeners(Module.class)) {
            list += module.getName() + " - " + module.getClass().getName() + "\n" + module.getDescription() + "\n\n";
        }
        list += "----------------------\n";
        return list;
    }

    public String stopModule(String string) {
        boolean exist = false;
        String res = "";
        for (Module module : listeners.getListeners(Module.class)) {
            if (module.getClass().getName().equals(string)) {
                exist = true;
                module.stop();
                listeners.remove(Module.class, module);
                res = string + " stopped";
                break;
            }
        }
        if (!exist) {
            res = string + " not found";
        }
        return res;
    }

    public ModuleHelper getModuleHelper() {
        return moduleHelper;
    }

    public EventListenerList getListeners() {
        return listeners;
    }

    public String shutdown() {
        for (Module module : listeners.getListeners(Module.class)) {
            module.stop();
            linker.println(module.getName() + " stopped");
        }
        listeners = new EventListenerList();
        moduleHelper.getJda().shutdown();
        return "Shutted down";
    }

    public String loadnews() {
        loadModules();
        return "load complete";
    }

    public String log(String s) {
        String path = Constantes.LOGS_FOLDER + ((s != null) ? s : "");
        File f = new File(path);
        String res = "Not a file or a directory";
        if (f.isFile() && f.getName().endsWith(".log")) {
            res = "\n-----------------------\n";
            res += Helper.readFile(f);
            res += "\n\n-----------------------";
        } else if (f.isDirectory()){
            res = "Logs and directories in " + path + " :";
            for (File file : f.listFiles()) {
                    res += "\n" + file.getName();
            }
        }
        return res;
    }
    
    public String setPrefix(String s){
        moduleHelper.setPrefix(s);
        return "Prefix changed to " + s;
    }
}
