package com.goertek.asp.sage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

public class WifiAdmin {
	
	private static final boolean D = true;
    private static final String TAG = WifiAdmin.class.getSimpleName();

    public static final int WIFI_TYPE_NOPASS = 0;
    public static final int WIFI_TYPE_WPA = 1;
    public static final int WIFI_TYPE_WEB = 2;
    
    private static final int WIFI_CONNECT_LOOP_TIMES = 10;

    private Context mContext;
    private WifiManager mWifiManager;

    private List<WifiConfiguration> mWifiConfiguredList;
    private List<ScanResult> mWifiScanResultList;

    public WifiAdmin(Context context) {
    	
        mContext = context;
        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        
    }

    public void openWifi() {
    	
        if(!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);
        
    }

    public void closeWifi() {

        if(mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(false);

    }

    public String getConnectedServerIp() {

        ConnectivityManager cntManager = (ConnectivityManager)mContext.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cntManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State state = networkInfo.getState();

        if(state== NetworkInfo.State.CONNECTED) {

            if(D) Log.d(TAG,"Wifi connected.........");
            DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
            return ipInt2String(dhcpInfo.serverAddress);
        }

        return null;

    }

    public String getLocalIp() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return ipInt2String(wifiInfo.getIpAddress());
    }

    public String getLocalHostIp() {
        try {
            Enumeration<NetworkInterface> netEmuners = NetworkInterface.getNetworkInterfaces();
            while(netEmuners.hasMoreElements()) {
                NetworkInterface networkInterface = netEmuners.nextElement();
                Enumeration<InetAddress> ipEmuners = networkInterface.getInetAddresses();
                while (ipEmuners.hasMoreElements()) {
                    InetAddress inetAddress = ipEmuners.nextElement();
                    if(!inetAddress.isLoopbackAddress()&& InetAddressUtils.isIPv4Address
                            (inetAddress.getHostAddress()))
                        return inetAddress.getHostAddress();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }
    
      private int getConnectedWifiSecurityType() {
        List<WifiConfiguration> wifiConfigList = mWifiManager.getConfiguredNetworks();
        for(WifiConfiguration wifiConfiguration:wifiConfigList) {
            if(wifiConfiguration.SSID.equals(getConnectedWifiSSID())) {
                if(wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)||
                        wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt
                                .WPA_PSK)||wifiConfiguration.allowedKeyManagement.get
                        (WifiConfiguration.KeyMgmt.IEEE8021X)){
                    return WifiAdmin.WIFI_TYPE_WPA;
                }

                return (wifiConfiguration.wepKeys[0]!=null)? WifiAdmin.WIFI_TYPE_WEB:WifiAdmin
                        .WIFI_TYPE_NOPASS;
            }
        }
        return -1;
    }

    public int getSecurityType(ScanResult scanResult) {
        String value = scanResult.capabilities;
        if(value.contains("WEP")) {
            return WifiAdmin.WIFI_TYPE_WEB;
        }else if(value.contains("WPA")) {
            return WifiAdmin.WIFI_TYPE_WPA;
        }
        return WifiAdmin.WIFI_TYPE_NOPASS;
    }

    public void startScanForWifi() {

        mWifiConfiguredList = mWifiManager.getConfiguredNetworks();
        mWifiManager.startScan();
        mWifiScanResultList = mWifiManager.getScanResults();

    }
    
    public void removeAllConfiguredNetwork() {
    	
        List<WifiConfiguration> lists =  mWifiManager.getConfiguredNetworks();
        
        if(lists!=null)
            for(WifiConfiguration wifi:lists) {
                removeNetwork(wifi.networkId);
            } 
        
        
    }


    public WifiConfiguration createNetworkConfiguration(String ssid, String key, int type) {

    	if(D) Log.d(TAG,"createNetworkConfiguration...");
        if(D) Log.d(TAG,"ssid in createCfg-------:"+ssid);
        if(D) Log.d(TAG,"key in createCfg-----------:"+key);
        if(D) Log.d(TAG,"type in createCfg--------:"+type);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        //config.SSID = "\"" + ssid + "\"";
        config.SSID = ssid;

        if(D) Log.d(TAG,"config.SSID:"+config.SSID);
        
        WifiConfiguration configTemp = isConfigured(ssid);

        if(configTemp!=null) 
            removeNetwork(configTemp.networkId);
        if(D) Log.d(TAG,"before switch...");

        switch (type) {

            case WIFI_TYPE_NOPASS:
            	
            	if(D) Log.d(TAG," WIFI_TYPE_NOPASS");

                config.wepKeys[0]= "\""+"\"";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
                break;

            case WIFI_TYPE_WEB:
            	
            	if(D) Log.d(TAG," WIFI_TYPE_WEB");
            	
                config.hiddenSSID = true;
                config.wepKeys[0]= "\""+key+"\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
                break;

            case WIFI_TYPE_WPA:

            	if(D) Log.d(TAG," WIFI_TYPE_WPA");
               // config.wepKeys[0]= "\""+"\"";
                config.preSharedKey = "\""+key+"\"";
               // config.preSharedKey = key;
                config.hiddenSSID = true;
             //   config.wepTxKeyIndex = 0;
             //   config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                
              //  config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
             //   config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
             //   config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;

                break;

            default:
                break;

        }

        return config;

    }



    @SuppressWarnings("static-access")
	public void add2ConnectNetWork(WifiConfiguration wcg,WifiActionListener listener) {

    	if(D) Log.d(TAG,"add2ConnectNetWork...");
    	
    	int loop_times = 0;
    	
        int wcgID= mWifiManager.addNetwork(wcg);
        
        if(wcgID<0) {
            //while(mWifiManager.addNetwork(wcg)<0);
        	if(D) Log.d(TAG,"wcgID < 0");
        	if(listener!=null)
        		listener.onWifiConnectFailed();
        }

        if(mWifiManager.enableNetwork(wcgID, true)) {
        	if(D) Log.d(TAG, "enableNetwork successful...");
            if(mWifiManager.pingSupplicant()) {
                while(!isConnected()) {
                	if(D) Log.d(TAG,"times: "+loop_times);
                	if(loop_times++>WIFI_CONNECT_LOOP_TIMES) {
                		if(D) Log.d(TAG,"WIFI_CONNECT_LOOP_TIMES timeout...");
                		if(listener!=null)
                			listener.onWifiConnectFailed();
                		return;
                	}else {
                		try {
                			Thread.currentThread().sleep(2000);
                			
                			mWifiManager.enableNetwork(wcgID, true);
                			
                		} catch (InterruptedException e) {
                			e.printStackTrace();
                		}
                	}
                }
                if(listener!=null)
                	listener.onWifiConnectSuccess();
                return;  	
            }
        }
        
        listener.onWifiConnectFailed();
        
    }

    public void removeNetwork(int netId) {
    	
    	if(D) Log.d(TAG,"remove Network...");
    	mWifiManager.disconnect();
        mWifiManager.disableNetwork(netId);
        if(D) Log.d(TAG,"netID is: "+netId);
        if( mWifiManager.removeNetwork(netId))
        	Log.d(TAG,"remove Network ID succussful..."+netId);
        if(mWifiManager.saveConfiguration())
        	Log.d(TAG,"save Configration successful...");

    }

    public WifiConfiguration isConfigured(String ssid) {
    	
        List<WifiConfiguration> configsTemp = mWifiManager.getConfiguredNetworks();
        
        if(configsTemp==null)
        	return null;

        for(WifiConfiguration wifiConfig:configsTemp) {
            if(wifiConfig.SSID.contains(ssid))
                return wifiConfig;
        }
        
        return null;
    }

    public boolean isConnected() {

        ConnectivityManager cntManager = (ConnectivityManager) mContext.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cntManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo.State state = networkInfo.getState();
        if (state == NetworkInfo.State.CONNECTED) {
            return true;
        }
        
        return false;
    }
    
    public void disconnect() {
    	WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
    	removeNetwork(wifiInfo.getNetworkId());
    }

    private String ipInt2String(int ip) {

        int first = ip>>24&0xff;
        if(first<0)
            first = first+1+0xff;
        int second = ip>>16&0xff;
        int third = ip>>8&0xff;
        int fourth = ip&0xff;
        StringBuilder builder = new StringBuilder();
        builder.append(fourth).append(".").append(third).append(".").append(second).append(".").append(first);

        return builder.toString();
    }

	public boolean setWifiApEnabled(boolean on) {

    	try {
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = "ApOfAlmo";
            wifiConfiguration.preSharedKey = "123456";
            return (Boolean) method.invoke(mWifiManager, wifiConfiguration, on);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    	return false;
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }
	
}
