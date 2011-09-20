package org.hyperchron.blocks;

public class BlockStoreMetric {
	public int BLOCK_SIZE;
	
	public int SUPERBLOCK_ENTRIES;
	
	public BlockStoreMetric(int blockSize, int superBlockEntries) {
		this.BLOCK_SIZE = blockSize;
		this.SUPERBLOCK_ENTRIES = superBlockEntries;
	}
	
	public int getBlocksPerSuperblock() {
		return (BLOCK_SIZE / SUPERBLOCK_ENTRIES) - 1;
	}
}
