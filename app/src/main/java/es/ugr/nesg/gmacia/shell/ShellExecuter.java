package es.ugr.nesg.gmacia.shell;

/**
 * Created by gmacia on 08/02/2018.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellExecuter {

    public String Executer(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        String response = output.toString();
        /* A partir de la versión 3.0, el envío de mensajes por red debe hacerse en una hebra separada
           Por este motivo creo la clase con la interfaz runnable, que permite enviar los datos al servidor MDSM.
         */
        new Thread(new MDSMmServerConnection(response)).start();
        return response;

    }





}