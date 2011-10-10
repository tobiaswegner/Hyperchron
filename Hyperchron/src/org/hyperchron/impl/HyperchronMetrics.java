package org.hyperchron.impl;

public class HyperchronMetrics {
	public static int BLOCK_SIZE = 4096;	
	
	public static int SUPERBLOCK_ENTRIES = 4;
	
	public static int SUPERBLOCK_ENTRY_ENTITY_ID = 0x00;
	public static int SUPERBLOCK_ENTRY_STARTTIME = 0x01;
	public static int SUPERBLOCK_ENTRY_LENGTH = 0x02;
	
	public static int blocksPerSuperblock = (HyperchronMetrics.BLOCK_SIZE / HyperchronMetrics.SUPERBLOCK_ENTRIES) - 1;
}
