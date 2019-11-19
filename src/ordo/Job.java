package ordo;

import formats.Format;
import map.MapReduce;

public class Job implements JobInterface {
    private Format.Type inputFormat;
    private String inputFname;

    @Override
    public void setInputFormat(Format.Type ft) {
        this.inputFormat = ft;
    }

    @Override
    public void setInputFname(String fname) {
        this.inputFname = fname;
    }

    @Override
    public void startJob(MapReduce mr) {
        // Call Daemon.runMap (mr..) on all data nodes
    }
}
