package org.ipfs;

import java.io.*;
import java.util.*;

public class Multihash {
    enum Type {
        sha1(0x11, 20),
        sha2_256(0x12, 32),
        sha2_512(0x13, 64),
        sha3(0x14, 64),
        blake2b(0x40, 64),
        blake2s(0x41, 32);

        public int index, length;

        Type(int index, int length) {
            this.index = index;
            this.length = length;
        }

        private static Map<Integer, Type> lookup = new TreeMap<>();
        static {
            for (Type t: Type.values())
                lookup.put(t.index, t);
        }

        public static Type lookup(int t) {
            if (!lookup.containsKey(t))
                throw new IllegalStateException("Unknown Multihash type: "+t);
            return lookup.get(t);
        }
    }

    public final Type type;
    public final byte size;
    public final byte[] hash;

    public Multihash(Type type, byte size, byte[] hash) {
        if ((size & 0xff) != hash.length)
            throw new IllegalStateException("Incorrect size: " + (size&0xff) + " != "+hash.length);
        if (hash.length != type.length)
            throw new IllegalStateException("Incorrect hash length: " + hash.length + " != "+type.length);
        this.type = type;
        this.size = size;
        this.hash = hash;
    }

    public Multihash(byte[] multihash) {
        this(Type.lookup(multihash[0] & 0xff), multihash[1], Arrays.copyOfRange(multihash, 2, multihash.length));
    }

    public byte[] toBytes() {
        byte[] res = new byte[hash.length+2];
        res[0] = (byte)type.index;
        res[1] = (byte)hash.length;
        System.arraycopy(hash, 0, res, 2, hash.length);
        return res;
    }

    public String toHex() {
        StringBuilder res = new StringBuilder();
        for (byte b: toBytes())
            res.append(String.format("%x", b&0xff));
        return res.toString();
    }

    public static Multihash fromHex(String hex) {
        if (hex.length() % 2 != 0)
            throw new IllegalStateException("Uneven number of hex digits!");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i=0; i < hex.length()-1; i+= 2)
            bout.write(Integer.valueOf(hex.substring(i, i+2), 16));
        return new Multihash(bout.toByteArray());
    }
}
