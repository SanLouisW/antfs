package io.antfs.common.lang.object;

/**
 * @author gris.wang
 * @since 2017/12/27
 **/
public interface ObjectWriter extends ObjectHandler{

    /**
     * store the antMetaObject into disk
     * @param antMetaObject the antMetaObject to be stored
     */
    void writeMeta(AntMetaObject antMetaObject);

    /**
     * store the antObject into disk
     * @param antObject the antObject to be stored
     */
    void write(AntObject antObject);

}
