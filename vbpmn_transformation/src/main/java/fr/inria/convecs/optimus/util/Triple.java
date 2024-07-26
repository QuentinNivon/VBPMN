package fr.inria.convecs.optimus.util;

public class Triple<U,V,W>
{
	private final U first;
	private final Pair<V, W> secondAndThird;

	public Triple(final U first,
				  final V second,
				  final W third)
	{
		this.first = first;
		this.secondAndThird = new Pair<>(second, third);
	}

	public U getFirst()
	{
		return this.first;
	}

	public V getSecond()
	{
		return this.secondAndThird.getFirst();
	}

	public W getThird()
	{
		return this.secondAndThird.getSecond();
	}
}
