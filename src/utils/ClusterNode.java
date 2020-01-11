package utils;

import java.io.Serializable;

public class ClusterNode implements Serializable {

    public ClusterNode(String hostname, String ipAddress){
        this.hostname = hostname;
        this.ipAddress = ipAddress;
    }
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return this.hostname;
    }

    private String hostname;
    private String ipAddress;

}
