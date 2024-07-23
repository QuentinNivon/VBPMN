package fr.inria.convecs.optimus.util;

public class Pair<U,V>
{
	private final U first;
	private final V second;

	public Pair(final U first,
				final V second)
	{
		this.first = first;
		this.second = second;
	}

	public U getFirst()
	{
		return this.first;
	}

	public V getSecond()
	{
		return this.second;
	}
}
