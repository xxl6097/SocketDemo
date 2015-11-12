package ostrichmyself.util.socket;

import java.io.Serializable;

/**
 * TCP/IP����ͷЭ��
 * 
 * @author admin
 * 
 */
public class Header implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4063282632016483057L;
	protected byte version;
	protected byte attr;
	protected byte type;
	protected byte reserved;
	protected int seqNo;
	protected int msgId;
	protected int msgDataLen;

	public Header() {
		super();
	}

	public Header(byte version, byte attr, byte type, byte reserved, int seqNo,
			int msgId) {
		this(version, attr, type, reserved, seqNo, msgId, 0);
	}

	public Header(byte version, byte attr, byte type, byte reserved, int seqNo,
			int msgId, int msgDataLen) {
		super();
		this.version = version;
		this.attr = attr;
		this.type = type;
		this.reserved = reserved;
		this.seqNo = seqNo;
		this.msgId = msgId;
		this.msgDataLen = msgDataLen;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte getAttr() {
		return attr;
	}

	public void setAttr(byte attr) {
		this.attr = attr;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte getReserved() {
		return reserved;
	}

	public void setReserved(byte reserved) {
		this.reserved = reserved;
	}

	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public int getMsgDataLen() {
		return msgDataLen;
	}

	public void setMsgDataLen(int msgDataLen) {
		this.msgDataLen = msgDataLen;
	}

	@Override
	public String toString() {
		return "Header [version=" + version + ", attr=" + attr + ", type="
				+ type + ", reserved=" + reserved + ", seqNo=" + seqNo
				+ ", msgId=" + msgId + ", msgDataLen=" + msgDataLen + "]";
	}

}
