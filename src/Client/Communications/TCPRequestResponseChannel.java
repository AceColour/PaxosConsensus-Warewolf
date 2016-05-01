package Client.Communications;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Kelas ini dapat mem-filter mana yang request dan response
 * Created by nim_13512501 on 4/30/16.
 */
public class TCPRequestResponseChannel extends Thread{

    Socket socket;

    boolean hasTakenARequestButNotYetResponded;
    BlockingQueue<JSONObject> requestQueue;

    BlockingQueue<JSONObject> responseQueue;

    public TCPRequestResponseChannel(Socket socket){
        this.socket = socket;
        requestQueue = new LinkedBlockingQueue<JSONObject>();
        responseQueue = new LinkedBlockingQueue<JSONObject>();
        hasTakenARequestButNotYetResponded = false;
    }

    public TCPRequestResponseChannel(InetAddress addressSana, int portSana) throws IOException {
        System.out.println("Connecting to server...");
        this.socket = new Socket(addressSana, portSana);
        requestQueue = new LinkedBlockingQueue<JSONObject>();
        responseQueue = new LinkedBlockingQueue<JSONObject>();
        hasTakenARequestButNotYetResponded = false;
    }

    /**
     * akan blocking hingga mendapat jawaban
     * ini hanya boleh dipanggil oleh satu thread
     * @param request JSONObject yang mengandung field method
     * @return response dari sana
     */
    public JSONObject sendRequestAndGetResponse(JSONObject request) throws IOException, InterruptedException {
        if (!request.containsKey("method"))
            throw new IllegalArgumentException("NO_METHOD_FIELD");
        sendJSON(request);

        return responseQueue.take();
    }

    /**
     * @return true jika ada request dari seberang sana
     */
    public boolean adaRequestDariSeberangSana(){
        return !requestQueue.isEmpty();
    }

    /**
     * method ini akan blocking hingga dapat request
     * setelah dipanggil, tidak boleh dipanggil lagi hingga menjawab request yang diambil di sini
     * dengan memanggil sendResponseKeSeberangSana()
     * @return request yang terakhir didapat
     */
    public JSONObject getLastRequestDariSeberangSana() throws InterruptedException {
        JSONObject response = requestQueue.take();
        hasTakenARequestButNotYetResponded = true;
        return response;
    }

    /**
     * method ini mengirimkan respon ke sana
     * setelah method ini dipanggil, getLastRequestDariSeberangSana() menjadi boleh dipanggil juga
     */
    public void sendResponseKeSeberangSana(JSONObject response) throws IOException {
        sendJSON(response);
        hasTakenARequestButNotYetResponded = false;
    }

    @Override
    public void run() {
        try {
            Reader in = new InputStreamReader(socket.getInputStream());

            String message = "";
            JSONParser jsonParser = new JSONParser();

            while (!interrupted()){
                message += (char) in.read();
                try {
                    JSONObject jsonObject = (JSONObject) jsonParser.parse(message);

                    if (jsonObject.containsKey("status")){
                        responseQueue.put(jsonObject);
                    }else if (jsonObject.containsKey("method")){
                        requestQueue.put(jsonObject);
                    }else{
                        //TODO kirim error
                    }
                    message = "";
                } catch (ParseException e) {
                } catch (InterruptedException e) {
                } catch (ClassCastException e){
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendJSON(JSONObject jsonObject) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        out.println(jsonObject.toJSONString());

        out.flush();
    }
}
