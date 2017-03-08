/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.agesic.firma.utils;

import java.util.ResourceBundle;
import org.agesic.firma.config.ConfigurationUtil;
import org.agesic.firma.dao.FirmaServerDAO;


/**
 *
 * @author usuario
 */
public class BDThread extends Thread{
    
    private ConfigurationUtil config = new ConfigurationUtil(null);
    private static final String TIME_RUN_CLEAN_BD_MS = "TIME_RUN_CLEAN_BD_MS";
    private static final String CLEAN_EXPIRED_AFTER = "CLEAN_EXPIRED_AFTER";
    
    
    @Override
    public void run(){
        System.out.println("run clean");
        FirmaServerDAO dao = new FirmaServerDAO();
        try{
            while (true){
                try{
                    System.out.println("do run clean");
                    
                    dao.deleteExpiredData(Long.valueOf(config.getValue(CLEAN_EXPIRED_AFTER)));
                    Thread.sleep(config.getIntValue(TIME_RUN_CLEAN_BD_MS));
                } catch (Exception ex) {
                    
                    Thread.sleep(config.getIntValue(TIME_RUN_CLEAN_BD_MS));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        
    }
}
