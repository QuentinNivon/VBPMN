package fr.inria.convecs.optimus.util;

public class Pair<U,V>
{
	private U first;
	private V second;

	public Pair(final U first,
				final V second)
	{
		this.first = first;
		this.second = second;
	}

	public Pair()
	{
		this.first = null;
		this.second = null;
	}

	public U getFirst()
	{
		return this.first;
	}

	public V getSecond()
	{
		return this.second;
	}

	public void setFirst(final U elem)
	{
		this.first = elem;
	}

	public void setSecond(final V elem)
	{
		this.second = elem;
	}

	@Override
	public String toString()
	{
		return "Pair of (" + this.first.toString() + ", " + this.second.toString() + ")";
	}
}
