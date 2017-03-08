/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.agesic.firma.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import org.agesic.firma.entities.Documentos;
import org.apache.commons.io.IOUtils;




/**
 *
 * @author usuario
 */

public class FirmaServerDAO {
    static EntityManagerFactory factory = null;
    private EntityManager em;


    static {
        factory = Persistence.createEntityManagerFactory("AgesicFirmaWSPU");
    }

    public FirmaServerDAO() {
        em = factory.createEntityManager();
    }
    
    public void check(){
        if (em == null || !em.isOpen()){
             em = factory.createEntityManager();
        }
    }

    public boolean registrarDocumentos(List<byte[]> docs, String idTransaction, String tipoFirma) {
        System.out.println("registrarDocumentos");
        try {
            check();
            em.getTransaction().begin();
            
            
            for (byte[] d: docs){
                
                Documentos doc = new Documentos();
                System.out.println("d.length: "+d.length);
                doc.setArchivo(d);
                doc.setFirmado(false);
                doc.setFirmaValida(false);
                doc.setIdTransaction(idTransaction);
                doc.setFechaModif(new Date());
                doc.setTipoFirma(tipoFirma);
                em.persist(doc);
            }
            em.getTransaction().commit();
            return true;
        } catch (Exception ex) {
            Logger.getLogger(FirmaServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
       
    }
    
    public void deleteExpiredData(long time){
        System.out.println("deleteExpiredData");
        Date limitDate = new Date(new Date().getTime() - time);
        try {
            check();
            em.getTransaction().begin();
            String q = "delete from Documentos d where d.fechaModif < :date";
            em.createQuery(q).setParameter("date", limitDate).executeUpdate();
            em.getTransaction().commit();
        } catch (Exception ex) {
            Logger.getLogger(FirmaServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
        } finally {
            em.close();
        }
        
        
    }
    
    public boolean actualizarDocumentosIdTransaction(List<DataHandler> docs, String idTransaction, String tipoFirma, boolean firmaValida, byte[] certificate) {
        System.out.println("actualizarDocumentosIdTransaction");
        try {
            
            check();
            em.getTransaction().begin();
            
            String q = "delete from Documentos d where d.idTransaction = '"+idTransaction+"'";
            em.createQuery(q).executeUpdate();
            
            for (DataHandler d: docs){
                Documentos doc = new Documentos();
                doc.setArchivo(IOUtils.toByteArray(d.getInputStream()));
                doc.setFirmado(true);
                doc.setCertificate(certificate);
                doc.setFirmaValida(firmaValida);
                doc.setIdTransaction(idTransaction);
                doc.setFechaModif(new Date());
                doc.setTipoFirma(tipoFirma);
                em.persist(doc);
            }
            em.getTransaction().commit();
            
            return true;
        } catch (Exception ex) {
            Logger.getLogger(FirmaServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
       
    }
    
    public List<Documentos> obtenerDocumentosIdTransaction(String idTransaction) {
        System.out.println("obtenerDocumentosIdTransaction " + idTransaction);
        List<Documentos> ret = new ArrayList<Documentos>();
        try {
            check();
            em.getTransaction().begin();
            String q = "select d from Documentos d where d.idTransaction = '"+idTransaction+"'";
            ret = em.createQuery(q).getResultList();
            System.out.println("obtenerDocumentosIdTransaction " + ret.size());
        } catch (Exception ex) {
            Logger.getLogger(FirmaServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
        } finally {
            em.close();
        }
        
        return ret;
       
    }
    
    public List<Documentos> obtenerDocumentosFirmadosIdTransaction(String idTransaction) {
        System.out.println("obtenerDocumentosIdTransaction");
        List<Documentos> ret = new ArrayList<Documentos>();
        try {
            check();
            em.getTransaction().begin();
            String q = "select d from Documentos d where d.idTransaction = '"+idTransaction+"' and d.firmado = true";
            ret = em.createQuery(q).getResultList();
            
            q = "delete from Documentos d where d.idTransaction = '"+idTransaction+"' and d.firmado = true";
            em.createQuery(q).executeUpdate();
            
            em.getTransaction().commit();
           
         
        } catch (Exception ex) {
            Logger.getLogger(FirmaServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
        } finally {
            em.close();
        }
        
        return ret;
       
    }
    
    public String obtenerTipoFirmaIdTransaction(String idTransaction) {
        
        try {
            
            check();
            String q = "select distinct d.tipoFirma from Documentos d where d.idTransaction = '"+idTransaction+"'";
            return (String)em.createQuery(q).getSingleResult();
          
        } catch (Exception ex) {
            Logger.getLogger(FirmaServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
        } finally {
            em.close();
        }
        
        return "";
        
        
       
    }

    
    
}
