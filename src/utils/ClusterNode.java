package utils;

import java.io.Serializable;

public class ClusterNode implements Serializable {

    public ClusterNode(String hostname){
        this.hostname = hostname;
    }
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return this.hostname;
    }

    private String hostname;

}
