package almanza1112.spottrade;

/**
 * Created by almanza1112 on 10/27/17.
 */

public class BuyerTracker {

    private String lat, lng, key;

    public BuyerTracker(){

    }

    public void setKey(String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
