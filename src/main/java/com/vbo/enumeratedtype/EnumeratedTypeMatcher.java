package com.vbo.enumeratedtype;

import java.util.Map;

import org.springframework.util.Assert;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class EnumeratedTypeMatcher<T extends EnumeratedType, P> {

	private final Map<String, Predicate<P>> predicates = Maps.newHashMap();

	public EnumeratedTypeMatcher<T, P> setup(T key, Predicate<P> function) {
		Assert.notNull(key);
		Assert.notNull(function);
		Assert.isTrue(!predicates.containsKey(key.getKey()));
		predicates.put(key.getKey(), function);
		return this;
	}

	public boolean matches(T key, P payload) {
		Predicate<P> predicate = predicates.get(key.getKey());
		if (predicate == null) {
			return false;
		} else {
			return predicate.apply(payload);
		}
	}
}
