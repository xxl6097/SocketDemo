package ostrichmyself.util.socket;

import java.util.Arrays;

public class MessageData {
	private Header header;

	private byte[] data;

	public MessageData() {
		super();
	}

	public MessageData(Header header, byte[] data) {
		this.header = header;
		this.data = data;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "MessageData [header=" + header + ", data="
				+ Arrays.toString(data) + "]";
	}

}
