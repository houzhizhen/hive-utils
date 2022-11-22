package com.baidu.hive.security.token;

import com.baidu.hive.util.Assert;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hive.metastore.security.DelegationTokenIdentifier;
import org.apache.hadoop.hive.metastore.security.TokenStoreDelegationTokenSecretManager;
import org.apache.hadoop.security.token.delegation.AbstractDelegationTokenSecretManager.DelegationTokenInformation;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.security.token.delegation.AbstractDelegationTokenSecretManager;
import org.apache.hadoop.security.token.delegation.MetastoreDelegationTokenSupport;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestReadAllTokensFromStore {
    
    @Test
    public void testPair() throws IOException {
        System.out.println("testPair");
        String tokenStr = "AARoaXZlBGhpdmUhaGl2ZS9ibXItbWFzdGVyLTQ3OWRkZWVAQkFJRFUuQ09NigGEje6Tv4oBhLH7F7-OBbIc";
        DelegationTokenIdentifier tokenIdentifier = ReadAllTokensFromStore.getDelegationTokenIdentifier(tokenStr);
        System.out.println("key:" + tokenIdentifier);
        System.out.println(ReadAllTokensFromStore.getTokenIdentifierStr(tokenIdentifier));
    }

    /**
     * Token not serialize url, and password not public to the outside, only renew data available.
     * @throws IOException
     */
    @Test
    public void testToken() throws IOException {
        byte[] password = new byte[100];
        password[0] = 1;
        String trackingUrl = "url";
        DelegationTokenInformation token = new DelegationTokenInformation(1L, password, trackingUrl);

        String tokenStr = Base64.encodeBase64URLSafeString(MetastoreDelegationTokenSupport.encodeDelegationTokenInformation(token));
        byte[] bytes = Base64.decodeBase64(tokenStr.getBytes(StandardCharsets.US_ASCII));
        DelegationTokenInformation tokenRead = MetastoreDelegationTokenSupport.decodeDelegationTokenInformation(bytes);
       // Assert.assertEquals(trackingUrl, tokenRead.getTrackingId());
        Assert.assertEquals(1L, tokenRead.getRenewDate());
    }

    @Test
    public void testDelegationTokenIdentifier() throws IOException {
        DelegationTokenIdentifier identifier = new DelegationTokenIdentifier();
        long issueDate = System.currentTimeMillis();
        identifier.setOwner(new Text("ownerHouzhzihen"));
        identifier.setMasterKeyId(1);
        identifier.setIssueDate(issueDate);
        identifier.setMaxDate(issueDate + 1000);
        identifier.setRenewer(new Text("renewer Houzhzihen"));
        identifier.setRealUser(new Text("realuser Houzhzihen"));
        identifier.setSequenceNumber(2);

        String identifierStr = TokenStoreDelegationTokenSecretManager.encodeWritable(identifier);
        System.out.println(identifierStr);
        DelegationTokenIdentifier identifier2 = ReadAllTokensFromStore.getDelegationTokenIdentifier(identifierStr);
        System.out.println("identifier2:" + identifier2.toString());
    }

//    @Test
//    public void testEncodeWritable() throws IOException {
//        Text text = new Text("abcdefg中国");
//        String s = encodeWritable(text);
//        System.out.println(s);
//        Text text2 = new Text();
//        decodeWritable(text2, s);
//        System.out.println("text2:"  + text2);
//
//    }
}
