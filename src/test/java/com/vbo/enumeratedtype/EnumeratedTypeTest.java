package com.vbo.enumeratedtype;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;

public class EnumeratedTypeTest {

	public static class MockEnum extends EnumeratedType {
		private static final long serialVersionUID = 1L;

		private static final EnumeratedTypeStorage STORAGE = new EnumeratedTypeStorage(MockEnum.class);

		public MockEnum(String key, String name) {
			super(key, name);
		}

		protected EnumeratedTypeStorage getStorage() {
			return STORAGE;
		}

		@SuppressWarnings("unchecked")
		public static <T extends EnumeratedType> T get(String key) {
			return (T) STORAGE.get(key);
		}

		public static Collection<? extends EnumeratedType> getValues() {
			return STORAGE.getValues();
		}

	}

	@Test
	public void testType() {
		MockEnum e = new MockEnum("T", "test");
		Assert.assertEquals("test", e.getName());
		Assert.assertEquals("T", e.getKey());

		Assert.assertEquals(2, MockEnum.getValues().size());
		Assert.assertTrue(MockEnum.getValues().contains(e));
		Assert.assertEquals(e, MockEnum.get("T"));

		e = new MockEnum("a", "àouf");
		Assert.assertEquals("àouf", e.getName());
		Assert.assertEquals("a", e.getKey());
		Assert.assertEquals(e, MockEnum.get("a"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNewWithoutKey() {
		@SuppressWarnings("unused")
		MockEnum e = new MockEnum("", "aaa");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNewWithNullKey() {
		@SuppressWarnings("unused")
		MockEnum e = new MockEnum(null, "sss");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNewWithoutName() {
		@SuppressWarnings("unused")
		MockEnum e = new MockEnum("àouf", "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNewWithNullName() {
		@SuppressWarnings("unused")
		MockEnum e = new MockEnum("àouf", null);
	}
}