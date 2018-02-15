package almanza1112.spottrade;

/**
 * Created by almanza1112 on 10/27/17.
 */

class BuyerTracker {

    private String key;
    private double lat, lng;

    BuyerTracker(){

    }

    public void setKey(String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }

    double getLat() {
        return lat;
    }

    void setLat(double lat) {
        this.lat = lat;
    }

    double getLng() {
        return lng;
    }

    void setLng(double lng) {
        this.lng = lng;
    }
}
