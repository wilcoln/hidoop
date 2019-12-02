package formats;

import java.io.Serializable;

public interface FormatWriter extends Serializable {
	public void write(KV record);
}
