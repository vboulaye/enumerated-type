package com.vbo.enumeratedtype;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

/**
 * base class for enums that can be extended...
 *
 * based on the org.apache.wicket.util.lang.EnumeratedType from wicket.
 *
 */
public class EnumeratedTypeStorage implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(EnumeratedTypeStorage.class);

	private final Class<? extends EnumeratedType> enumeratedTypeClass;

	/** Map of type values by class */
	private final Map<String, EnumeratedType> valuesByKey = Maps.newHashMap();
	// private final Map<String, String> importStringToKey = Maps.newHashMap();
	// private final Map<String, String> keyToExportString = Maps.newHashMap();
	private final Map<String, BiMap<String, String>> exportMappingsByReferential = Maps.newHashMap();

	/**
	 * in strict mode referencing an unknown key triggers an error.
	 */
	private boolean strictMode;

	public EnumeratedTypeStorage(Class<? extends EnumeratedType> enumeratedTypeClass) {
		super();
		this.enumeratedTypeClass = enumeratedTypeClass;

		forceConstantFieldsLoad(enumeratedTypeClass);
		initializeFromResourceBundle(enumeratedTypeClass);
	}

	public void addEnumeratedType(EnumeratedType t) {
		Assert.notNull(t);
		if (valuesByKey.containsKey(t.getKey())) {
			throw new IllegalArgumentException("the enum for this key '" + t.getKey() + "' is already present in "
					+ valuesByKey);
		}
		LOG.info("adding enumerated type {} : {}", t.getClass(), t);
		valuesByKey.put(t.getKey(), t);

		// initialize a default mapping with the mapping name
		addKeyAlias(t.getName(), t.getKey());

	}

	protected void addKeyAlias(String alias, String key) {
		Assert.hasLength(alias);
		Assert.hasLength(key);
		EnumeratedType mappedValue = valuesByKey.get(alias);
		if (mappedValue == null) {
			LOG.info("adding conversion from {} to {}({})", new Object[] { alias, enumeratedTypeClass, key });
			valuesByKey.put(alias, valuesByKey.get(key));
		} else if (!alias.equals(key)) {
			LOG.warn("trying to replace an existing conversion from {} to {}({})", new Object[] { alias,
					enumeratedTypeClass, key });
			LOG.warn("the existing conversion is from {} to {}({})", new Object[] { alias, enumeratedTypeClass,
					mappedValue });
		}
	}

	/**
	 * trigger the load of all static fields
	 *
	 * @param c
	 */
	void forceConstantFieldsLoad(Class<?> c) {
		ReflectionUtils.doWithFields(c, new ReflectionUtils.FieldCallback() {

			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				field.get(null);
			}
		}, new ReflectionUtils.FieldFilter() {

			public boolean matches(Field field) {
				return ReflectionUtils.isPublicStaticFinal(field);
			}
		});
	}

	private void initializeFromResourceBundle(Class<?> clazz) {
		Assert.notNull(clazz);
		try {
			ResourceBundle rb = ResourceBundle.getBundle(clazz.getName());

			Enumeration<String> keys = rb.getKeys();
			while (keys.hasMoreElements()) {
				String bundleKey = keys.nextElement();
				String bundleValue = rb.getString(bundleKey);
				if (bundleValue != null && bundleValue.length() > 0) {
					loadAliasKey(bundleKey, bundleValue);
				}
			}
		} catch (MissingResourceException e) {
			LOG.info("unable to load custom enumerated type extension from {} properties bundle", clazz);
		}
	}

	private void loadAliasKey(String keyPath, String key) {
		String prefix = "alias.";
		if (keyPath.startsWith(prefix) //
				&& keyPath.length() > prefix.length()) {
			String conversionName = keyPath.substring(prefix.length());
			addKeyAlias(conversionName, key);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends EnumeratedType> T map(String alias) {
		if (alias == null || alias.length() == 0) {
			return null;
		} else {
			EnumeratedType result = get(alias);
			if (result == null) {
				throw new IllegalArgumentException("invalid " + enumeratedTypeClass.getSimpleName()
						+ " enum name : unable to find a conversion for " + alias);
			} else {
				return (T) result;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends EnumeratedType> T get(String key) {
		return (T) valuesByKey.get(key);
	}

	public Collection<? extends EnumeratedType> getValues() {
		return valuesByKey.values();
	}

	public String export(EnumeratedType enumeratedType, String referential) {
		if (enumeratedType == null) {
			return null;
		} else {
			BiMap<String, String> exportsByKey = getOrInitExportMappings(referential);
			String mappedExport = exportsByKey.get(enumeratedType.getKey());
			return Objects.firstNonNull(mappedExport, enumeratedType.getName());
		}
	}

	public EnumeratedType convert(String referential, String localName) {
		if (localName == null) {
			return null;
		} else {
			BiMap<String, String> exportsByKey = getOrInitExportMappings(referential);
			String key = exportsByKey.get(localName);
			Assert.notNull(key, "the name " + localName + " is not defined as a " + enumeratedTypeClass.getSimpleName()
					+ " in referential " + referential);
			return get(key);
		}
	}

	private BiMap<String, String> getOrInitExportMappings(String referential) {
		BiMap<String, String> exportsByKey = exportMappingsByReferential.get(referential);
		if (exportsByKey == null) {
			synchronized (exportMappingsByReferential) {
				exportsByKey = exportMappingsByReferential.get(referential);
				if (exportsByKey == null) {
					BiMap<String, String> exports = getExportsForReferential(referential);
					exportMappingsByReferential.put(referential, exports);
				}
			}
		}
		return exportsByKey;
	}

	private BiMap<String, String> getExportsForReferential(String referential) {
		BiMap<String, String> exports = HashBiMap.create();
		try {
			ResourceBundle rb = ResourceBundle.getBundle(enumeratedTypeClass.getName(), new Locale(referential));
			ArrayList<String> listOfKeys = Collections.list(rb.getKeys());
			for (String key : listOfKeys) {
				String exportString = rb.getString(key);
				exports.put(key, exportString);
			}
		} catch (MissingResourceException e) {
			LOG.info("no mappings found for referential {} in enum {}", referential, enumeratedTypeClass);
		}
		return exports;
	}

	public boolean isStrictMode() {
		return strictMode;
	}

	public void setStrictMode(boolean strictMode) {
		this.strictMode = strictMode;
	}

}
