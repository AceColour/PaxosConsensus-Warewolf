package Client.Communications;

import org.json.simple.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Kelas ini dapat mem-filter mana yang request dan response
 * Created by nim_13512501 on 4/30/16.
 */
public class TCPRequestResponseChannel implements Runnable{

    Socket socket;

    public TCPRequestResponseChannel(Socket socket){
        this.socket = socket;
    }

    public TCPRequestResponseChannel(InetAddress addressSana, int portSana) throws IOException {
        this.socket = new Socket(addressSana, portSana);
    }

    public JSONObject sendRequestAndGetResponse(JSONObject request){
        throw new NotImplementedException();
    }

    /**
     * @return true jika ada request dari seberang sana
     */
    public boolean adaRequestDariSeberangSana(){
        throw new NotImplementedException();
    }

    /**
     * method ini akan blocking hingga dapat request
     * setelah dipanggil, tidak boleh dipanggil lagi hingga menjawab request yang diambil di sini
     * dengan memanggil sendResponseKeSeberangSana()
     * @return request yang terakhir didapat
     */
    public JSONObject getLastRequestDariSeberangSana(){
        throw new NotImplementedException();
    }

    /**
     * method ini mengirimkan respon ke sana
     * setelah method ini dipanggil, getLastRequestDariSeberangSana() menjadi boleh dipanggil juga
     */
    public void sendResponseKeSeberangSana(JSONObject response){
        throw new NotImplementedException();
    }

    @Override
    public void run() {
        throw new NotImplementedException();
    }
}
