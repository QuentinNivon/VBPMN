package fr.inria.convecs.optimus.util;

public class Pair<U, V>
{
	private final U firstElement;
	private final V secondElement;

	public Pair(final U firstElement,
				final V secondElement)
	{
		this.firstElement = firstElement;
		this.secondElement = secondElement;
	}

	public static <U, V> Pair<U, V> of(final U firstElement,
									   final V secondElement)
	{
		return new Pair<>(firstElement, secondElement);
	}

	public U getFirstElement()
	{
		return this.firstElement;
	}

	public U getLeft()
	{
		return this.getFirstElement();
	}

	public V getSecondElement()
	{
		return this.secondElement;
	}

	public V getRight()
	{
		return this.getSecondElement();
	}
}
