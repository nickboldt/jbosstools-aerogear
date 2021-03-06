/*******************************************************************************
 * Copyright (c) 2007-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.cordovasim.plugin.inappbrowser;


import org.jboss.tools.browsersim.browser.IBrowser;
import org.jboss.tools.browsersim.browser.IBrowserFunction;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
public class ExecScriptFunction implements IBrowserFunction {
	private IBrowser browser;
	private IBrowser inAppBrowser;

	public ExecScriptFunction(IBrowser browser, IBrowser inAppBrowser) {
		this.browser = browser;
		this.inAppBrowser = inAppBrowser;
	}

	@Override
	public Object function(Object[] arguments) { // called from ripple.js 'injectScriptCode' function
		String code = null;
		String successCallBackName = null;
		String failCallBackName = null;

		if (arguments != null) {
			if (arguments.length > 0 && arguments[0] != null) {
				code = arguments[0].toString();
			}
			if (arguments.length > 1 && arguments[1] != null) {
				successCallBackName = arguments[1].toString();
			}
			if (arguments.length > 2 && arguments[2] != null) {
				failCallBackName = arguments[2].toString();
			}

			if (code != null) {
				executeCode(code, successCallBackName, failCallBackName);
			} else if (failCallBackName != null) {
				processCallBackFunction(failCallBackName);
			}
		}

		return null;
	}
	
	@SuppressWarnings("nls")
	private void executeCode(String code, String successCallBackName, String failCallBackName) {
		boolean codeSuccessfullyExecuted = false;
		codeSuccessfullyExecuted = inAppBrowser.execute(
													"(function(){"
														+ "var f = function(){" + code + "};"
														+ "if (document.readyState === 'complete') {"
													    + 	"f();"
													    + "} else {"
													    + 	"window.addEventListener('load', f);"
													    + "}"
													+ "})()");
		
		if (successCallBackName != null && codeSuccessfullyExecuted) {
			processCallBackFunction(successCallBackName);
		} else if (failCallBackName != null) {
			processCallBackFunction(failCallBackName);
		}
		
		deleteCallBacksFromWindowProperties(successCallBackName, failCallBackName);
	}
	
	@SuppressWarnings("nls")
	private void processCallBackFunction(String callBackName) {
		browser.execute("(window['" + callBackName + "']())()");
	}
	
	@SuppressWarnings("nls")
	private void deleteCallBacksFromWindowProperties(String successCallBackName, String failCallBackName) {
		browser.execute("(function(){"
								+ "delete window['" + successCallBackName + "'];"
								+ "delete window['" + failCallBackName + "'];"
							 +"})()");
	}
}
