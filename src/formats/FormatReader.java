package formats;

import java.io.Serializable;

public interface FormatReader extends Serializable {
	public KV read();
}
