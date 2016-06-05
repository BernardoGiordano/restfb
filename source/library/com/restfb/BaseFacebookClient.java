/**
 * Copyright (c) 2010-2016 Mark Allen, Norbert Bartels.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.restfb;

import static com.restfb.util.StringUtils.isBlank;
import static com.restfb.util.StringUtils.trimToEmpty;
import static com.restfb.util.UrlUtils.urlEncode;
import static java.util.Arrays.asList;

import com.restfb.exception.generator.DefaultLegacyFacebookExceptionGenerator;
import com.restfb.exception.generator.LegacyFacebookExceptionGenerator;
import com.restfb.json.JsonObject;
import com.restfb.json.ParseException;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Base class that contains data and functionality common to {@link DefaultFacebookClient} and
 * {@link DefaultLegacyFacebookClient}.
 * 
 * @author <a href="http://restfb.com">Mark Allen</a>
 * @since 1.5
 */
abstract class BaseFacebookClient {
  /**
   * Handles {@code GET}s and {@code POST}s to the Facebook API endpoint.
   */
  protected WebRequestor webRequestor;

  /**
   * Handles mapping Facebook response JSON to Java objects.
   */
  protected JsonMapper jsonMapper;

  /**
   * legacy facebook exception generator to convert Facebook error json into java exceptions
   */
  private LegacyFacebookExceptionGenerator legacyFacebookExceptionGenerator;

  /**
   * Set of parameter names that user must not specify themselves, since we use these parameters internally.
   */
  protected final Set<String> illegalParamNames = new HashSet<String>();

  /**
   * Set of API calls that can use the read-only endpoint for a performance boost.
   */
  protected final Set<String> readOnlyApiCalls = new HashSet<String>();

  /**
   * Reserved access token parameter name.
   */
  protected static final String ACCESS_TOKEN_PARAM_NAME = "access_token";

  /**
   * Reserved application secret proof parameter name.
   */
  protected static final String APP_SECRET_PROOF_PARAM_NAME = "appsecret_proof";

  /**
   * Logger.
   */
  protected final Logger LOGGER = Logger.getLogger(getClass().getName());

  /**
   * Initializes this Facebook client.
   */
  public BaseFacebookClient() {
    initializeReadOnlyApiCalls();
    legacyFacebookExceptionGenerator = new DefaultLegacyFacebookExceptionGenerator();
  }

  /**
   * fetch the current legacy facebook exception generator implementing the {@see LegacyFacebookExceptionGenerator}
   * interface
   *
   * @return the current legacy facebook exception generator
   */
  public LegacyFacebookExceptionGenerator getLegacyFacebookExceptionGenerator() {
    return legacyFacebookExceptionGenerator;
  }

  /**
   * override the default legacy facebook exception generator to provide a custom handling for the legacy facebook error
   * objects
   *
   * @param legacyExceptionGenerator
   *          the custom legacy exception generator implementing the {@see LegacyFacebookExceptionGenerator} interface
   */
  public void setLegacyFacebookExceptionGenerator(LegacyFacebookExceptionGenerator legacyExceptionGenerator) {
    legacyFacebookExceptionGenerator = legacyExceptionGenerator;
  }

  /**
   * Stores off the set of API calls that support the read-only endpoint.
   * <p>
   * This list was cribbed from the
   * <a href="https://github.com/facebook/php-sdk/blob/master/src/facebook.php" target="_blank">Official PHP Facebook
   * API client</a>.
   * 
   * @since 1.6.3
   */
  protected void initializeReadOnlyApiCalls() {
    readOnlyApiCalls.addAll(asList("admin.getallocation", "admin.getappproperties", "admin.getbannedusers",
      "admin.getlivestreamvialink", "admin.getmetrics", "admin.getrestrictioninfo", "application.getpublicinfo",
      "auth.getapppublickey", "auth.getsession", "auth.getsignedpublicsessiondata", "comments.get",
      "connect.getunconnectedfriendscount", "dashboard.getactivity", "dashboard.getcount", "dashboard.getglobalnews",
      "dashboard.getnews", "dashboard.multigetcount", "dashboard.multigetnews", "data.getcookies", "events.get",
      "events.getmembers", "fbml.getcustomtags", "feed.getappfriendstories", "feed.getregisteredtemplatebundlebyid",
      "feed.getregisteredtemplatebundles", "fql.multiquery", "fql.query", "friends.arefriends", "friends.get",
      "friends.getappusers", "friends.getlists", "friends.getmutualfriends", "gifts.get", "groups.get",
      "groups.getmembers", "intl.gettranslations", "links.get", "notes.get", "notifications.get", "pages.getinfo",
      "pages.isadmin", "pages.isappadded", "pages.isfan", "permissions.checkavailableapiaccess",
      "permissions.checkgrantedapiaccess", "photos.get", "photos.getalbums", "photos.gettags", "profile.getinfo",
      "profile.getinfooptions", "stream.get", "stream.getcomments", "stream.getfilters", "users.getinfo",
      "users.getloggedinuser", "users.getstandardinfo", "users.hasapppermission", "users.isappuser", "users.isverified",
      "video.getuploadlimits"));
  }

  /**
   * Appends the given {@code parameter} to the given {@code parameters} array.
   * 
   * @param parameter
   *          The parameter value to append.
   * @param parameters
   *          The parameters to which the given {@code parameter} is appended.
   * @return A new array which contains both {@code parameter} and {@code parameters}.
   */
  protected Parameter[] parametersWithAdditionalParameter(Parameter parameter, Parameter... parameters) {
    Parameter[] updatedParameters = new Parameter[parameters.length + 1];
    System.arraycopy(parameters, 0, updatedParameters, 0, parameters.length);
    updatedParameters[parameters.length] = parameter;
    return updatedParameters;
  }

  /**
   * Given a map of query names to queries, verify that it contains valid data and convert it to a JSON object string.
   * 
   * @param queries
   *          The query map to convert.
   * @return The {@code queries} in JSON string format.
   * @throws IllegalArgumentException
   *           If the provided {@code queries} are invalid.
   */
  protected String queriesToJson(Map<String, String> queries) {
    verifyParameterPresence("queries", queries);

    if (queries.keySet().isEmpty()) {
      throw new IllegalArgumentException("You must specify at least one query.");
    }

    JsonObject jsonObject = new JsonObject();

    for (Entry<String, String> entry : queries.entrySet()) {
      if (isBlank(entry.getKey()) || isBlank(entry.getValue())) {
        throw new IllegalArgumentException(
          "Provided queries must have non-blank keys and values. You provided: " + queries);
      }

      try {
        jsonObject.add(trimToEmpty(entry.getKey()), trimToEmpty(entry.getValue()));
      } catch (ParseException e) {
        // Shouldn't happen unless bizarre input is provided
        throw new IllegalArgumentException("Unable to convert " + queries + " to JSON.", e);
      }
    }

    return jsonObject.toString();
  }

  /**
   * Gets the URL-encoded version of the given {@code value} for the parameter named {@code name}.
   * <p>
   * Includes special-case handling for access token parameters where we check if the token is already URL-encoded - if
   * so, we don't encode again. All other parameter types are always URL-encoded.
   * 
   * @param name
   *          The name of the parameter whose value should be URL-encoded and returned.
   * @param value
   *          The value of the parameter which should be URL-encoded and returned.
   * @return The URL-encoded version of the given {@code value}.
   */
  protected String urlEncodedValueForParameterName(String name, String value) {
    // Special handling for access_token -
    // '%7C' is the pipe character and will be present in any access_token
    // parameter that's already URL-encoded. If we see this combination, don't
    // URL-encode. Otherwise, URL-encode as normal.
    return ACCESS_TOKEN_PARAM_NAME.equals(name) && value.contains("%7C") ? value : urlEncode(value);
  }

  /**
   * Given an api call (e.g. "me" or "fql.query"), returns the correct FB API endpoint to use.
   * <p>
   * Useful for returning the read-only API endpoint where possible.
   * 
   * @param apiCall
   *          The FB API call (Graph or Old REST API) for which we'd like an endpoint.
   * @param hasAttachment
   *          Are we including a multipart file when making this API call?
   * @return An absolute endpoint URL to communicate with.
   * @since 1.6.3
   */
  protected abstract String createEndpointForApiCall(String apiCall, boolean hasAttachment);

  /**
   * Returns the base read-only endpoint URL.
   * 
   * @return The base read-only endpoint URL.
   * @since 1.6.3
   */
  protected abstract String getFacebookReadOnlyEndpointUrl();

  /**
   * Verifies that the provided parameter names don't collide with the ones we internally pass along to Facebook.
   * 
   * @param parameters
   *          The parameters to check.
   * @throws IllegalArgumentException
   *           If there's a parameter name collision.
   */
  protected void verifyParameterLegality(Parameter... parameters) {
    for (Parameter parameter : parameters)
      if (illegalParamNames.contains(parameter.name)) {
        throw new IllegalArgumentException(
          "Parameter '" + parameter.name + "' is reserved for RestFB use - " + "you cannot specify it yourself.");
      }
  }

  /**
   * Ensures that {@code parameter} isn't {@code null} or an empty string.
   * 
   * @param parameterName
   *          The name of the parameter (to be used in exception message).
   * @param parameter
   *          The parameter to check.
   * @throws IllegalArgumentException
   *           If {@code parameter} is {@code null} or an empty string.
   */
  protected void verifyParameterPresence(String parameterName, String parameter) {
    verifyParameterPresence(parameterName, (Object) parameter);
    if (parameter.trim().length() == 0) {
      throw new IllegalArgumentException("The '" + parameterName + "' parameter cannot be an empty string.");
    }
  }

  /**
   * Ensures that {@code parameter} isn't {@code null}.
   * 
   * @param parameterName
   *          The name of the parameter (to be used in exception message).
   * @param parameter
   *          The parameter to check.
   * @throws IllegalArgumentException
   *           If {@code parameter} is {@code null}.
   */
  protected void verifyParameterPresence(String parameterName, Object parameter) {
    if (parameter == null) {
      throw new NullPointerException("The '" + parameterName + "' parameter cannot be null.");
    }
  }
}