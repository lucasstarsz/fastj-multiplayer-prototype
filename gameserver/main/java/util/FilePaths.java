package util;

import java.io.InputStream;

import core.util.FilePathUtil;

public class FilePaths {
    public static final InputStream PrivateGameKey = FilePathUtil.streamResource(FilePaths.class, "/serverks.pkcs12");
}
