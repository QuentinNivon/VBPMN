package fr.inria.convecs.optimus.compatibility;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public abstract class GenericTest
{
	public GenericTest()
	{

	}

	@Test
	public abstract void test() throws IOException, InterruptedException;
}
