/**
 *
 * @author sofis-solutions
 */
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
import org.agesic.firma.config.ConfigurationUtil;
import org.agesic.firma.dao.FirmaServerDAO;
import org.agesic.firma.datatypes.DataDocumento;
import org.agesic.firma.datatypes.ResultadoValidacion;
import org.agesic.firma.entities.Documentos;
import org.agesic.firma.validaciones.util.FirmaValidate;
import org.apache.axiom.attachments.ByteArrayDataSource;
import uy.gub.agesic.firma.FirmaAgesic;
import uy.gub.agesic.firma.TipoDocumento;

/**
 *
 * Web Services de Integración en Java.
 *
 * @author sofis-solutions
 */
@WebService(name = "AgesicFirmaServer", serviceName = "AgesicFirmaServerWS", portName = "AgesicFirmaServerWSPort", targetNamespace = "http://ws.firma.agesic.gub.uy/")
public class AgesicFirmaServer {

    private ConfigurationUtil config = new ConfigurationUtil(null);

    @WebMethod()
    @WebResult(name = "respuesta", targetNamespace = "http://ws.firma.agesic.gub.uy/")
    public List<DataDocumento> obtenerDocumentosFirmados(@WebParam(name = "id_transaccion", targetNamespace = "http://ws.firma.agesic.gub.uy/") String idTransaccion) {
        List<DataDocumento> a = new ArrayList();
        try {
            FirmaServerDAO dao = new FirmaServerDAO();
            List<Documentos> docs = dao.obtenerDocumentosFirmadosIdTransaction(idTransaccion);
            for (Documentos d : docs) {
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

    @WebMethod()
    @WebResult(name = "respuesta", targetNamespace = "http://ws.firma.agesic.gub.uy/")
    public String firmarDocumentos(@WebParam(name = "tipo_firma", targetNamespace = "http://ws.firma.agesic.gub.uy/") String tipoFirma,
            @WebParam(name = "documentos", targetNamespace = "http://ws.firma.agesic.gub.uy/") List<byte[]> documentos) {
        String idTransaction = UUID.randomUUID().toString();
        FirmaServerDAO dao = new FirmaServerDAO();
        boolean ret = dao.registrarDocumentos(documentos, idTransaction, tipoFirma);
        return ret ? idTransaction : null;
    }

    @WebMethod()
    @WebResult(name = "respuesta", targetNamespace = "http://ws.firma.agesic.gub.uy/")
    public List<DataDocumento> firmarDocumentosServidor(@WebParam(name = "tipo_firma", targetNamespace = "http://ws.firma.agesic.gub.uy/") String tipoFirma,
            @WebParam(name = "documentos", targetNamespace = "http://ws.firma.agesic.gub.uy/") List<byte[]> documentos, @WebParam(name = "keys", targetNamespace = "http://ws.firma.agesic.gub.uy/") String keys) {

        List<DataDocumento> a = new ArrayList();
        HashMap opcionesFirma = new HashMap();
        try {
            StringTokenizer st = new StringTokenizer(keys, ",");
            FirmaAgesic firmaAgesic = new FirmaAgesic();
            while (st.hasMoreTokens()) {
                String p12 = st.nextToken();
                //obtiene el keystore a partir del p12
                if (config.contains(p12)) {
                    String keyStore = config.getValue(p12);
                    String password = config.getValue(p12 + "_pass");
                    String alias = config.getValue(p12 + "_alias");
                    if (alias != null && !alias.equals("")) {
                        opcionesFirma.put("alias", alias);
                    }
                    int idOpcion = firmaAgesic.inicializarP12(keyStore, password, opcionesFirma);
                    byte[] docFirmado = firmaAgesic.firmaDocumento(idOpcion, TipoDocumento.PDF, documentos.get(0), null);
                    DataDocumento dd = new DataDocumento();
                    dd.getDoc().add(docFirmado);
                    dd.getCert().add(null);
                    dd.setValid(true);
                    a.add(dd);
                }
            }

        } catch (Exception w) {
            w.printStackTrace();
        }
        return a;
    }
}
