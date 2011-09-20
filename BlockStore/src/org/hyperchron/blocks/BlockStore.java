package org.hyperchron.blocks;

import java.nio.LongBuffer;

public interface BlockStore {
	public long getNextBlockID ();

	public LongBuffer loadChunk(long chunkID);
	
	public long ReadFromSuperblock(long chunkID, int chunkOffset, int entry);	
	public void WriteToSuperblock(long chunkID, int chunkOffset, int entry, long value);
	
	public void Shutdown();
}
