package com.popebp.bitcoin.exchange.btce;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;

import android.net.Uri;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

public class BTCe {
	public static final String API_URL = "https://btc-e.com/tapi";
	private String apiKey = null;
	private String apiSecret = null;
	
	/*
	 * Needs
	 *  o Method that builds the QueryParams and signs
	 *  o Method that builds the Headers
	 *  o Method that takes in api path, query params, headers and makes request (returns JSONObject)
	 *  o ResultFactory that builds each result type
	 */
	
	public BTCe(String key, String secret) {
		this.apiKey = key;
		this.apiSecret = secret;
	}
	
	private JSONObject doRequest(String uri) {
		return doRequest(uri, "");
	}
	
	private JSONObject doRequest(String uri, String postBody) {
		// 1. Build Headers with key and signature of post body
		// 2. Make a POST request to uri, with headers and postBody
		// 3. Read response from server
		// 4. Return parse of JSONObject
		BasicHeader signHeader = null;
		BasicHeader keyHeader = new BasicHeader("Key", apiKey);
		// HMAC-SHA512 of the post params with the apiSecret
		SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA512");
		
		// TODO: Refactor the code below its nasty
		try {
			Mac mac = Mac.getInstance("HmacSHA512");
			mac.init(spec);
			byte[] signatureBytes = mac.doFinal(postBody.getBytes());
			signHeader = new BasicHeader("Sign", Hex.encodeHex(signatureBytes).toString());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HttpPost post = new HttpPost();
		post.addHeader(keyHeader);
		post.addHeader(signHeader);
		try {
			post.setURI(new URI(uri));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse resp = client.execute(post);
			if(resp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				// TODO: Throw an exception for unexpected result
				// This will always be an OK unless something is wrong with the gateway
			}
			// TODO: this is how we get the content resp.getEntity().getContent()
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Result getInfo() {
		String uri = BTCe.buildTradeUrl("getInfo");
		doRequest(uri);
		return new GetInfoResult();
	}
	
	public Result getTransactionHistory(Parameters params) {
		String uri = BTCe.buildTradeUrl("TransHistory");
		return new TransactionHistoryResult();
	}
	
	public Result getTradeHistory(Parameters params) {
		String uri = BTCe.buildTradeUrl("TradeHistory");
		return new TradeHistoryResult();
	}
	
	public Result getActiveOrders(Parameters params) {
		String uri = BTCe.buildTradeUrl("ActiveOrders");
		return new ActiveOrdersResult();
	}
	
	public Result doTrade(Parameters params) {
		String uri = BTCe.buildTradeUrl("Trade");
		return new TradeResult();
	}
	
	public Result doCancelOrder(Parameters params) {
		String uri = BTCe.buildTradeUrl("CancelOrder");
		return new CancelOrderResult();
	}
	
	private static String buildTradeUrl(String action) {
		
		Uri uri = Uri.parse(API_URL);
		
		Uri.Builder builder = uri.buildUpon();
		builder.appendPath(action);
		
		return builder.toString();
	}
}
