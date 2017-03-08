/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.agesic.firma.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.agesic.firma.utils.BDThread;

/**
 *
 * @author usuario
 */
public class OnStartListener implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("starting...");
        new BDThread().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
    
}
