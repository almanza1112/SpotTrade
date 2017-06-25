package almanza1112.spottrade.nonActivity;

/**
 * Created by almanza1112 on 6/21/17.
 */

public class HttpConnection {
    //linux command for finding IP = ifconfig -a
    private final String localURL = "http://192.168.1.8";
    private final String localPort = "3000";

    public String htppConnectionURL(){
        return localURL + ":" + localPort;
    }
}
