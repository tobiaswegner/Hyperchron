package org.hyperchron.impl;

public class HyperchronMetrics {
	public static int BLOCK_SIZE = 4096;	
	
	public static int SUPERBLOCK_ENTRIES = 4;
	
	public static int blocksPerSuperblock = (HyperchronMetrics.BLOCK_SIZE / HyperchronMetrics.SUPERBLOCK_ENTRIES) - 1;
}
