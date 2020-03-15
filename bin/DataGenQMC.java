import formats.KVFormat;
import formats.KV;

public class DataGenQMC {

	public static void main(String[] args) {
		KVFormat file = new KVFormat("DataQMC");
		long nbSamples = 100000L;
		long size = 1000L;
		for (long offset = 0L; offset < nbSamples;) {
			file.write(new KV(Long.toString(offset),Long.toString(size)));
			offset += size;
		}
	}

}
