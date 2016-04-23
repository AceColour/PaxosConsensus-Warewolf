package Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by nim_13512501 on 4/23/16.
 *
 * SynchronousTCPJSONClient membungkus operasi mengirim request dan menerima respons dalam bentuk JSON
 *
 * SynchronousTCPJSONClient mengirim request kepada server, menerima respons dan menutup koneksi.
 *
 * SynchronousTCPJSONClient melakukan request-response secara sinkronus, artinya thread akan ter-blok hingga response
 * diterima
 *
 * Asumsi server menutup koneksi setelah mengirimkan response
 */
public class SynchronousTCPJSONClient {
    Socket serverSocket;
    InetSocketAddress serverAddress;

    public SynchronousTCPJSONClient (InetSocketAddress serverAddress){
        serverSocket = new Socket();
        this.serverAddress = serverAddress;
    }

    public void connect() throws IOException {
        serverSocket.connect(serverAddress);
    }

    public JSONObject call(JSONObject request) throws IOException, ParseException {
        PrintWriter printWriter= new PrintWriter(serverSocket.getOutputStream());
        printWriter.println(request.toJSONString());
        printWriter.flush();
        serverSocket.getOutputStream().flush();

        JSONParser jsonParser = new JSONParser();
        JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(serverSocket.getInputStream()));
        return response;
    }

    public boolean isClosed(){
        return serverSocket.isClosed();
    }
}
