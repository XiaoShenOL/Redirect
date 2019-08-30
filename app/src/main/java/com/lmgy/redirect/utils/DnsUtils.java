package com.lmgy.redirect.utils;

import android.util.Log;

import com.lmgy.redirect.bean.HostData;
import com.lmgy.redirect.net.Packet;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Address;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lmgy
 * @date 2019/8/15
 */
public class DnsUtils {
    private static final String TAG = "DnsUtils";
    private static ConcurrentHashMap<String, String> DOMAINS_IP_MAPS4 = null;
    private static ConcurrentHashMap<String, String> DOMAINS_IP_MAPS6 = null;


    public static ByteBuffer handleDnsPacket(Packet packet) {
        if (DOMAINS_IP_MAPS4 == null) {
            Log.d(TAG, "DOMAINS_IP_MAPS IS　NULL　HOST FILE ERROR");
            return null;
        }
        try {
            ByteBuffer packetBuffer = packet.backingBuffer;
            packetBuffer.mark();
            byte[] tmpBytes = new byte[packetBuffer.remaining()];
            packetBuffer.get(tmpBytes);
            packetBuffer.reset();
            Message message = new Message(tmpBytes);
            Record question = message.getQuestion();
            ConcurrentHashMap<String, String> DOMAINS_IP_MAPS;
            int type = question.getType();
            if (type == Type.A) {
                DOMAINS_IP_MAPS = DOMAINS_IP_MAPS4;
            } else if (type == Type.AAAA) {
                DOMAINS_IP_MAPS = DOMAINS_IP_MAPS6;
            } else {
                return null;
            }
            Name queryDomain = message.getQuestion().getName();
            String queryString = queryDomain.toString();
            Log.d(TAG, "query: " + question.getType() + " :" + queryString);
            if (!DOMAINS_IP_MAPS.containsKey(queryString)) {
                queryString = "." + queryString;
                int j = 0;
                while (true) {
                    int i = queryString.indexOf(".", j);
                    if (i == -1) {
                        return null;
                    }
                    String str = queryString.substring(i);

                    if (".".equals(str) || "".equals(str)) {
                        return null;
                    }
                    if (DOMAINS_IP_MAPS.containsKey(str)) {
                        queryString = str;
                        break;
                    }
                    j = i + 1;
                }
            }
            InetAddress address = Address.getByAddress(DOMAINS_IP_MAPS.get(queryString));
            Record record;
            if (type == Type.A) {
                record = new ARecord(queryDomain, 1, 86400, address);
            } else {
                record = new AAAARecord(queryDomain, 1, 86400, address);
            }
            message.addRecord(record, 1);
            message.getHeader().setFlag(Flags.QR);
            packetBuffer.limit(packetBuffer.capacity());
            packetBuffer.put(message.toWire());
            packetBuffer.limit(packetBuffer.position());
            packetBuffer.reset();
            packet.swapSourceAndDestination();
            packet.updateUDPBuffer(packetBuffer, packetBuffer.remaining());
            packetBuffer.position(packetBuffer.limit());
            return packetBuffer;
        } catch (Exception e) {
            Log.d(TAG, "dns hook error", e);
            return null;
        }

    }

    public static int handleHosts(List<HostData> savedHostDataList) {
        try {
            Iterator<HostData> savedHostDataIterator = savedHostDataList.iterator();
            HostData savedHostData;

            DOMAINS_IP_MAPS4 = new ConcurrentHashMap<>();
            DOMAINS_IP_MAPS6 = new ConcurrentHashMap<>();

            while (!Thread.interrupted() && savedHostDataIterator.hasNext()) {
                savedHostData = savedHostDataIterator.next();
                if (!savedHostData.getType()) {
                    continue;
                }
                String ip = savedHostData.getIpAddress().trim();
                String hostName = savedHostData.getHostName().trim();
                Log.e(TAG, "handle_hosts: " + ip + " - " + hostName);
                try {
                    Address.getByAddress(ip);
                } catch (Exception e) {
                    continue;
                }
                if (ip.contains(":")) {
                    DOMAINS_IP_MAPS6.put(hostName + ".", ip);
                } else {
                    DOMAINS_IP_MAPS4.put(hostName + ".", ip);
                }

            }
            Log.d(TAG, DOMAINS_IP_MAPS4.toString());
            Log.d(TAG, DOMAINS_IP_MAPS6.toString());
            return DOMAINS_IP_MAPS4.size() + DOMAINS_IP_MAPS6.size();
        } catch (Exception e) {
            Log.d(TAG, "Hook dns error", e);
            return 0;
        }
    }
}
