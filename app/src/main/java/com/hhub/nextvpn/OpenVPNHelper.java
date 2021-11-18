package com.hhub.nextvpn;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.api.IOpenVPNAPIService;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;

public class OpenVPNHelper {

    public static String getLocalOVPNConfig(Context context) {
        String config = "";
        String line;
        InputStream conf = null;
        try {
            conf = context.getAssets().open("default.ovpn");
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            while (true) {
                line = br.readLine();
                if (line == null) break;
                config += line + "\n";
            }

            br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return config;
    }

    public static void startOpenVPN(Context context, String config) throws RemoteException {
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(config));
            VpnProfile vp = cp.convertProfile();// Analysis.ovpn
            vp.mName = "myvpn";
            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = "vpn";
            vp.mPassword = "vpn";
            ProfileManager.setTemporaryProfile(context, vp);
            VPNLaunchHelper.startOpenVpn(vp, context);
            Log.d("hhlog1", "launch");
        } catch (IOException | ConfigParser.ConfigParseError e) {
            Log.d("hhlog1", e.getMessage());
            throw new RemoteException(e.getMessage());
        }
    }

    public static void stopOpenVPN(Context context, IOpenVPNServiceInternal service) {
        ProfileManager.setConntectedVpnProfileDisconnected(context);
        if (service != null) {
            try {
                service.stopVPN(false);
            } catch (RemoteException e) {
                VpnStatus.logException(e);
            }
        }
    }
}
