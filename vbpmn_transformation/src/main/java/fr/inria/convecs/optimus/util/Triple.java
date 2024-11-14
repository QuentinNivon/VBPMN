package fr.inria.convecs.optimus.util;

public class Triple<U, V, W>
{
	private final U firstElement;
	private final V secondElement;
	private final W thirdElement;

	public Triple(final U firstElement,
				  final V secondElement,
				  final W thirdElement)
	{
		this.firstElement = firstElement;
		this.secondElement = secondElement;
		this.thirdElement = thirdElement;
	}

	public static <U, V, W> Triple<U, V, W> of(final U firstElement,
											   final V secondElement,
											   final W thirdElement)
	{
		return new Triple<>(firstElement, secondElement, thirdElement);
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

	public V getMiddle()
	{
		return this.getSecondElement();
	}

	public W getThirdElement()
	{
		return this.thirdElement;
	}

	public W getRight()
	{
		return this.getThirdElement();
	}
}
