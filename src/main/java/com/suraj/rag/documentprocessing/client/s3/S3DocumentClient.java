package com.suraj.rag.documentprocessing.client.s3;

import java.io.InputStream;

public interface S3DocumentClient {

    InputStream readObject(String bucket, String key);
}
