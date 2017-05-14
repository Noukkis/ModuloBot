/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modulobot;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import modulobot.modules.ModuleHelper;
import javax.security.auth.login.LoginException;
import modulobot.bot.Bot;
import modulobot.logs.GlobalHandler;
import modulobot.network.Linker;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.AccountTypeException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

/**
 *
 * @author Jordan
 */
public class ModuloBot {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean ok = false;
        while (!ok) {
            Linker linker = null;
            try {
                System.out.println("Choose the port for remote connection :");
                int port = new Scanner(System.in).nextInt();
                linker = new Linker(port);
                ok = linker.tryToConnect(true) && createBot(linker.readLine(), linker);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Failed to connect");
            }
        }
    }

    private static boolean createBot(String token, Linker linker) {
        new File(Constantes.LOGS_FOLDER).mkdirs();
        new File(Constantes.MODULES_LOGS_FOLDER).mkdirs();
        try {
            Logger.getGlobal().addHandler(new GlobalHandler(linker, Constantes.LOGS_FOLDER +"global.log"));
        } catch (IOException | SecurityException ex) {
            Logger.getGlobal().log(Level.SEVERE, "Can't create Global Handler", ex);
        }
        boolean ok = false;
        try {
            JDA jda = new JDABuilder(AccountType.BOT).setToken(token).buildBlocking();
            linker.println("JDA built");
            Bot bot = new Bot(new ModuleHelper(jda), linker);
            bot.launch();
            jda.addEventListener(bot);
            ok = true;
        } catch (LoginException | IllegalArgumentException | AccountTypeException e) {
            System.out.println("The given token is invalid");
        } catch (InterruptedException | RateLimitedException e) {
            System.out.println("An unexpected error occured on Bot's loading");
        }
        return ok;
    }

}
