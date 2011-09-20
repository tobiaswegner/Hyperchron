package org.hyperchron.blocks.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Hashtable;

import org.hyperchron.blocks.BlockStore;
import org.hyperchron.blocks.BlockStoreMetric;

public class MappedBlockStore implements BlockStore {
	protected final int FILE_HEADER_OFFSET_BLOCK_ID = 0; 
	
	protected final long FILE_HEADER_SIZE = 256;
	protected LongBuffer fileHeaderBuffer = null;
	
	protected Hashtable<Long, LongBuffer> openFileMappings = new Hashtable<Long, LongBuffer>();
	
	protected RandomAccessFile fileBackend = null;
	protected BlockStoreMetric metric = null;
	
	protected long numChunks = 0;
	protected Object numChunksMutex = new Object();
	
	public MappedBlockStore (File file, BlockStoreMetric metric) throws FileNotFoundException {
		fileBackend = new RandomAccessFile(file, "rwd");
		this.metric = metric;
		
		try {
			numChunks = (fileBackend.length() - FILE_HEADER_SIZE) / (8 * metric.BLOCK_SIZE * metric.BLOCK_SIZE  / metric.SUPERBLOCK_ENTRIES); 
			
			fileHeaderBuffer = fileBackend.getChannel().map(MapMode.READ_WRITE, 0, FILE_HEADER_SIZE).asLongBuffer();
			
			blockID = fileHeaderBuffer.get(FILE_HEADER_OFFSET_BLOCK_ID);			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected Object blockIDSemaphore = new Object(); 	
	protected long blockID;	

	@Override
	public long getNextBlockID() {
		synchronized (blockIDSemaphore) {
			long nextBlockID = blockID;

			blockID++;
			
			fileHeaderBuffer.put(FILE_HEADER_OFFSET_BLOCK_ID, blockID);
			
			return nextBlockID;			
		}
	}

	@Override
	public LongBuffer loadChunk(long chunkID) {
		if (!openFileMappings.containsKey(new Long(chunkID))) {
			try {
				ByteBuffer buf = fileBackend.getChannel().map(MapMode.READ_WRITE, FILE_HEADER_SIZE + chunkID * (metric.getBlocksPerSuperblock() + 1) * metric.BLOCK_SIZE * 8, (metric.getBlocksPerSuperblock() + 1) * metric.BLOCK_SIZE * 8);
				
				openFileMappings.put(new Long(chunkID), buf.asLongBuffer());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return openFileMappings.get(new Long(chunkID));
	}

	@Override
	public long ReadFromSuperblock(long chunkID, int chunkOffset, int entry) {
		LongBuffer buffer = loadChunk(chunkID);
		
		synchronized (numChunksMutex) {			
			if (chunkID >= numChunks)
			{
				numChunks = chunkID + 1;
	
				for (int i = metric.getBlocksPerSuperblock() * metric.BLOCK_SIZE; i < (metric.getBlocksPerSuperblock() + 1) * metric.BLOCK_SIZE - 1; i++)
					buffer.put(i, -1);
				
				buffer.put((metric.getBlocksPerSuperblock() + 1) * metric.BLOCK_SIZE - 1, 0x53555041424C4F4BL);			
			}
		}

		return buffer.get(metric.getBlocksPerSuperblock() * metric.BLOCK_SIZE + chunkOffset * metric.SUPERBLOCK_ENTRIES + entry);
	}

	@Override
	public void WriteToSuperblock(long chunkID, int chunkOffset, int entry,
			long value) {
		LongBuffer buffer = loadChunk(chunkID);
		
		synchronized (numChunksMutex) {			
			if (chunkID >= numChunks)
			{
				numChunks = chunkID + 1;
	
				for (int i = metric.getBlocksPerSuperblock() * metric.BLOCK_SIZE; i < (metric.getBlocksPerSuperblock() + 1) * metric.BLOCK_SIZE - 1; i++)
					buffer.put(i, -1);
				
				buffer.put((metric.getBlocksPerSuperblock() + 1) * metric.BLOCK_SIZE - 1, 0x53555041424C4F4BL);
			}
		}

		buffer.put(metric.getBlocksPerSuperblock() * metric.BLOCK_SIZE + chunkOffset * metric.SUPERBLOCK_ENTRIES + entry, value);		
	}

	@Override
	public void Shutdown() {
		try {
			fileBackend.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
