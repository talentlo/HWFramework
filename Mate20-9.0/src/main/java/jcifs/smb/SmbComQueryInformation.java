package jcifs.smb;

class SmbComQueryInformation extends ServerMessageBlock {
    SmbComQueryInformation(String filename) {
        this.path = filename;
        this.command = 8;
    }

    /* access modifiers changed from: package-private */
    public int writeParameterWordsWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int writeBytesWireFormat(byte[] dst, int dstIndex) {
        int start = dstIndex;
        int dstIndex2 = dstIndex + 1;
        dst[dstIndex] = 4;
        return (dstIndex2 + writeString(this.path, dst, dstIndex2)) - start;
    }

    /* access modifiers changed from: package-private */
    public int readParameterWordsWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public int readBytesWireFormat(byte[] buffer, int bufferIndex) {
        return 0;
    }

    public String toString() {
        return new String("SmbComQueryInformation[" + super.toString() + ",filename=" + this.path + "]");
    }
}
