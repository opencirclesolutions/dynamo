//package com.vaadin.flow.server;
//
//import static com.vaadin.flow.server.Constants.EXTERNAL_STATS_FILE;
//import static com.vaadin.flow.server.Constants.EXTERNAL_STATS_FILE_TOKEN;
//import static com.vaadin.flow.server.Constants.EXTERNAL_STATS_URL;
//import static com.vaadin.flow.server.Constants.EXTERNAL_STATS_URL_TOKEN;
//import static com.vaadin.flow.server.Constants.FRONTEND_TOKEN;
//import static com.vaadin.flow.server.Constants.NPM_TOKEN;
//import static com.vaadin.flow.server.Constants.VAADIN_PREFIX;
//import static com.vaadin.flow.server.Constants.VAADIN_SERVLET_RESOURCES;
//import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_COMPATIBILITY_MODE;
//import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_ENABLE_DEV_SERVER;
//import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_PRODUCTION_MODE;
//import static com.vaadin.flow.server.InitParameters.SERVLET_PARAMETER_REUSE_DEV_SERVER;
//import static com.vaadin.flow.server.frontend.FrontendUtils.PARAM_TOKEN_FILE;
//import static com.vaadin.flow.server.frontend.FrontendUtils.PROJECT_BASEDIR;
//import static com.vaadin.flow.server.frontend.FrontendUtils.TOKEN_FILE;
//
///*
// * Copyright 2000-2020 Vaadin Ltd.
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//
//import java.io.File;
//import java.io.IOException;
//import java.io.Serializable;
//import java.io.UncheckedIOException;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.Properties;
//
//import org.apache.commons.io.FileUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.vaadin.flow.component.UI;
//import com.vaadin.flow.di.Lookup;
//import com.vaadin.flow.di.ResourceProvider;
//import com.vaadin.flow.function.DeploymentConfiguration;
//import com.vaadin.flow.function.SerializableConsumer;
//import com.vaadin.flow.internal.AnnotationReader;
//import com.vaadin.flow.server.frontend.FallbackChunk;
//import com.vaadin.flow.server.frontend.FrontendUtils;
//
//import elemental.json.JsonObject;
//import elemental.json.impl.JsonUtil;
//
///**
// * Creates {@link DeploymentConfiguration} filled with all parameters specified
// * by the framework users.
// *
// * @since 1.2
// */
//public final class DeploymentConfigurationFactory implements Serializable {
//
//	private static final long serialVersionUID = -5754283329664089883L;
//
//	@SuppressWarnings("serial")
//	public static final Object DEV_MODE_ENABLE_STRATEGY = new Serializable() {
//	};
//
//	@SuppressWarnings("serial")
//	public static final Object FALLBACK_CHUNK = new Serializable() {
//	};
//
//	public static final String ERROR_COMPATIBILITY_MODE_UNSET = "Unable to determine mode of operation. To use npm mode, ensure "
//			+ "'flow-build-info.json' exists on the classpath. With Maven, "
//			+ "this is handled by the 'prepare-frontend' goal. To use "
//			+ "compatibility mode, add the 'flow-server-compatibility-mode' "
//			+ "dependency. If using Vaadin with Spring Boot, instead set the "
//			+ "property 'vaadin.compatibilityMode' to 'true' in " + "'application.properties'.";
//
//	public static final String ERROR_DEV_MODE_NO_FILES = "The compatibility mode is explicitly set to 'false', "
//			+ "but there are neither 'flow-build-info.json' nor 'webpack.config.js' file available in "
//			+ "the project/working directory. Ensure 'webpack.config.js' is present or trigger creation of "
//			+ "'flow-build-info.json' via running 'prepare-frontend' Maven goal.";
//
//	public static final String DEV_FOLDER_MISSING_MESSAGE = "Running project in development mode with no access to folder '%s'.%n"
//			+ "Build project in production mode instead, see https://vaadin.com/docs/v14/flow/production/tutorial-production-mode-basic.html";
//	private static final Logger logger = LoggerFactory.getLogger(DeploymentConfigurationFactory.class);
//
//	private DeploymentConfigurationFactory() {
//	}
//
//	/**
//	 * Creates a {@link DeploymentConfiguration} instance that is filled with all
//	 * parameters, specified for the current app.
//	 *
//	 * @param systemPropertyBaseClass the class to look for properties defined with
//	 *                                annotations
//	 * @param vaadinConfig            the config to get the rest of the properties
//	 *                                from
//	 * @return {@link DeploymentConfiguration} instance
//	 * @throws VaadinConfigurationException thrown if property construction fails
//	 */
//	public static DeploymentConfiguration createDeploymentConfiguration(Class<?> systemPropertyBaseClass,
//			VaadinConfig vaadinConfig) throws VaadinConfigurationException {
//		return new DefaultDeploymentConfiguration(systemPropertyBaseClass,
//				createInitParameters(systemPropertyBaseClass, vaadinConfig));
//	}
//
//	/**
//	 * Creates a {@link DeploymentConfiguration} instance that has all parameters,
//	 * specified for the current app without doing checks so property states and
//	 * only returns default.
//	 *
//	 * @param systemPropertyBaseClass the class to look for properties defined with
//	 *                                annotations
//	 * @param vaadinConfig            the config to get the rest of the properties
//	 *                                from
//	 * @return {@link DeploymentConfiguration} instance
//	 * @throws VaadinConfigurationException thrown if property construction fails
//	 */
//	public static DeploymentConfiguration createPropertyDeploymentConfiguration(Class<?> systemPropertyBaseClass,
//			VaadinConfig vaadinConfig) throws VaadinConfigurationException {
//		return new PropertyDeploymentConfiguration(systemPropertyBaseClass,
//				createInitParameters(systemPropertyBaseClass, vaadinConfig));
//	}
//
//	/**
//	 * Generate Property containing parameters for with all parameters contained in
//	 * current application.
//	 *
//	 * @param systemPropertyBaseClass the class to look for properties defined with
//	 *                                annotations
//	 * @param vaadinConfig            the config to get the rest of the properties
//	 *                                from
//	 * @return {@link Properties} instance
//	 * @throws VaadinConfigurationException thrown if property construction fails
//	 */
//	protected static Properties createInitParameters(Class<?> systemPropertyBaseClass, VaadinConfig vaadinConfig)
//			throws VaadinConfigurationException {
//		Properties initParameters = new Properties();
//		readUiFromEnclosingClass(systemPropertyBaseClass, initParameters);
//		readConfigurationAnnotation(systemPropertyBaseClass, initParameters);
//
//		// Read default parameters from server.xml
//		final VaadinContext context = vaadinConfig.getVaadinContext();
//		for (final Enumeration<String> e = context.getContextParameterNames(); e.hasMoreElements();) {
//			final String name = e.nextElement();
//			initParameters.setProperty(name, context.getContextParameter(name));
//		}
//
//		// Override with application config from web.xml
//		for (final Enumeration<String> e = vaadinConfig.getConfigParameterNames(); e.hasMoreElements();) {
//			final String name = e.nextElement();
//			initParameters.setProperty(name, vaadinConfig.getConfigParameter(name));
//		}
//
//		readBuildInfo(systemPropertyBaseClass, initParameters, vaadinConfig.getVaadinContext());
//		return initParameters;
//	}
//
//	private static void readBuildInfo(Class<?> systemPropertyBaseClass, Properties initParameters,
//			VaadinContext context) {
//		String json = getTokenFileContents(systemPropertyBaseClass, initParameters, context);
//
//		// Read the json and set the appropriate system properties if not
//		// already set.
//		if (json != null) {
//			JsonObject buildInfo = JsonUtil.parse(json);
//			setInitParametersUsingTokenData(initParameters, buildInfo);
//
//			FallbackChunk fallbackChunk = FrontendUtils.readFallbackChunk(buildInfo);
//			if (fallbackChunk != null) {
//				initParameters.put(FALLBACK_CHUNK, fallbackChunk);
//			}
//		}
//
//		try {
//			boolean hasWebPackConfig = hasWebpackConfig(initParameters);
//			boolean hasTokenFile = json != null;
//			SerializableConsumer<CompatibilityModeStatus> strategy = value -> verifyMode(value, hasTokenFile,
//					hasWebPackConfig);
//			initParameters.put(DEV_MODE_ENABLE_STRATEGY, strategy);
//		} catch (IOException e) {
//			throw new UncheckedIOException(e);
//		}
//
//	}
//
//	@SuppressWarnings("deprecation")
//	private static void setInitParametersUsingTokenData(Properties initParameters, JsonObject buildInfo) {
//		if (buildInfo.hasKey(SERVLET_PARAMETER_PRODUCTION_MODE)) {
//			initParameters.setProperty(SERVLET_PARAMETER_PRODUCTION_MODE,
//					String.valueOf(buildInfo.getBoolean(SERVLET_PARAMETER_PRODUCTION_MODE)));
//		}
//		if (buildInfo.hasKey(EXTERNAL_STATS_FILE_TOKEN) || buildInfo.hasKey(EXTERNAL_STATS_URL_TOKEN)) {
//			// If external stats file is flagged then compatibility mode and
//			// dev server should both be false - only variable that can
//			// be configured, in addition to stats variables, is
//			// production mode
//			initParameters.setProperty(SERVLET_PARAMETER_COMPATIBILITY_MODE, Boolean.toString(false));
//			initParameters.setProperty(SERVLET_PARAMETER_ENABLE_DEV_SERVER, Boolean.toString(false));
//			initParameters.setProperty(EXTERNAL_STATS_FILE, Boolean.toString(true));
//			if (buildInfo.hasKey(EXTERNAL_STATS_URL_TOKEN)) {
//				initParameters.setProperty(EXTERNAL_STATS_URL, buildInfo.getString(EXTERNAL_STATS_URL_TOKEN));
//			}
//			// NO OTHER CONFIGURATION:
//			return;
//		}
//		if (buildInfo.hasKey(SERVLET_PARAMETER_COMPATIBILITY_MODE)) {
//			initParameters.setProperty(SERVLET_PARAMETER_COMPATIBILITY_MODE,
//					String.valueOf(buildInfo.getBoolean(SERVLET_PARAMETER_COMPATIBILITY_MODE)));
//			// Need to be sure that we remove the system property,
//			// because it has priority in the configuration getter
//			System.clearProperty(VAADIN_PREFIX + SERVLET_PARAMETER_COMPATIBILITY_MODE);
//		}
//
//		if (buildInfo.hasKey(NPM_TOKEN)) {
//			initParameters.setProperty(PROJECT_BASEDIR, buildInfo.getString(NPM_TOKEN));
//			verifyFolderExists(initParameters, buildInfo.getString(NPM_TOKEN));
//		}
//
//		if (buildInfo.hasKey(FRONTEND_TOKEN)) {
//			initParameters.setProperty(FrontendUtils.PARAM_FRONTEND_DIR, buildInfo.getString(FRONTEND_TOKEN));
//			// Only verify frontend folder if it's not a subfolder of the
//			// npm folder.
//			if (!buildInfo.hasKey(NPM_TOKEN)
//					|| !buildInfo.getString(FRONTEND_TOKEN).startsWith(buildInfo.getString(NPM_TOKEN))) {
//				verifyFolderExists(initParameters, buildInfo.getString(FRONTEND_TOKEN));
//			}
//		}
//
//		// These should be internal only so if there is a System
//		// property override then the user probably knows what
//		// they are doing.
//		if (buildInfo.hasKey(SERVLET_PARAMETER_ENABLE_DEV_SERVER)) {
//			initParameters.setProperty(SERVLET_PARAMETER_ENABLE_DEV_SERVER,
//					String.valueOf(buildInfo.getBoolean(SERVLET_PARAMETER_ENABLE_DEV_SERVER)));
//		}
//		if (buildInfo.hasKey(SERVLET_PARAMETER_REUSE_DEV_SERVER)) {
//			initParameters.setProperty(SERVLET_PARAMETER_REUSE_DEV_SERVER,
//					String.valueOf(buildInfo.getBoolean(SERVLET_PARAMETER_REUSE_DEV_SERVER)));
//		}
//
//		setDevModePropertiesUsingTokenData(initParameters, buildInfo);
//	}
//
//	private static void setDevModePropertiesUsingTokenData(Properties initParameters, JsonObject buildInfo) {
//		// read dev mode properties from the token and set init parameter only
//		// if it's not yet set
//		if (initParameters.getProperty(InitParameters.SERVLET_PARAMETER_ENABLE_PNPM) == null
//				&& buildInfo.hasKey(InitParameters.SERVLET_PARAMETER_ENABLE_PNPM)) {
//			initParameters.setProperty(InitParameters.SERVLET_PARAMETER_ENABLE_PNPM,
//					String.valueOf(buildInfo.getBoolean(InitParameters.SERVLET_PARAMETER_ENABLE_PNPM)));
//		}
//		if (initParameters.getProperty(InitParameters.REQUIRE_HOME_NODE_EXECUTABLE) == null
//				&& buildInfo.hasKey(InitParameters.REQUIRE_HOME_NODE_EXECUTABLE)) {
//			initParameters.setProperty(InitParameters.REQUIRE_HOME_NODE_EXECUTABLE,
//					String.valueOf(buildInfo.getBoolean(InitParameters.REQUIRE_HOME_NODE_EXECUTABLE)));
//		}
//	}
//
//	private static String getTokenFileContents(Class<?> systemPropertyBaseClass, Properties initParameters,
//			VaadinContext context) {
//		String json = null;
//		try {
//			json = getResourceFromFile(initParameters);
//			if (json == null) {
//				json = getTokenFileFromClassloader(systemPropertyBaseClass, context);
//			}
//		} catch (IOException e) {
//			throw new UncheckedIOException(e);
//		}
//		return json;
//	}
//
//	private static String getResourceFromFile(Properties initParameters) throws IOException {
//		String json = null;
//		// token file location passed via init parameter property
//		String tokenLocation = initParameters.getProperty(PARAM_TOKEN_FILE);
//		if (tokenLocation != null) {
//			File tokenFile = new File(tokenLocation);
//			if (tokenFile != null && tokenFile.canRead()) {
//				json = FileUtils.readFileToString(tokenFile, StandardCharsets.UTF_8);
//			}
//		}
//		return json;
//	}
//
//	private static String getTokenFileFromClassloader(Class<?> contextClass, VaadinContext context) throws IOException {
//		String tokenResource = VAADIN_SERVLET_RESOURCES + TOKEN_FILE;
//
//		Lookup lookup = context.getAttribute(Lookup.class);
//		if (lookup == null) {
//			return null;
//		}
//
//		ResourceProvider resourceProvider = lookup.lookup(ResourceProvider.class);
//
//		List<URL> resources = resourceProvider.getApplicationResources(context, tokenResource);
//
//		// Accept resource that doesn't contain
//		// 'jar!/META-INF/Vaadin/config/flow-build-info.json'
//		URL resource = resources.stream().filter(url -> !url.getPath().endsWith("jar!/" + tokenResource)).findFirst()
//				.orElse(null);
//		if (resource == null && !resources.isEmpty()) {
//			// For no non jar build info, in production mode check for
//			// webpack.generated.json if it's in a jar then accept
//			// single jar flow-build-info.
//			return getPossibleJarResource(context, resources);
//		}
//		return resource == null ? null : FrontendUtils.streamToString(resource.openStream());
//
//	}
//
//	/**
//	 * Check if the webpack.generated.js resources is inside 2 jars (flow-server.jar
//	 * and application.jar) if this is the case then we can accept a build info file
//	 * from inside jar with a single jar in the path.
//	 * <p>
//	 * Else we will accept any flow-build-info and log a warning that it may not be
//	 * the correct file, but it's the best we could find.
//	 */
//	private static String getPossibleJarResource(VaadinContext context, List<URL> resources) throws IOException {
//		Objects.requireNonNull(resources);
//
//		Lookup lookup = context.getAttribute(Lookup.class);
//		ResourceProvider resourceProvider = lookup.lookup(ResourceProvider.class);
//
//		assert !resources.isEmpty() : "Possible jar resource requires resources to be available.";
//
//		URL webpackGenerated = resourceProvider.getApplicationResource(context, FrontendUtils.WEBPACK_GENERATED);
//
//		// If jar!/ exists 2 times for webpack.generated.json then we are
//		// running from a jar
//		if (webpackGenerated != null && countInstances(webpackGenerated.getPath(), "jar!/") >= 2) {
//			for (URL resource : resources) {
//				// As we now know that we are running from a jar we can accept a
//				// build info with a single jar in the path
//				if (countInstances(resource.getPath(), "jar!/") == 1) {
//					return FrontendUtils.streamToString(resource.openStream());
//				}
//			}
//		}
//		URL firstResource = resources.get(0);
//		if (resources.size() > 1) {
//			String warningMessage = String.format("Unable to fully determine correct flow-build-info.%n"
//					+ "Accepting file '%s' first match of '%s' possible.%n"
//					+ "Please verify flow-build-info file content.", firstResource.getPath(), resources.size());
//			logger.warn(warningMessage);
//		} else {
//			String debugMessage = String.format(
//					"Unable to fully determine correct flow-build-info.%n" + "Accepting file '%s'",
//					firstResource.getPath());
//			logger.debug(debugMessage);
//		}
//		return FrontendUtils.streamToString(firstResource.openStream());
//	}
//
//	private static int countInstances(String input, String value) {
//		return input.split(value, -1).length - 1;
//	}
//
//	/**
//	 * Verify that given folder actually exists on the system if we are not in
//	 * production mode.
//	 * <p>
//	 * If folder doesn't exist throw IllegalStateException saying that this should
//	 * probably be a production mode build.
//	 *
//	 * @param initParameters deployment init parameters
//	 * @param folder         folder to check exists
//	 */
//	private static void verifyFolderExists(Properties initParameters, String folder) {
//		Boolean productionMode = Boolean
//				.parseBoolean(initParameters.getProperty(SERVLET_PARAMETER_PRODUCTION_MODE, "false"));
//		if (!productionMode && !new File(folder).exists()) {
//			String message = String.format(DEV_FOLDER_MISSING_MESSAGE, folder);
//			throw new IllegalStateException(message);
//		}
//	}
//
//	private static void verifyMode(CompatibilityModeStatus value, boolean hasTokenFile, boolean hasWebpackConfig) {
//		// Don't handle the case when compatibility mode is enabled.
//
//		// If no compatibility mode setting is defined
//		// and the project/working directory doesn't contain an appropriate
//		// webpack.config.js, then show the error message.
//		if (value == CompatibilityModeStatus.UNDEFINED) {
//			if (!hasWebpackConfig) {
//				throw new IllegalStateException(ERROR_COMPATIBILITY_MODE_UNSET);
//			}
//		} else if (!hasTokenFile && !hasWebpackConfig) {
//			// If compatibility mode is explicitly set to false, no
//			// flow-build-info.json file exists, and no appropriate
//			// webpack.config.js is found in the current working directory, then
//			// show an error message that suggest either triggering creation of
//			// flow-build-info.json or ensuring webpack.config.js is present in
//			// the working directory.
//			throw new IllegalStateException(ERROR_DEV_MODE_NO_FILES);
//		}
//
//		// If flow-build-info.json doesn't exist, but an appropriate
//		// webpack.config.js is found in the working directory, then launch a
//		// dev server with configuration based on the project/working directory
//		// location
//		if (!hasTokenFile && hasWebpackConfig) {
//			// the current working directory will be used automatically by the
//			// dev server unless it's specified explicitly
//			logger.warn("Found 'webpack.config.js' in the project/working directory. "
//					+ "Will use it for webpack dev server.");
//		}
//	}
//
//	private static boolean hasWebpackConfig(Properties initParameters) throws IOException {
//		String baseDir = initParameters.getProperty(FrontendUtils.PROJECT_BASEDIR);
//		File projectBaseDir = baseDir == null ? new File(".") : new File(baseDir);
//		File webPackConfig = new File(projectBaseDir, FrontendUtils.WEBPACK_CONFIG);
//		return FrontendUtils.isWebpackConfigFile(webPackConfig);
//	}
//
//	private static void readUiFromEnclosingClass(Class<?> systemPropertyBaseClass, Properties initParameters) {
//		Class<?> enclosingClass = systemPropertyBaseClass.getEnclosingClass();
//
//		if (enclosingClass != null && UI.class.isAssignableFrom(enclosingClass)) {
//			initParameters.put(InitParameters.UI_PARAMETER, enclosingClass.getName());
//		}
//	}
//
//	/**
//	 * Read the VaadinServletConfiguration annotation for initialization name value
//	 * pairs and add them to the initial properties object.
//	 *
//	 * @param systemPropertyBaseClass base class for constructing the configuration
//	 * @param initParameters          current initParameters object
//	 * @throws VaadinConfigurationException exception thrown for failure in invoking
//	 *                                      method on configuration annotation
//	 */
//	@SuppressWarnings("deprecation")
//	private static void readConfigurationAnnotation(Class<?> systemPropertyBaseClass, Properties initParameters)
//			throws VaadinConfigurationException {
//		Optional<VaadinServletConfiguration> optionalConfigAnnotation = AnnotationReader
//				.getAnnotationFor(systemPropertyBaseClass, VaadinServletConfiguration.class);
//
//		if (!optionalConfigAnnotation.isPresent()) {
//			return;
//		}
//
//		VaadinServletConfiguration configuration = optionalConfigAnnotation.get();
//		Method[] methods = VaadinServletConfiguration.class.getDeclaredMethods();
//		for (Method method : methods) {
//			VaadinServletConfiguration.InitParameterName name = method
//					.getAnnotation(VaadinServletConfiguration.InitParameterName.class);
//			assert name != null
//					: "All methods declared in VaadinServletConfiguration should have a @InitParameterName annotation";
//
//			try {
//				Object value = method.invoke(configuration);
//
//				String stringValue;
//				if (value instanceof Class<?>) {
//					stringValue = ((Class<?>) value).getName();
//				} else {
//					stringValue = value.toString();
//				}
//
//				initParameters.setProperty(name.value(), stringValue);
//			} catch (IllegalAccessException | InvocationTargetException e) {
//				// This should never happen
//				throw new VaadinConfigurationException(
//						"Could not read @VaadinServletConfiguration value " + method.getName(), e);
//			}
//		}
//	}
//}
