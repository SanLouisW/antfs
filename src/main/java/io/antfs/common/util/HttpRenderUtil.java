package io.antfs.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * HttpRenderUtil
 * @author gris.wang
 * @since 2017-10-20
 */
public class HttpRenderUtil {

	private static final String EMPTY_CONTENT = "";
	private static final String NO_RESPONSE = "No Response";
	private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
	private static final String CONTENT_TYPE_TEXT = "text/plain;charset=UTF-8";
	private static final String CONTENT_TYPE_XML = "text/xml;charset=UTF-8";
	private static final String CONTENT_TYPE_HTML = "text/html;charset=UTF-8";

	private HttpRenderUtil(){

	}

	/**
	 * renderJSON
	 */
	public static FullHttpResponse renderJSON(String json){
		return render(json, CONTENT_TYPE_JSON);
	}
	
	/**
	 * renderText
	 */
	public static FullHttpResponse renderText(String text) {
		return render(text, CONTENT_TYPE_TEXT);
	}
	
	/**
	 * renderXML
	 */
	public static FullHttpResponse renderXML(String xml) {
		return render(xml, CONTENT_TYPE_XML);
	}
	
	/**
	 * renderHTML
	 */
	public static FullHttpResponse renderHTML(String html) {
		return render(html, CONTENT_TYPE_HTML);
	}

	/**
	 * 转换byte
	 * @param content
	 * @return
	 */
	public static byte[] getBytes(Object content){
		if(content==null){
			return EMPTY_CONTENT.getBytes(CharsetUtil.UTF_8);
		}
		String data = content.toString();
		data = (data==null || data.trim().length()==0)?EMPTY_CONTENT:data;
		return data.getBytes(CharsetUtil.UTF_8);
	}

	/**
	 * render response
	 * @param content the content
	 * @param contentType the contentType
	 * @return the response
	 */
	public static FullHttpResponse render(String content, String contentType){
		return render(Unpooled.wrappedBuffer(getBytes(content)),contentType);
	}

	/**
	 * render response
	 * @param byteBuf the byteBuf
	 * @param contentType the contentType
	 * @return the response
	 */
	public static FullHttpResponse render(ByteBuf byteBuf, String contentType){
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
		if(contentType!=null && contentType.trim().length()>0) {
			response.headers().add(HttpHeaderNames.CONTENT_TYPE, contentType);
		}
		response.headers().add(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(byteBuf.readableBytes()));
		return response;
	}

}
