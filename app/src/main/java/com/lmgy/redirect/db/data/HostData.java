package com.lmgy.redirect.db.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author lmgy
 * @date 2019/8/31
 */
@Entity
public class HostData implements Serializable {

    private static final long serialVersionUID = -3951863351189023086L;

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "host_type")
    public boolean type;

    @ColumnInfo(name = "host_ip")
    public String ipAddress;

    @ColumnInfo(name = "host_name")
    public String hostName;

    @ColumnInfo(name = "host_remark")
    public String remark;

    @Ignore
    public HostData(boolean type, String ipAddress, String hostName, String remark){
        this.type = type;
        this.ipAddress = ipAddress;
        this.hostName = hostName;
        this.remark = remark;
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
