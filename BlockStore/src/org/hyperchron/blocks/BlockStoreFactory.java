package org.hyperchron.blocks;

import java.io.File;
import java.io.FileNotFoundException;

import org.hyperchron.blocks.impl.MappedBlockStore;

public class BlockStoreFactory {
	public static BlockStore openBlockStore(String filename, BlockStoreMetric metric) throws FileNotFoundException {
		return openBlockStore(new File(filename), metric);
	}
	
	public static BlockStore openBlockStore(File file, BlockStoreMetric metric) throws FileNotFoundException {
		return new MappedBlockStore(file, metric);
	}
}
