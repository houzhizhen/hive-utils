package com.baidu.hive.security.token;

import com.baidu.hive.util.HiveTestUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStore;
import org.apache.hadoop.hive.metastore.RawStore;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.security.DelegationTokenIdentifier;
import org.apache.hadoop.hive.metastore.security.TokenStoreDelegationTokenSecretManager;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.security.token.delegation.AbstractDelegationTokenSecretManager.DelegationTokenInformation;
import org.apache.hadoop.security.token.delegation.MetastoreDelegationTokenSupport;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Reference @code{addToken#org.apache.hadoop.hive.metastore.security.DBTokenStore}
 */
public class ReadAllTokensFromStore {

    private static SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

    public static void main(String[] args) throws MetaException, IOException {
        SimpleDateFormat fileNameFormat = new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss");
        File tokenIdenFile = new File("hive-metastore-delegation-token-" + fileNameFormat.format(new Date()) + ".txt");
        PrintWriter tokenIdenWriter = new PrintWriter(new BufferedWriter(new FileWriter(tokenIdenFile)));
        System.out.println("output file: " + tokenIdenFile.getAbsolutePath());
        HiveConf conf = new HiveConf();
        HiveTestUtils.addResource(conf, args);
        try {
            RawStore store = HiveMetaStore.HMSHandler.getMSForConf(conf);
            List<String> tokenIdentifierStrList = store.getAllTokenIdentifiers();
            System.out.println("get tokens with size:" + tokenIdentifierStrList.size());

            for (String identifierStr : tokenIdentifierStrList) {
                DelegationTokenIdentifier tokenIdentifier = getDelegationTokenIdentifier(identifierStr);
                tokenIdenWriter.println(getTokenIdentifierStr(tokenIdentifier));
            }
        } finally {
            tokenIdenWriter.close();
        }
    }

    protected static DelegationTokenIdentifier getDelegationTokenIdentifier(String identifierStr) throws IOException {
        DelegationTokenIdentifier identifier = new DelegationTokenIdentifier();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                Base64.decodeBase64(identifierStr.getBytes(StandardCharsets.US_ASCII))));
        identifier.readFields(in);
        return identifier;
    }

    public static String getTokenIdentifierStr(DelegationTokenIdentifier identifier) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(identifier.getKind())
              .append(", owner=").append(identifier.getOwner())
              .append(", renewer=").append(identifier.getRenewer())
              .append(", realUser=").append(identifier.getRealUser())
              .append(", issueDate=").append(format.format(identifier.getIssueDate()))
              .append(", maxDate=").append(format.format(identifier.getMaxDate()))
              .append(", sequenceNumber=").append(identifier.getSequenceNumber())
              .append(", masterKeyId=").append(identifier.getMasterKeyId());
        return buffer.toString();
    }
}
