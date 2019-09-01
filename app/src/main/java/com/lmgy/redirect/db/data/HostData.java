package com.lmgy.redirect.db.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author lmgy
 * @date 2019/8/31
 */
@Entity(tableName = "HostData")
public class HostData implements Serializable {

    private static final long serialVersionUID = -3951863351189023086L;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    @ColumnInfo(name = "host_type")
    private boolean type;

    @ColumnInfo(name = "host_ip")
    private String ipAddress;

    @ColumnInfo(name = "host_name")
    private String hostName;

    @ColumnInfo(name = "host_remark")
    private String remark;

    public HostData(boolean type, String ipAddress, String hostName, String remark){
        this.type = type;
        this.ipAddress = ipAddress;
        this.hostName = hostName;
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public boolean getType() {
        return type;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public String getRemark() {
        return remark;
    }
}
