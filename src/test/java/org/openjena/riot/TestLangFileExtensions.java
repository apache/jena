package org.openjena.riot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.openjena.atlas.junit.BaseTest;
import org.openjena.riot.Lang;

public class TestLangFileExtensions extends BaseTest
{

	@Test
	public void testFileExtensionsProvided()
	{
		for (Lang l : Lang.values())
		{
			String ext = l.getDefaultFileExtension();
			Assert.assertFalse( l+" does not have default extension defined",  ext==null||ext.isEmpty() );
			Assert.assertNotNull( l+" does not have file extensions defined", l.getFileExtensions());
			Assert.assertTrue( l+" does not have file extensions defined", l.getFileExtensions().length > 0);
		}
	}
	
	@Test
	public void testFileExtensionUnique()
	{
		Collection<String> exts = new ArrayList<String>();
		for (Lang l : Lang.values())
		{
			for (String ext : l.getFileExtensions())
			{
				Assert.assertFalse( "The "+ext+" file extensions in "+l+" was already used",
						exts.contains(ext));
			}
			exts.addAll( Arrays.asList(l.getFileExtensions()));
		}
		
	}
	
	@Test
	public void testDefaultInExtensions()
	{
		for (Lang l : Lang.values())
		{
			Assert.assertTrue( l+" default extension not in file extensions list", Arrays.asList( l.getFileExtensions()).contains( l.getDefaultFileExtension())  );
		}
	}
	
}
