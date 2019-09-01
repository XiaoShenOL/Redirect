package com.lmgy.redirect.net;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.lmgy.redirect.R;
import com.lmgy.redirect.db.data.DnsData;
import com.lmgy.redirect.db.data.HostData;
import com.lmgy.redirect.db.repository.HostRepository;
import com.lmgy.redirect.utils.DnsUtils;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.Selector;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author lm
 * Created by lmgy on 15/8/2019
 */
public class LocalVpnService extends VpnService {
    private static final String TAG = "LocalVpnService";
    private static final String VPN_ADDRESS = "10.1.10.1";
    //本地代理服务器IP地址，必要，建议用A类IP地址，防止冲突

    private static final String VPN_ADDRESS6 = "fe80:49b1:7e4f:def2:e91f:95bf:fbb6:1111";

//    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
//    private static final String VPN_ROUTE6 = "::"; // Intercept everything

    private static final int VPN_ADDRESS_MASK = 32;
    private static final int VPN_ADDRESS6_MASK = 128;
    private static final int VPN_DNS4_MASK = 32;
    private static final int VPN_DNS6_MASK = 128;
    private static final int VPN_ROUTE_MASK = 0;
    private static final int VPN_MTU = 4096;

    public static final String BROADCAST_VPN_STATE = LocalVpnService.class.getName() + ".VPN_STATE";
    public static final String ACTION_CONNECT = LocalVpnService.class.getName() + ".START";
    public static final String ACTION_DISCONNECT = LocalVpnService.class.getName() + ".STOP";

    private static boolean isRunning = false;

    private String VPN_DNS4;
    private String VPN_DNS6;
    private ParcelFileDescriptor vpnInterface = null;
    private ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue;
    private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
    private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;
    private ExecutorService executorService;

    private Selector udpSelector;
    private Selector tcpSelector;

    @Override
    public void onCreate() {
        super.onCreate();
        Maybe.zip(setupDns(), setupHostRules(), (dnsData, hostData) -> {
            if (dnsData.isEmpty()) {
                VPN_DNS4 = "8.8.8.8";
                VPN_DNS6 = "2001:4860:4860::8888";
            } else {
                VPN_DNS4 = dnsData.get(0).getIpv4();
                VPN_DNS6 = dnsData.get(0).getIpv6();
            }
            try {
                DnsUtils.handleHosts(hostData);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "error setup host file service", e);
                return false;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaybeObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(Boolean b) {
                        if (b) {
                            setupVpn();
                        } else {
                            Log.e(TAG, "start vpn error");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private Maybe<List<DnsData>> setupDns() {
        return HostRepository.INSTANCE.getDns(getApplicationContext()).
                subscribeOn(Schedulers.io());
    }


    private Maybe<List<HostData>> setupHostRules() {
        return HostRepository.INSTANCE.getAllHosts(getApplicationContext())
                .subscribeOn(Schedulers.io());
    }

    private void setupVpn() {
        if (vpnInterface == null) {
            Builder builder = new Builder();
            builder.addAddress(VPN_ADDRESS, VPN_ADDRESS_MASK);
            builder.addAddress(VPN_ADDRESS6, VPN_ADDRESS6_MASK);
            builder.addRoute(VPN_DNS4, VPN_DNS4_MASK);
            builder.addRoute(VPN_DNS6, VPN_DNS6_MASK);
//            builder.addRoute(VPN_ROUTE,VPN_ROUTE_MASK);
//            builder.addRoute(VPN_ROUTE6,VPN_ROUTE_MASK);
            builder.setMtu(VPN_MTU);
            builder.addDnsServer(VPN_DNS4);
            builder.addDnsServer(VPN_DNS6);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String[] whiteList = {"com.android.vending", "com.google.android.apps.docs", "com.google.android.apps.photos", "com.google.android.gm", "com.google.android.apps.translate"};
                for (String white : whiteList) {
                    try {
                        builder.addDisallowedApplication(white);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            vpnInterface = builder.setSession(getString(R.string.app_name)).setConfigureIntent(null).establish();
        }
        if (vpnInterface == null) {
            Log.d(TAG, "unknown error");
            stopVpnService();
            return;
        }
        isRunning = true;
        try {
            udpSelector = Selector.open();
            tcpSelector = Selector.open();
            deviceToNetworkUDPQueue = new ConcurrentLinkedQueue<>();
            deviceToNetworkTCPQueue = new ConcurrentLinkedQueue<>();
            networkToDeviceQueue = new ConcurrentLinkedQueue<>();
            executorService = Executors.newFixedThreadPool(5);
            executorService.submit(new UDPInput(networkToDeviceQueue, udpSelector));
            executorService.submit(new UDPOutput(deviceToNetworkUDPQueue, networkToDeviceQueue, udpSelector, this));
            executorService.submit(new TCPInput(networkToDeviceQueue, tcpSelector));
            executorService.submit(new TCPOutput(deviceToNetworkTCPQueue, networkToDeviceQueue, tcpSelector, this));
            executorService.submit(new VPNRunnable(vpnInterface.getFileDescriptor(),
                    deviceToNetworkUDPQueue, deviceToNetworkTCPQueue, networkToDeviceQueue));
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_VPN_STATE).putExtra("running", true));
            Log.i(TAG, "Started");
        } catch (Exception e) {
            // TODO: Here and elsewhere, we should explicitly notify the user of any errors
            // and suggest that they stop the service, since we can't do it ourselves
            Log.e(TAG, "Error starting service", e);
            stopVpnService();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            stopVpnService();
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private void stopVpnService() {

        if (executorService != null) {
            executorService.shutdown();
        }
        isRunning = false;
        cleanup();
        stopSelf();
        Log.d(TAG, "Stopping");
    }

    @Override
    public void onRevoke() {
        stopVpnService();
        super.onRevoke();
    }

    @Override
    public void onDestroy() {
        stopVpnService();
        super.onDestroy();
    }

    private void cleanup() {
        deviceToNetworkTCPQueue = null;
        deviceToNetworkUDPQueue = null;
        networkToDeviceQueue = null;
        ByteBufferPool.clear();
        closeResources(udpSelector, tcpSelector, vpnInterface);
    }

    private static void closeResources(Closeable... resources) {
        for (Closeable resource : resources) {
            try {
                resource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class VPNRunnable implements Runnable {
        private static final String TAG = VPNRunnable.class.getSimpleName();

        private FileDescriptor vpnFileDescriptor;

        private ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue;
        private ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue;
        private ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue;

        public VPNRunnable(FileDescriptor vpnFileDescriptor,
                           ConcurrentLinkedQueue<Packet> deviceToNetworkUDPQueue,
                           ConcurrentLinkedQueue<Packet> deviceToNetworkTCPQueue,
                           ConcurrentLinkedQueue<ByteBuffer> networkToDeviceQueue) {
            this.vpnFileDescriptor = vpnFileDescriptor;
            this.deviceToNetworkUDPQueue = deviceToNetworkUDPQueue;
            this.deviceToNetworkTCPQueue = deviceToNetworkTCPQueue;
            this.networkToDeviceQueue = networkToDeviceQueue;
        }

        @Override
        public void run() {
            Log.i(TAG, "Started");

            FileChannel vpnInput = new FileInputStream(vpnFileDescriptor).getChannel();
            FileChannel vpnOutput = new FileOutputStream(vpnFileDescriptor).getChannel();
            try {
                ByteBuffer bufferToNetwork = null;
                boolean dataSent = true;
                boolean dataReceived;
                while (!Thread.interrupted()) {
                    if (dataSent) {
                        bufferToNetwork = ByteBufferPool.acquire();
                    } else {
                        bufferToNetwork.clear();
                    }

                    // TODO: Block when not connected
                    int readBytes = vpnInput.read(bufferToNetwork);
                    if (readBytes > 0) {
                        dataSent = true;
                        bufferToNetwork.flip();
                        Packet packet = new Packet(bufferToNetwork);
                        if (packet.isUDP()) {
                            deviceToNetworkUDPQueue.offer(packet);
                        } else if (packet.isTCP()) {
                            deviceToNetworkTCPQueue.offer(packet);
                        } else {
                            Log.w(TAG, "Unknown packet type");
                            dataSent = false;
                        }
                    } else {
                        dataSent = false;
                    }
                    ByteBuffer bufferFromNetwork = networkToDeviceQueue.poll();
                    if (bufferFromNetwork != null) {
                        bufferFromNetwork.flip();
                        while (bufferFromNetwork.hasRemaining()) {
                            try {
                                vpnOutput.write(bufferFromNetwork);
                            } catch (Exception e) {
                                Log.e(TAG, e.toString(), e);
                                break;
                            }
                        }
                        dataReceived = true;
                        ByteBufferPool.release(bufferFromNetwork);
                    } else {
                        dataReceived = false;
                    }

                    // TODO: Sleep-looping is not very battery-friendly, consider blocking instead
                    // Confirm if throughput with ConcurrentQueue is really higher compared to BlockingQueue
                    if (!dataSent && !dataReceived) {
                        Thread.sleep(11);
                    }
                }
            } catch (InterruptedException e) {
                Log.i(TAG, "Stopping");
            } catch (IOException e) {
                Log.w(TAG, e.toString(), e);
            } finally {
                closeResources(vpnInput, vpnOutput);
            }
        }
    }
}
