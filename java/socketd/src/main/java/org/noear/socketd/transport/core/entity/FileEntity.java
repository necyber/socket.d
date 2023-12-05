package org.noear.socketd.transport.core.entity;

import org.noear.socketd.transport.core.EntityMetas;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 文件实体
 *
 * @author noear
 * @since 2.0
 */
public class FileEntity extends EntityDefault {
    public FileEntity(File file) throws IOException {
        long len = file.length();
        MappedByteBuffer byteBuffer = new RandomAccessFile(file, "r")
                .getChannel()
                .map(FileChannel.MapMode.READ_ONLY, 0, len);

        data(byteBuffer);
        meta(EntityMetas.META_DATA_DISPOSITION_FILENAME, file.getName());
    }
}
