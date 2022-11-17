package com.baidu.hive.security.token;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStore;
import org.apache.hadoop.hive.metastore.RawStore;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.security.DelegationTokenIdentifier;
import org.apache.hadoop.hive.metastore.security.TokenStoreDelegationTokenSecretManager;
import org.apache.hadoop.security.token.delegation.AbstractDelegationTokenSecretManager.DelegationTokenInformation;
import org.apache.hadoop.security.token.delegation.MetastoreDelegationTokenSupport;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * public boolean addToken(DelegationTokenIdentifier tokenIdentifier,
 *       DelegationTokenInformation token) throws TokenStoreException {
 *
 *     try {
 *       String identifier = TokenStoreDelegationTokenSecretManager.encodeWritable(tokenIdentifier);
 *       String tokenStr = Base64.encodeBase64URLSafeString(
 *         MetastoreDelegationTokenSupport.encodeDelegationTokenInformation(token));
 *       boolean result = (Boolean)invokeOnTokenStore("addToken", new Object[] {identifier, tokenStr},
 *         String.class, String.class);
 *       LOG.trace("addToken: tokenIdentifier = {}, added = {}", tokenIdentifier, result);
 *       return result;
 *     } catch (IOException e) {
 *       throw new TokenStoreException(e);
 *     }
 *   }
 */
public class ReadAllTokensFromStore {

    public static void main(String[] args) throws MetaException, IOException {


        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss");
        File tokenIdenFile = new File("tokenIden-" + format.format(new Date()) + ".txt");
        File tokenInfoFile = new File("tokenInfo-" + format.format(new Date()) + ".txt");
        PrintWriter tokenIdenWriter = new PrintWriter(new BufferedWriter(new FileWriter(tokenIdenFile)));
        PrintWriter tokenInfoWriter = new PrintWriter(new BufferedWriter(new FileWriter(tokenInfoFile)));
        System.out.println(tokenIdenFile.getAbsolutePath());
        HiveConf conf = new HiveConf();
        try {
            RawStore store = HiveMetaStore.HMSHandler.getMSForConf(conf);
            List<String> tokenIdentifiers = store.getAllTokenIdentifiers();
            System.out.println("get tokens with size:" + tokenIdentifiers.size());
            DelegationTokenIdentifier identifier = new DelegationTokenIdentifier();
            for (String identifierStr : tokenIdentifiers) {
                String tokenStr = store.getToken(identifierStr);
                TokenStoreDelegationTokenSecretManager.decodeWritable(identifier, tokenStr);

                DelegationTokenInformation tokenInformation =
                        MetastoreDelegationTokenSupport.decodeDelegationTokenInformation(Base64.decodeBase64(tokenStr));
                
                writeTokenIden(identifier, tokenIdenWriter);
                writeTokenInfo(tokenInformation, tokenInfoWriter);
            }
        } finally {
            tokenIdenWriter.close();
            tokenInfoWriter.close();
        }
    }

    private static void writeTokenInfo(DelegationTokenInformation tokenInformation,
                                       PrintWriter tokenInfoWriter) {
    }

    private static void writeTokenIden(DelegationTokenIdentifier identifier, PrintWriter tokenIdenWriter) {
        StringBuilder sb = new StringBuilder();
        tokenIdenWriter.write("masterkey:");
        tokenIdenWriter.write(identifier.getMasterKeyId());
        tokenIdenWriter.write("\n");

        tokenIdenWriter.write("Kind:");
        tokenIdenWriter.write(identifier.getKind().toString());
        tokenIdenWriter.write("\n");

    }

}
