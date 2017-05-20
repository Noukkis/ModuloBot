/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulobot.bot;

import java.util.ArrayList;
import java.util.Arrays;
import modulobot.modules.Module;

/**
 *
 * @author Jordan
 */
public class Commands {

    Bot bot;
    ArrayList<String> possibles;

    public Commands(Bot bot) {
        this.bot = bot;
        String[] arrayOfPossibleCommands = new String[]{"help", "help .+", "list"};
        possibles = new ArrayList<>(Arrays.asList(arrayOfPossibleCommands));
    }

    public String interpret(String command) {
        String s = command.substring(bot.getModuleHelper().getPrefix().length() * 2);
        int num = correct(s);
        String res = "";
        switch (num) {
            case 0:
                res += getHelp();
                break;
            case 1:
                res += getHelp(s.substring(5));
                break;
            case 2:
                res += bot.listModules();
                break;
            default:
                return "This command doesn't exist";
        }
        return res;
    }

    private int correct(String s) {
        for (String regex : possibles) {
            if (s.matches(regex)) {
                return possibles.indexOf(regex);
            }
        }
        return -1;
    }

    private String getHelp() {
        String prefix = bot.getModuleHelper().getPrefix() + bot.getModuleHelper().getPrefix();
        String help = "Type " + prefix + "help *moduleName* to get help on the module you want\n"
                + "Type " + prefix + "list to list every working modules";
        return help;
    }

    private String getHelp(String name) {
        String res = "";
        for (Module module : bot.getModules()) {
            if (name.equals(module.getName()) || name.equals(module.getClass().getName())) {
                res += "\n Help for " + name + "\n" + module.getHelp() + "\n";
            }
        }
        if(res.equals("")){
            res = "No module found";
        }
        return res;
    }
}
