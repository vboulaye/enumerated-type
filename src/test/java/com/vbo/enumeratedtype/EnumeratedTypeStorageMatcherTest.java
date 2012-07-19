package com.vbo.enumeratedtype;

import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.base.Predicate;

public class EnumeratedTypeStorageMatcherTest {

	private static class MockEnumeratedType extends EnumeratedType {
		private static final long serialVersionUID = 373577105500475470L;

		private final static EnumeratedTypeStorage STORAGE = new EnumeratedTypeStorage(MockEnumeratedType.class);

		private static final MockEnumeratedType M = new MockEnumeratedType("mock", "M");
		private static final MockEnumeratedType N = new MockEnumeratedType("nock", "N");

		public MockEnumeratedType(String name, String key) {
			super(name, key);
		}

		protected EnumeratedTypeStorage getStorage() {
			return STORAGE;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetupNull() {
		EnumeratedTypeMatcher<MockEnumeratedType, Object> matcher = new EnumeratedTypeMatcher<MockEnumeratedType, Object>();
		matcher.setup(null, new Predicate<Object>() {

			public boolean apply(Object input) {
				return false;
			}
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetupNull2() {
		EnumeratedTypeMatcher<MockEnumeratedType, Object> matcher = new EnumeratedTypeMatcher<MockEnumeratedType, Object>();
		matcher.setup(MockEnumeratedType.M, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetupDuplicate() {
		EnumeratedTypeMatcher<MockEnumeratedType, Object> matcher = new EnumeratedTypeMatcher<MockEnumeratedType, Object>();
		matcher.setup(MockEnumeratedType.M, new Predicate<Object>() {

			public boolean apply(Object input) {
				return false;
			}
		});
		matcher.setup(MockEnumeratedType.M, new Predicate<Object>() {

			public boolean apply(Object input) {
				return false;
			}
		});
	}

	public void testMatcher() {

		final AtomicBoolean atomicBoolean = new AtomicBoolean();
		atomicBoolean.set(false);
		EnumeratedTypeMatcher<MockEnumeratedType, Object> matcher = new EnumeratedTypeMatcher<MockEnumeratedType, Object>();
		matcher.setup(MockEnumeratedType.M, new Predicate<Object>() {

			public boolean apply(Object input) {
				atomicBoolean.set(true);
				return true;
			}
		});

		Assert.assertEquals(false, matcher.matches(MockEnumeratedType.N, null));

		Assert.assertEquals(false, atomicBoolean.get());
		Assert.assertEquals(true, matcher.matches(MockEnumeratedType.M, null));
		Assert.assertEquals(true, atomicBoolean.get());
	}

}
