package almanza1112.spottrade.nonActivity;

/**
 * Created by almanza1112 on 6/21/17.
 */

public class HttpConnection {
    public String htppConnectionURL(){
        String localURL = "http://192.168.1.9";
        String localPort = "3000";
        return localURL + ":" + localPort;
    }
}
