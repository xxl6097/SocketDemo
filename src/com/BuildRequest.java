package com;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wisegeek.finance.protobuf.Header;
import com.wisegeek.finance.protobuf.PubSub;
import com.wisegeek.finance.protobuf.RealTimeQuoteData.RealTimeQuoteDataResponse;
import com.wisegeek.finance.protobuf.RealTimeQuoteData.RealTimeQuoteDataResponse.RealData;

public class BuildRequest {
	public static Header realTimeHeader = new Header((byte) 0, (byte) 0,
			(byte) 2, (byte) 0, 0, 7013);
	public static String[] codes = new String[] { "USD", "EURUSD", "GBPUSD","USDCHF", "USDCAD", "EURGBP" };
	protected static Header getHeader(ByteBuffer buf) {
		Header head = new Header();
		head.setVersion(buf.get());
		head.setAttr(buf.get());
		head.setType(buf.get());
		head.setReserved(buf.get());
		head.setSeqNo(buf.getInt());
		head.setMsgId(buf.getInt());
		head.setMsgDataLen(buf.getInt());
		return head;
	}
	
	/**
	 * 解析包
	 * @param msgData
	 * @return
	 */
	public static List<RealData> parse(byte[] msgData) {
		ByteBuffer buf = ByteBuffer.wrap(msgData);
		Header header = getHeader(buf);
		System.out.println("消息头:"+header.toString());
		if (header.getMsgId() == 7013) {
			System.out.println("订阅成功");
			return null;
		}else if (2001 == header.getMsgId()) {
			byte[] body = new byte[msgData.length - buf.position()];
			buf.get(body);
			List<RealData> list = new ArrayList<RealData>();
			if (msgData != null) {

				try {
					RealTimeQuoteDataResponse response = RealTimeQuoteDataResponse
							.parseFrom(body);
					list = response.getRealDatasList();
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}

			}
			
			try {
				for (RealData realData : list) {
					System.out.println(realData.getChsNameBytes().toString("GBK")+" "+realData.getLastPrice()+" "+realData.getLastTime());
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("----------------------------------------------------------");
//			System.out.println(list.toString());
			return list;
		}
		return null;
	}

	/**
	 * 构建请求参数
	 * @return
	 */
	public static byte[] buildRequestByte() {
		PubSub.StockSubscribe.Builder builders = PubSub.StockSubscribe
				.newBuilder();
		PubSub.StockSubscribe.SubStock.Builder value = PubSub.StockSubscribe.SubStock
				.newBuilder();
		if (codes != null && codes.length > 0) {
			for (String c : codes) {
				value.setCode(c);
				value.setType("RealTime");
				builders.addSubStocks(value);
			}
		}
		builders.setAction("ADD");
		builders.setPushType(1);
		PubSub.StockSubscribe requestStock = builders.build();
		byte[] data = requestStock.toByteArray();
		realTimeHeader.setSeqNo(1009);
		realTimeHeader.setMsgDataLen(data.length);
		realTimeHeader.setMsgId(7013);
		byte[] h = getHeaderData(realTimeHeader);
		ByteBuffer buffer = ByteBuffer.allocate(data.length + h.length);
		buffer.put(h);
		buffer.put(data);
		buffer.flip();
		return buffer.array();
	}

	/**
	 * 解析头
	 * @param header
	 * @return
	 */
	protected static byte[] getHeaderData(Header header){
		byte version = header.getVersion();
		byte attr = header.getAttr();
		byte type = header.getType();
		byte reserved = header.getReserved();
		int seqNo = header.getSeqNo();
		int msgId = header.getMsgId();
		int msgDataLen = header.getMsgDataLen();
		ByteBuffer buffer = ByteBuffer.allocate(16);
		buffer.put(version);
		buffer.put(attr);
		buffer.put(type);
		buffer.put(reserved);
		buffer.putInt(seqNo);
		buffer.putInt(msgId);
		buffer.putInt(msgDataLen);
		return buffer.array();
	}
}
