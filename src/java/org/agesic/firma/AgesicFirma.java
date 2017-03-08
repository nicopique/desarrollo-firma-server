
package org.agesic.firma;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Action;
import org.agesic.firma.dao.FirmaServerDAO;
import org.agesic.firma.datatypes.DataDocumento;
import org.agesic.firma.datatypes.ResultadoValidacion;
import org.agesic.firma.entities.Documentos;
import org.agesic.firma.utils.Constantes;
import org.agesic.firma.validaciones.util.FirmaValidate;
import org.apache.axiom.attachments.ByteArrayDataSource;

/**
 *
 * Web Services de Integración en Java.
 * @author sofis-solutions
 */
@WebService(name="AgesicFirma", serviceName = "AgesicFirmaWS", portName = "AgesicFirmaWSPort",  targetNamespace = "http://ws.firma.agesic.gub.uy/")
public class AgesicFirma {
    
    /**
     * Metodo invocado por el APPLET para obtener los documentos a Firmar.
     * Dado el número de transacción retorna el conjunto de documentos a firmar
     * @param idTransaccion el número de transacción
     * @return 
     */
    @WebMethod()
    @WebResult(name = "respuesta", targetNamespace = "http://ws.firma.agesic.gub.uy/")
    public List<byte[]> obtenerDocumentos(@WebParam(name = "id_transaccion",targetNamespace="http://ws.firma.agesic.gub.uy/")String idTransaccion) {
        List<byte[]> a = new ArrayList();
        try {
            FirmaServerDAO dao = new FirmaServerDAO();
            List<Documentos> docs = dao.obtenerDocumentosIdTransaction(idTransaccion);
            for (Documentos d: docs){
                a.add(d.getArchivo());
            }
        } catch (Exception w) {
            w.printStackTrace();
        }
        return a;
    }
    
/*    @WebMethod()
    @WebResult(name = "respuesta", targetNamespace = "http://ws.firma.agesic.gub.uy/")
    public List<DataDocumento> obtenerDocumentosFirmados(@WebParam(name = "id_transaccion",targetNamespace="http://ws.firma.agesic.gub.uy/")String idTransaccion) {
        List<DataDocumento> a = new ArrayList();
        try {
            FirmaServerDAO dao = new FirmaServerDAO();
            List<Documentos> docs = dao.obtenerDocumentosFirmadosIdTransaction(idTransaccion);
            for (Documentos d: docs){
                DataDocumento dd = new DataDocumento();
                dd.getDoc().add(d.getArchivo());
                dd.getCert().add(d.getCertificate());
                dd.setValid(d.getFirmaValida());
                a.add(dd);
            }
        } catch (Exception w) {
            w.printStackTrace();
        }

        return a;
    }
*/
    /**
     * Metodo invocado con el APPLET una vez que se firmaron los documentos de forma correcta.
     * @param idTransaccion id de transacción
     * @param documentos lista de documentos con la firma incorporada
     * @return 0 si se ejecutó de forma correcta, 1 si error. En caso de 1 se despliega un error general al usuario del Applet.
     */
    @WebMethod()
    @WebResult(name = "respuesta", targetNamespace = "http://ws.firma.agesic.gub.uy/")
    @Action(input = "http://ws.firma.agesic.gub.uy/AgesicFirmaWS/comunicarDocumentosRequest", 
            output = "http://ws.firma.agesic.gub.uy/AgesicFirmaWS/comunicarDocumentosResponse")
    public List<ResultadoValidacion> comunicarDocumentos(
            @WebParam(name = "id_transaccion", targetNamespace="http://ws.firma.agesic.gub.uy/")String idTransaccion, 
            @WebParam(name = "documentos", targetNamespace="http://ws.firma.agesic.gub.uy/")List<DataHandler> documentos,
            @WebParam(name = "certificate", targetNamespace="http://ws.firma.agesic.gub.uy/")byte[] certificate){
        
        FirmaServerDAO dao = new FirmaServerDAO();
        String tipoFirma = dao.obtenerTipoFirmaIdTransaction(idTransaccion);
        
        Integer result = 0;
        List ret = new ArrayList();
        //si es hash se necesita la firma para validar, implica cambiar el ws
        if (!Constantes.TIPO_FIRMA_HASH.equals(tipoFirma)){
            ret = FirmaValidate.validarFirma(documentos, tipoFirma, certificate, null);
            result = ret.isEmpty()?0:1;
        }
        dao.actualizarDocumentosIdTransaction(documentos, idTransaccion, tipoFirma, result == 0, certificate);
        return ret;
       
    }
    
  /*  @WebMethod()
    @WebResult(name = "respuesta", targetNamespace = "http://ws.firma.agesic.gub.uy/")
    public String firmarDocumentos(@WebParam(name = "tipo_firma",targetNamespace="http://ws.firma.agesic.gub.uy/") String tipoFirma,
                                   @WebParam(name = "documentos", targetNamespace="http://ws.firma.agesic.gub.uy/") List<byte[]> documentos) {
         String idTransaction = UUID.randomUUID().toString();
         FirmaServerDAO dao = new FirmaServerDAO();
         boolean ret = dao.registrarDocumentos(documentos, idTransaction, tipoFirma);
         return ret?idTransaction:null;
    }

    */
    

}
