package io.antfs.common.lang.file;

import io.antfs.common.lang.object.AntMetaObject;
import io.antfs.common.lang.object.AntObject;

/**
 * FileStorerListener
 * @author gris.wang
 * @since 2017/12/27
 **/
public interface FileStorerListener {

	/**
	 * handle the antMetaObject when it is ready
	 * @param antMetaObject the antMetaObject to be handled
	 */
	void onMetaObjectReady(AntMetaObject antMetaObject);

	/**
	 * handle the antObject when it is ready
	 * @param antObject the antObject to be handled
	 */
	void onAntObjectReady(AntObject antObject);

}