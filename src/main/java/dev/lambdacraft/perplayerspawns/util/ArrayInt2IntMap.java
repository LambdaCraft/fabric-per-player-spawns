package dev.lambdacraft.perplayerspawns.util;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.lang.reflect.Array;
import java.util.Collection;

public class ArrayInt2IntMap extends AbstractObject2IntMap<Integer> {
	private final int[] array;

	public ArrayInt2IntMap(int[] array) {this.array = array;}


	@Override
	public int size() {
		return this.array.length;
	}

	public void set(int key, int value) {
		this.array[key] = value;
	}



	@Override
	public ObjectSet<Entry<Integer>> object2IntEntrySet() {
		throw new UnsupportedOperationException("cease");
	}

	@Override
	public int getInt(Object key) {
		if(key instanceof Integer)
			return this.array[(Integer) key];
		if(key instanceof Enum)
			return this.array[((Enum) key).ordinal()];
		throw new IllegalArgumentException("not an integer");
	}
}
