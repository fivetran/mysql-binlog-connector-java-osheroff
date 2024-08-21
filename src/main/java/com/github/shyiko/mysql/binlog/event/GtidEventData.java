/*
 * Copyright 2014 Patrick Prasse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shyiko.mysql.binlog.event;

/**
 * @author <a href="mailto:pprasse@actindo.de">Patrick Prasse</a>
 */
public class GtidEventData implements EventData {

    public static final byte COMMIT_FLAG = 1;

    private MySqlGtid gtid;
    private byte flags;
    private long lastCommitted;
    private long sequenceNumber;
    private long immediateCommitTimestamp;
    private long originalCommitTimestamp;
    private long transactionLength;
    private int immediateServerVersion;
    private int originalServerVersion;

    @Deprecated
    public GtidEventData() {
    }

    public GtidEventData(MySqlGtid gtid, byte flags, long lastCommitted, long sequenceNumber, long immediateCommitTimestamp, long originalCommitTimestamp, long transactionLength, int immediateServerVersion, int originalServerVersion) {
        this.gtid = gtid;
        this.flags = flags;
        this.lastCommitted = lastCommitted;
        this.sequenceNumber = sequenceNumber;
        this.immediateCommitTimestamp = immediateCommitTimestamp;
        this.originalCommitTimestamp = originalCommitTimestamp;
        this.transactionLength = transactionLength;
        this.immediateServerVersion = immediateServerVersion;
        this.originalServerVersion = originalServerVersion;
    }

    @Deprecated
    public String getGtid() {
        return gtid.toString();
    }

    @Deprecated
    public void setGtid(String gtid) {
        this.gtid = MySqlGtid.fromString(gtid);
    }

    public MySqlGtid getMySqlGtid() {
        return gtid;
    }

    public byte getFlags() {
        return flags;
    }

    public long getLastCommitted() {
        return lastCommitted;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public long getImmediateCommitTimestamp() {
        return immediateCommitTimestamp;
    }

    public long getOriginalCommitTimestamp() {
        return originalCommitTimestamp;
    }

    public long getTransactionLength() {
        return transactionLength;
    }

    public int getImmediateServerVersion() {
        return immediateServerVersion;
    }

    public int getOriginalServerVersion() {
        return originalServerVersion;
    }

    @Deprecated
    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GtidEventData");
        sb.append("{flags=").append(flags).append(", gtid='").append(gtid).append('\'');
        sb.append(", last_committed='").append(lastCommitted).append('\'');
        sb.append(", sequence_number='").append(sequenceNumber).append('\'');
        if (immediateCommitTimestamp != 0) {
            sb.append(", immediate_commit_timestamp='").append(immediateCommitTimestamp).append('\'');
            sb.append(", original_commit_timestamp='").append(originalCommitTimestamp).append('\'');
        }
        if (transactionLength != 0) {
            sb.append(", transaction_length='").append(transactionLength).append('\'');
            if (immediateServerVersion != 0) {
               sb.append(", immediate_server_version='").append(immediateServerVersion).append('\'');
               sb.append(", original_server_version='").append(originalServerVersion).append('\'');
            }
        }
        sb.append('}');
        return sb.toString();
    }

}
