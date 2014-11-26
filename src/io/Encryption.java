package io;

import java.util.Random;

import DataTypes.ByteConventions;

public class Encryption {

	static Random gen = new Random();

	public static byte[] genPGPSessionKey() {
		gen.setSeed(System.currentTimeMillis());
		byte[] ret = new byte[64];
		gen.nextBytes(ret);
		System.out.println("Key generated : "
				+ ByteConventions.bytesToHexes(ret));
		return ret;
	}

}
