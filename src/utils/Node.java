package utils;

public class Node {

    public Node(String hostname, String ipAddress){
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

    private String hostname;
    private String ipAddress;

}
