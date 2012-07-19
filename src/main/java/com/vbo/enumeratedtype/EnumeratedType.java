package com.vbo.enumeratedtype;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * base class for enums that can be extended...
 *
 * based on the org.apache.wicket.util.lang.EnumeratedType from wicket.
 *
 * @deprecated might not be useful!
 */
@Deprecated
public abstract class EnumeratedType implements Serializable, Comparable<EnumeratedType> {

	private static final long serialVersionUID = 1L;

	private final String key;
	private final String name;

	// private final Map<String, String> exports = Maps.newHashMap();

	protected abstract EnumeratedTypeStorage getStorage();

	/**
	 * @param name
	 *            Name of this enumerated type value
	 */
	public EnumeratedType(String key, String name) {
		super();
		Assert.hasLength(key);
		Assert.hasLength(name);

		this.key = key;
		this.name = name;

		// Add this object to the list of values for our subclass
		getStorage().addEnumeratedType(this);
	}

	// public void putExport(String referential, String export) {
	// exports.put(referential, export);
	// }
	//
	// public String getExport(String referential) {
	// return Objects.firstNonNull(exports.get(referential), name);
	// }
	//
	// public Collection<String> getExportReferentials() {
	// return exports.values();
	// }

	/**
	 * Method to ensure that == works after deserialization
	 *
	 * @return object instance
	 * @throws java.io.ObjectStreamException
	 */
	public Object readResolve() throws java.io.ObjectStreamException {
		EnumeratedTypeStorage storage = getStorage();
		EnumeratedType result = storage.get(this.key);

		// fallback when the enum does not exist at all
		if (result == null) {
			synchronized (storage) {
				result = storage.get(this.key);
				if (result == null) {
					storage.addEnumeratedType(this);
					result = this;
				}
			}
		}

		return result;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EnumeratedType other = (EnumeratedType) obj;
		return key.equals(other.key);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(EnumeratedType o) {
		if (this == o) {
			return 0;
		} else if (o == null) {
			return 1;
		} else {
			return this.key.compareTo(o.key);
		}
	}

}
