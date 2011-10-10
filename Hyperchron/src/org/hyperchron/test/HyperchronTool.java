package org.hyperchron.test;

import java.io.FileNotFoundException;
import java.nio.LongBuffer;

import org.hyperchron.blocks.BlockStore;
import org.hyperchron.blocks.BlockStoreFactory;
import org.hyperchron.blocks.BlockStoreMetric;
import org.hyperchron.impl.EntityDescriptor;
import org.hyperchron.impl.HyperchronMetrics;
import org.hyperchron.impl.TreeLeaf;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;

public class HyperchronTool {
	protected static String databaseFileName = "";
	protected static String blockDB = "";
	protected static String uuid = null;
	
	public static void printHelp() {
		System.out.println("Usage: org.hyperchron.test.HyperchronTool [options]");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("  -h\t\tshow this help message and exit");
		System.out.println("");		
		System.out.println("  -df DATABASE_FILE");
		System.out.println("  -bf BLOCKDB_FILE");
		System.out.println("  -u UUID");
		System.out.println("  -v \t\tverbose output");
		System.out.println("");		
		
		System.exit(0);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int argIndex = 0; 

		if (args.length == 0)
			printHelp();

		while (argIndex < args.length) {
			String arg = args[argIndex++];
			
			if (arg.equals("-h")) {
				printHelp();
			}
			
			if (arg.equals("-df")) {
				databaseFileName = args[argIndex++];
			}			

			if (arg.equals("-bf")) {
				blockDB = args[argIndex++];
			}
			
			if (arg.equals("-u")) {
				uuid = args[argIndex++];
			}									
		}
		
		if (uuid != null) {
			ObjectContainer entityDB = Db4oEmbedded.openFile(databaseFileName);
			
			final Query query = entityDB.query();
			query.constrain(EntityDescriptor.class);
			query.descend("uuid").constrain(uuid);
	
			ObjectSet<EntityDescriptor> qresult = query.execute();

			if (qresult.hasNext()) {
				long targetEntityID = qresult.next().entityID;
				
				try {
					BlockStore blockStore = BlockStoreFactory.openBlockStore(blockDB, new BlockStoreMetric(HyperchronMetrics.BLOCK_SIZE, HyperchronMetrics.SUPERBLOCK_ENTRIES));
					
					int chunk = 0;
					int chunkOffset = 0;
					
					while (true) {
						long entityID = blockStore.ReadFromSuperblock(chunk, chunkOffset, HyperchronMetrics.SUPERBLOCK_ENTRY_ENTITY_ID);
						
						if (entityID == -1)
							break;
						
						if (entityID == targetEntityID) {
							int blockLength = ((int)blockStore.ReadFromSuperblock(chunk, chunkOffset, HyperchronMetrics.SUPERBLOCK_ENTRY_LENGTH));

							LongBuffer chunkBuffer = blockStore.loadChunk(chunk);
							
							for (int i = 0; i < blockLength; i++) {
								long timeStamp = chunkBuffer.get(chunkOffset * HyperchronMetrics.BLOCK_SIZE + i);
								
								System.out.println(Long.toString(timeStamp));
							}
						}
						
						if (++chunkOffset >= HyperchronMetrics.blocksPerSuperblock) {
							chunkOffset = 0;
							chunk++;
						}
					}
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				System.out.println ("Entity " + uuid + " not found.");
			}
		}
	}
}
