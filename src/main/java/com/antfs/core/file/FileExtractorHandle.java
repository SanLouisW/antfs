package com.antfs.core.file;

import com.antfs.core.object.AntMetaObject;
import com.antfs.core.object.AntObject;

/**
 * FileExtractorHandle
 * @author gris.wang
 * @since 2017/12/27
 **/
public interface FileExtractorHandle {

	/**
	 * store the antMetaObject into disk
	 * @param antMetaObject the antMetaObject to be stored
	 */
	void storeMeta(AntMetaObject antMetaObject);

	/**
	 * store the antObject into disk
	 * @param antObject the antObject to be stored
	 */
	void store(AntObject antObject);

}