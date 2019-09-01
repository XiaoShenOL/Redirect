package com.lmgy.redirect.db.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.lmgy.redirect.utils.DnsUtils;

import java.io.Serializable;

/**
 * @author lmgy
 * @date 2019/9/1
 */
@Entity(tableName = "DnsData")
public class DnsData implements Serializable {

    private static final long serialVersionUID = -1569227975909267633L;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    @ColumnInfo(name = "position")
    private int position;

    @ColumnInfo(name = "dns_ipv4")
    private String ipv4;

    @ColumnInfo(name = "dns_ipv6")
    private String ipv6;

    @Ignore
    public DnsData(int id, int position, String ipv4, String ipv6){
        this.id = id;
        this.position = position;
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
    }

    public DnsData(int position, String ipv4, String ipv6){
        this.position = position;
        this.ipv4 = ipv4;
        this.ipv6 = ipv6;
    }

    public int getId() {
        return id;
    }

    public String getIpv4() {
        return ipv4;
    }

    public String getIpv6() {
        return ipv6;
    }

    public int getPosition() { return position; }

    public void setId(int id) {
        this.id = id;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }
}
